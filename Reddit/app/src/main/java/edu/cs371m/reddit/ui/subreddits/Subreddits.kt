package edu.cs371m.reddit.ui.subreddits

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cs371m.reddit.MainActivity
import edu.cs371m.reddit.R
import edu.cs371m.reddit.databinding.FragmentRvBinding
import edu.cs371m.reddit.ui.MainViewModel

class Subreddits : Fragment(R.layout.fragment_rv) {
    // XXX initialize viewModel
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentRvBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    // XXX Write me, onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(javaClass.simpleName, "onViewCreated")
        _binding = FragmentRvBinding.bind(view)
        binding.swipeRefreshLayout.isEnabled = false
        viewModel.setTitle("Subreddits")
        viewModel.hideActionBarFavorites()

        val subredditListAdapter = SubredditListAdapter(viewModel, findNavController())

        // XXX Write me, observe posts
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = subredditListAdapter
        viewModel.observeLiveSubreddits().observe(viewLifecycleOwner) {
            subredditListAdapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}