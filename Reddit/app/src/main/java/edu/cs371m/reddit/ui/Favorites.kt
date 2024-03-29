package edu.cs371m.reddit.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cs371m.reddit.R
import edu.cs371m.reddit.databinding.FragmentRvBinding

class Favorites: Fragment(R.layout.fragment_rv) {
    // XXX initialize viewModel
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentRvBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(javaClass.simpleName, "onViewCreated")
        _binding = FragmentRvBinding.bind(view)
        viewModel.setTitle("Favorites")
        viewModel.hideActionBarFavorites()
        // XXX Write me
        binding.swipeRefreshLayout.isEnabled = false
        val postRowAdapter = PostRowAdapter(viewModel) {
            Log.d("OnePost",
                String.format("OnePost title %s",
                    if (it.title.length > 32)
                        it.title.substring(0, 31) + "..."
                    else it.title))
            Log.d("doOnePost", "image ${it.imageURL}")
            // XXX Write me
            val direction = FavoritesDirections.actionFavoritesFragmentToOnePostFragment(it)
            findNavController().navigate(direction)
        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = postRowAdapter

        //postRowAdapter.submitList(viewModel.getFavoriteRedditPosts().values.toList())

        viewModel.observeLiveFavoriteRedditPosts().observe(viewLifecycleOwner) {
            postRowAdapter.submitList(it)
        }
    }
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}