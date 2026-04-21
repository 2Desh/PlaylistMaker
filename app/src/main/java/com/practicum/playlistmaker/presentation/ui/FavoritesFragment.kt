package com.practicum.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentFavoritesBinding
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.adapters.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

// Фрагмент для отображения и управления экраном "Избранные треки"
class FavoritesFragment : Fragment() {

    private val viewModel by viewModel<FavoritesViewModel>()

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var trackAdapter: TrackAdapter
    private val favoriteTracks = ArrayList<Track>()

    companion object {
        fun newInstance() = FavoritesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            renderState(state)
        }
    }

    private fun setupRecyclerView() {
        trackAdapter = TrackAdapter(favoriteTracks) { track ->
            openPlayer(track)
        }

        binding.favoriteRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.favoriteRecyclerView.adapter = trackAdapter
    }

    private fun renderState(state: FavoritesState) {
        when (state) {
            is FavoritesState.Empty -> {
                // Показываем заглушку, скрываем список
                binding.favoriteRecyclerView.isVisible = false
                binding.placeholderImageView.isVisible = true
                binding.placeholderTextView.isVisible = true
            }
            is FavoritesState.Content -> {
                // Показываем список, скрываем заглушку
                binding.placeholderImageView.isVisible = false
                binding.placeholderTextView.isVisible = false
                binding.favoriteRecyclerView.isVisible = true

                favoriteTracks.clear()
                favoriteTracks.addAll(state.tracks)
                trackAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun openPlayer(track: Track) {
        val bundle = Bundle().apply {
            putParcelable(PlayerFragment.TRACK_KEY, track)
        }
        findNavController().navigate(R.id.playerFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}