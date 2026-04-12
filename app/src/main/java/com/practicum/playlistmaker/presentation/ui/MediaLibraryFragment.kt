package com.practicum.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentMedialibraryBinding

// Фрагмент Медиатеки
class MediaLibraryFragment : Fragment() {

    private var _binding: FragmentMedialibraryBinding? = null
    private val binding get() = _binding!!
    private var tabMediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedialibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.libraryToolbar.navigationIcon = null

        binding.viewPager.adapter = MediaLibraryPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)

        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.favorites_tracks)
                1 -> tab.text = getString(R.string.playlists)
            }
        }
        tabMediator?.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabMediator?.detach()
        _binding = null
    }
}