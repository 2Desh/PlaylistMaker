package com.practicum.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistsBinding
import com.practicum.playlistmaker.presentation.adapters.PlaylistAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

// Фрагмент для визуального отображения и управления экраном "Плейлисты"
class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<PlaylistsViewModel>()
    private var adapter: PlaylistAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO
        adapter = PlaylistAdapter(emptyList()) { playlist ->
        }

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter

        // Переход на экран создания плейлиста
        binding.newPlaylistButton.setOnClickListener {
            findNavController().navigate(R.id.playlistCreateFragment)
        }

        // Наблюдаем за состоянием экрана
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    // Обновляем данные каждый раз, когда возвращаемся на экран
    override fun onResume() {
        super.onResume()
        viewModel.fillData()
    }

    private fun render(state: PlaylistsState) {
        when (state) {
            is PlaylistsState.Empty -> showEmpty()
            is PlaylistsState.Content -> showContent(state.playlists)
        }
    }

    private fun showEmpty() {
        binding.recyclerView.visibility = View.GONE
        binding.placeholderLayout.visibility = View.VISIBLE
    }

    private fun showContent(playlists: List<com.practicum.playlistmaker.domain.models.Playlist>) {
        binding.placeholderLayout.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE

        adapter?.playlists = playlists
        adapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapter = null
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}