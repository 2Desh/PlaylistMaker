package com.practicum.playlistmaker.presentation.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentAudioplayerBinding
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.adapters.BottomSheetPlaylistAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

// Фрагмент Аудиоплеера
class PlayerFragment : Fragment() {

    private val viewModel by viewModel<PlayerViewModel>()

    private var _binding: FragmentAudioplayerBinding? = null
    private val binding get() = _binding!!

    private var currentTrack: Track? = null
    private var bottomSheetAdapter: BottomSheetPlaylistAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioplayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        setupBottomSheet()

        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(TRACK_KEY, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(TRACK_KEY)
        }

        if (track != null) {
            currentTrack = track
            bindTrackData(track)

            // Сообщаем ViewModel текущее состояние избранного
            viewModel.checkIsFavorite(track.isFavorite)

            viewModel.setTrackId(track.trackId)

            val url = track.previewUrl
            if (!url.isNullOrEmpty()) {
                viewModel.preparePlayer(url)
            }
        }

        observeViewModel()
    }

    private fun initViews() {
        binding.playerToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.playPauseButton.setOnClickListener {
            viewModel.playbackControl()
        }

        binding.likeButton.setOnClickListener {
            currentTrack?.let { track ->
                viewModel.onFavoriteClicked(track)
            }
        }
    }

    private fun setupBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
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

        binding.addPlaylistButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.newPlaylistBottomSheetButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(R.id.playlistCreateFragment)
        }

        bottomSheetAdapter = BottomSheetPlaylistAdapter(emptyList()) { playlist ->
            currentTrack?.let { track ->
                viewModel.addTrackToPlaylist(playlist, track)
            }
        }
        binding.playlistsBottomSheetRecyclerView.adapter = bottomSheetAdapter
    }

    private fun observeViewModel() {
        // Подписка на состояния плеера
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            renderState(state)
        }

        // Подписка на статус избранного
        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            if (isFavorite) {
                binding.likeButton.setImageResource(R.drawable.ic_liked)
            } else {
                binding.likeButton.setImageResource(R.drawable.ic_like)
            }
        }
        viewModel.isAddedToPlaylist.observe(viewLifecycleOwner) { isAdded ->
            if (isAdded) {
                // Если трек есть хотя бы в одном плейлисте, отображаем соответствующую иконку.
                binding.addPlaylistButton.setImageResource(R.drawable.ic_added_to_playlist)
            } else {
                binding.addPlaylistButton.setImageResource(R.drawable.ic_add_to_playlist)
            }
        }
        // Подписка на список плейлистов
        viewModel.playlistsLiveData.observe(viewLifecycleOwner) { playlists ->
            bottomSheetAdapter?.playlists = playlists
            bottomSheetAdapter?.notifyDataSetChanged()
        }

        // Подписка на сообщения о добавлении трека
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                // Если трек успешно добавлен, прячем шторку
                if (message.startsWith("Добавлено")) {
                    val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }

                viewModel.toastMessageShown()
            }
        }
    }

    private fun bindTrackData(track: Track) {
        binding.trackNameTextView.text = track.trackName
        binding.artistNameTextView.text = track.artistName

        val formattedTime = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTime)
        binding.durationValueTextView.text = formattedTime
        binding.collectionNameValueTextView.text = track.collectionName

        val year = track.releaseDate?.take(4) ?: ""
        binding.releaseDateValueTextView.text = year

        binding.primaryGenreNameValueTextView.text = track.primaryGenreName
        binding.countryValueTextView.text = track.country

        val artworkUrl512 = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

        Glide.with(this)
            .load(artworkUrl512)
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.player_artwork_corner_radius)))
            .into(binding.artworkImageView)
    }

    private fun renderState(state: PlayerState) {
        when (state) {
            is PlayerState.Default -> {
                binding.playPauseButton.isEnabled = false
                binding.playPauseButton.setImageResource(R.drawable.ic_play)
                binding.playbackProgressTextView.text = state.progress
            }
            is PlayerState.Prepared -> {
                binding.playPauseButton.isEnabled = true
                binding.playPauseButton.setImageResource(R.drawable.ic_play)
                binding.playbackProgressTextView.text = state.progress
            }
            is PlayerState.Playing -> {
                binding.playPauseButton.setImageResource(R.drawable.ic_pause)
                binding.playbackProgressTextView.text = state.progress
            }
            is PlayerState.Paused -> {
                binding.playPauseButton.setImageResource(R.drawable.ic_play)
                binding.playbackProgressTextView.text = state.progress
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        bottomSheetAdapter = null
    }

    companion object {
        const val TRACK_KEY = "track_key"
    }
}