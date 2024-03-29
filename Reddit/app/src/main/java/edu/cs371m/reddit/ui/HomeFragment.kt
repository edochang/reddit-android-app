package edu.cs371m.reddit.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import edu.cs371m.reddit.databinding.FragmentRvBinding

// XXX Write most of this file
class HomeFragment: Fragment() {
    // XXX initialize viewModel\
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentRvBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    // Set up the adapter and recycler view
    private fun initAdapter(binding: FragmentRvBinding) {
        val postRowAdapter = PostRowAdapter(viewModel) {
            Log.d("OnePost",
                String.format("OnePost title %s",
                    if (it.title.length > 32)
                        it.title.substring(0, 31) + "..."
                    else it.title))
            Log.d("doOnePost", "image ${it.imageURL}")
            // XXX Write me
            val direction = HomeFragmentDirections.actionHomeFragmentToOnePostFragment(it)
            findNavController().navigate(direction)

            Log.d("doOnePost", "RedditPost: $it")
        }
        // XXX Write me, observe posts
        binding.recyclerView.adapter = postRowAdapter

        viewModel.observeLivePosts().observe(viewLifecycleOwner) {
            postRowAdapter.submitList(it)
            viewModel.fetchDone.value = true
        }
    }

    private fun initSwipeLayout(swipe : SwipeRefreshLayout) {
        // XXX Write me
        swipe.setOnRefreshListener {
            viewModel.fetchDone.value = false
            viewModel.repoFetch()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(javaClass.simpleName, "onViewCreated")
        // XXX Write me.  Set title based on current subreddit
        initSwipeLayout(binding.swipeRefreshLayout)
        viewModel.showActionBarFavorites()
        // Fetch over the network Reddit posts
        viewModel.observeSubreddit().observe(viewLifecycleOwner) {
            val title = String.format("r/%s", it)
            viewModel.setTitle(title)
        }
        viewModel.repoFetch()

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        initAdapter(binding)

        // Listen to fetchDone and cancel the swipeRefreshLayout.isRefreshing status
        viewModel.fetchDone.observe(viewLifecycleOwner) {
            // XXX Write me, what does fetchDone mean?
            binding.swipeRefreshLayout.isRefreshing = !it
        }
    }
}