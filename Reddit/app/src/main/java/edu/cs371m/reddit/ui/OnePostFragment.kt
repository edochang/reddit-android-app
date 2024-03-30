package edu.cs371m.reddit.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import edu.cs371m.reddit.databinding.FragmentOnePostBinding
import edu.cs371m.reddit.glide.Glide

class OnePostFragment : Fragment(){
    private var _binding: FragmentOnePostBinding? = null
    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!
    // SafeArgs plugins
    private val args: OnePostFragmentArgs by navArgs()

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val post = args.post
        viewModel.setTitle("One Post")
        viewModel.hideActionBarFavorites()
        /*
        viewModel.observeSubreddit().observe(viewLifecycleOwner) {
            binding.onePostSubreddit.text = String.format("r/%s", it)
        }
         */
        binding.onePostSubreddit.text = String.format("r/%s", post.subreddit)
        binding.onePostTitle.text = post.title

        if (post.selfText.isNullOrBlank()) {
            binding.onePostSelfText.text = ""
            binding.onePostSelfText.visibility = View.GONE
        } else {
            binding.onePostSelfText.text = post.selfText
            binding.onePostSelfText.visibility = View.VISIBLE
        }

        //binding.onePostSelfText.text = post.selfText

        Glide.glideFetch(post.imageURL, post.thumbnailURL, binding.onePostImage)

        Log.d(javaClass.simpleName, "Image URLs: ${post.imageURL}, ${post.thumbnailURL}")

        viewModel.observeSearchTerm().observe(viewLifecycleOwner) {
            post.searchFor(it)
            binding.onePostTitle.text = post.title
            binding.onePostSelfText.text = post.selfText
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}