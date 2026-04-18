package com.practicum.playlistmaker.presentation.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentSearchBinding
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.adapters.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

// Фрагмент Поиска
class SearchFragment : Fragment() {

    private val viewModel by viewModel<SearchViewModel>()

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val tracks = ArrayList<Track>()
    private val historyTracks = ArrayList<Track>()

    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleSearch.navigationIcon = null

        setupAdapters()
        setupListeners()
        observeViewModel()
    }

    private fun setupAdapters() {
        trackAdapter = TrackAdapter(tracks) { track ->
            if (viewModel.clickDebounce()) {
                viewModel.addToHistory(track)
                openPlayer(track)
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = trackAdapter

        historyAdapter = TrackAdapter(historyTracks) { track ->
            if (viewModel.clickDebounce()) {
                viewModel.addToHistory(track)
                openPlayer(track)
            }
        }
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = historyAdapter
    }

    private fun setupListeners() {
        binding.clearIcon.setOnClickListener {
            binding.inputEditText.setText("")
            hideKeyboard()
            viewModel.showHistory()
        }

        binding.refreshButton.setOnClickListener { viewModel.refreshSearch() }
        binding.clearHistoryButton.setOnClickListener { viewModel.clearHistory() }

        binding.inputEditText.doOnTextChanged { text, _, _, _ ->
            binding.clearIcon.isVisible = !text.isNullOrEmpty()
            if (binding.inputEditText.hasFocus() && text.isNullOrEmpty()) {
                viewModel.showHistory()
            } else if (!text.isNullOrEmpty() && text.length >= 3) {
                viewModel.searchDebounce(text.toString())
            }
        }

        binding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.inputEditText.text.isEmpty()) viewModel.showHistory()
        }

        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.inputEditText.text.toString()
                if (query.isNotEmpty()) {
                    viewModel.searchRequest(query)
                }
                true
            } else false
        }
    }

    private fun observeViewModel() {
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            hideAllViews()
            when (state) {
                is TracksSearchState.Loading -> binding.progressBar.isVisible = true
                is TracksSearchState.Content -> {
                    binding.recyclerView.isVisible = true
                    tracks.clear()
                    tracks.addAll(state.tracks)
                    trackAdapter.notifyDataSetChanged()
                }
                is TracksSearchState.Empty -> binding.placeholderNoResults.isVisible = true
                is TracksSearchState.Error -> binding.placeholderServerError.isVisible = true
                is TracksSearchState.History -> {
                    binding.historyLayout.isVisible = true
                    historyTracks.clear()
                    historyTracks.addAll(state.tracks)
                    historyAdapter.notifyDataSetChanged()
                }
                else -> Unit
            }
        }
    }

    private fun hideAllViews() {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = false
        binding.historyLayout.isVisible = false
        binding.placeholderNoResults.isVisible = false
        binding.placeholderServerError.isVisible = false
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
    }

    private fun openPlayer(track: Track) {
        val bundle = Bundle().apply {
            putParcelable(TRACK_KEY, track)
        }
        findNavController().navigate(R.id.playerFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TRACK_KEY = "track_key"
    }
}