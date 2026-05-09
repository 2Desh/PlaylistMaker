package com.practicum.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistBinding
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.adapters.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File
import java.util.concurrent.TimeUnit

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    // Получаем ID из аргументов навигации
    private val playlistId by lazy { arguments?.getLong("playlistId") ?: 0L }

    // Передаем параметр во ViewModel
    private val viewModel: PlaylistViewModel by viewModel { parametersOf(playlistId) }

    private var trackAdapter: TrackAdapter? = null
    private var menuBottomSheetBehavior: BottomSheetBehavior<*>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupBottomSheets()

        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlaylistState.Loading -> Unit 
                is PlaylistState.Content -> renderContent(state.playlist, state.tracks, state.totalDurationMinutes)
            }
        }
    }

    private fun setupUI() {
        binding.toolbar.setOnClickListener {
            findNavController().navigateUp()
        }

        trackAdapter = TrackAdapter(
            tracks = arrayListOf(),
            onTrackClickListener = { track ->
                val bundle = Bundle().apply { putParcelable(PlayerFragment.TRACK_KEY, track) }
                findNavController().navigate(R.id.playerFragment, bundle)
            },
            onTrackLongClickListener = { track ->
                showDeleteTrackDialog(track)
                true
            }
        )

        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksRecyclerView.adapter = trackAdapter

        // Клики
        binding.menuButton.setOnClickListener {
            menuBottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.shareButton.setOnClickListener { sharePlaylist() }
        binding.shareMenuButton.setOnClickListener {
            menuBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            sharePlaylist()
        }

        binding.deleteMenuButton.setOnClickListener {
            menuBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            confirmDeletePlaylist()
        }

        binding.editMenuButton.setOnClickListener {
            menuBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

            val currentState = viewModel.stateLiveData.value
            if (currentState is PlaylistState.Content) {
                val bundle = Bundle().apply {
                    putSerializable("playlist", currentState.playlist)
                }
                findNavController().navigate(R.id.playlistCreateFragment, bundle)
            }
        }
    }

    private fun sharePlaylist() {
        val currentState = viewModel.stateLiveData.value
        if (currentState is PlaylistState.Content) {
            if (currentState.tracks.isEmpty()) {
                Toast.makeText(requireContext(), "В этом плейлисте нет списка треков, которым можно поделиться", Toast.LENGTH_SHORT).show()
                return
            }

            // Формируем текст сообщения
            var shareText = "${currentState.playlist.name}\n"
            if (!currentState.playlist.description.isNullOrEmpty()) {
                shareText += "${currentState.playlist.description}\n"
            }
            val tracksCountStr = resources.getQuantityString(R.plurals.tracks_count, currentState.playlist.trackCount, currentState.playlist.trackCount)
            shareText += "$tracksCountStr\n"

            currentState.tracks.forEachIndexed { index, track ->
                val minutes = TimeUnit.MILLISECONDS.toMinutes(track.trackTime)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(track.trackTime) % 60
                val timeString = String.format("%02d:%02d", minutes, seconds)
                shareText += "${index + 1}. ${track.artistName} - ${track.trackName} ($timeString)\n"
            }

            // Запускаем системный диалог "поделиться"
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            }
            startActivity(android.content.Intent.createChooser(intent, "Поделиться плейлистом"))
        }
    }

    private fun confirmDeletePlaylist() {
        // Достаем название плейлиста
        val currentState = viewModel.stateLiveData.value
        val playlistName = if (currentState is PlaylistState.Content) currentState.playlist.name else ""

        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialogTheme)
            .setTitle("Хотите удалить плейлист «$playlistName»?")
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Да") { dialog, _ ->
                viewModel.deletePlaylist()
                dialog.dismiss()
                findNavController().navigateUp()
            }
            .show()
    }

    private fun setupBottomSheets() {
        // Настройка шторки со списком треков
        val tracksBottomSheetBehavior = BottomSheetBehavior.from(binding.tracksBottomSheet)

        binding.root.post {
            if (_binding == null) return@post

            val screenHeight = binding.root.height
            val shareButtonBottom = binding.shareButton.bottom
            val spacer = (24 * resources.displayMetrics.density).toInt()

            tracksBottomSheetBehavior.peekHeight = screenHeight - shareButtonBottom - spacer
        }

        // Настройка шторки с меню
        menuBottomSheetBehavior = BottomSheetBehavior.from(binding.menuBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        menuBottomSheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> _binding?.overlay?.visibility = View.GONE
                    else -> _binding?.overlay?.visibility = View.VISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                _binding?.overlay?.alpha = slideOffset + 1f
            }
        })
    }

    private fun renderContent(playlist: Playlist, tracks: List<Track>, durationMinutes: String) {
        binding.playlistNameTextView.text = playlist.name

        if (playlist.description.isNullOrEmpty()) {
            binding.playlistDescriptionTextView.visibility = View.GONE
        } else {
            binding.playlistDescriptionTextView.visibility = View.VISIBLE
            binding.playlistDescriptionTextView.text = playlist.description
        }

        // Формируем строку
        val tracksCountStr = resources.getQuantityString(R.plurals.tracks_count, playlist.trackCount, playlist.trackCount)
        val minutesCountStr = resources.getQuantityString(R.plurals.minutes_count, durationMinutes.toIntOrNull() ?: 0, durationMinutes.toIntOrNull() ?: 0)
        binding.playlistStatsTextView.text = "$minutesCountStr • $tracksCountStr"

        // Загрузка обложки плейлиста
        if (!playlist.coverFilePath.isNullOrEmpty()) {
            val file = File(playlist.coverFilePath)
            if (file.exists()) {
                Glide.with(this).load(file).into(binding.playlistCoverImageView)
            } else {
                binding.playlistCoverImageView.setImageResource(R.drawable.placeholder)
            }
        } else {
            binding.playlistCoverImageView.setImageResource(R.drawable.placeholder)
        }

        // Обновляем список треков
        if (tracks.isEmpty()) {
            binding.emptyTracksMessage.visibility = View.VISIBLE
            binding.tracksRecyclerView.visibility = View.GONE
        } else {
            binding.emptyTracksMessage.visibility = View.GONE
            binding.tracksRecyclerView.visibility = View.VISIBLE
            trackAdapter?.tracks?.clear()
            trackAdapter?.tracks?.addAll(tracks)
            trackAdapter?.notifyDataSetChanged()
        }

        // Заполняем карточку плейлиста в меню ботомшите
        binding.menuPlaylistInfo.playlistNameTextView.text = playlist.name
        binding.menuPlaylistInfo.tracksCountTextView.text = tracksCountStr

        if (!playlist.coverFilePath.isNullOrEmpty()) {
            val file = File(playlist.coverFilePath)
            if (file.exists()) {
                Glide.with(this)
                    .load(file)
                    .transform(CenterCrop(), RoundedCorners(resources.getDimensionPixelSize(R.dimen.cover_corner_radius)))
                    .placeholder(R.drawable.placeholder)
                    .into(binding.menuPlaylistInfo.playlistCoverImageView)
            } else {
                binding.menuPlaylistInfo.playlistCoverImageView.setImageResource(R.drawable.placeholder)
            }
        } else {
            binding.menuPlaylistInfo.playlistCoverImageView.setImageResource(R.drawable.placeholder)
        }
    }

    private fun showDeleteTrackDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialogTheme)
            .setTitle("Хотите удалить трек?")
            .setNegativeButton("НЕТ") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("ДА") { dialog, _ ->
                viewModel.deleteTrack(track.trackId)
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        trackAdapter = null
    }
}