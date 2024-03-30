package edu.cs371m.reddit.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.cs371m.reddit.MainActivity
import edu.cs371m.reddit.api.RedditPost
import edu.cs371m.reddit.databinding.RowPostBinding
import edu.cs371m.reddit.glide.Glide

/**
 * Created by witchel on 8/25/2019
 */

// https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter
// Slick adapter that provides submitList, so you don't worry about how to update
// the list, you just submit a new one when you want to change the list and the
// Diff class computes the smallest set of changes that need to happen.
// NB: Both the old and new lists must both be in memory at the same time.
// So you can copy the old list, change it into a new list, then submit the new list.
//
// You can call adapterPosition to get the index of the selected item
class PostRowAdapter(private val viewModel: MainViewModel,
                     private val navigateToOnePost: (redditPost: RedditPost)->Unit )
    : ListAdapter<RedditPost, PostRowAdapter.VH>(RedditDiff()) {
    inner class VH(val rowPostBinding: RowPostBinding)
        : RecyclerView.ViewHolder(rowPostBinding.root) {
        //init {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val rowBinding = RowPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return VH(rowBinding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        //Log.d(javaClass.simpleName,"VH called with position: $position; currentIndex: ${viewModel.currentIndex}")
        val postInfo = getItem(position)
        val rowBinding = holder.rowPostBinding
        rowBinding.title.text = postInfo.title
        if (postInfo.selfText.isNullOrBlank()) {
            rowBinding.selfText.text = ""
            rowBinding.selfText.visibility = View.GONE
        } else {
            rowBinding.selfText.text = postInfo.selfText
            rowBinding.selfText.visibility = View.VISIBLE
        }
        Glide.glideFetch(postInfo.imageURL, postInfo.thumbnailURL, rowBinding.image)
        rowBinding.score.text = postInfo.score.toString()
        rowBinding.comments.text = postInfo.commentCount.toString()

        // setOnClickListener()
        rowBinding.title.setOnClickListener() {
            navigateToOnePost(postInfo)
        }
        rowBinding.selfText.setOnClickListener() {
            navigateToOnePost(postInfo)
        }
        rowBinding.image.setOnClickListener() {
            navigateToOnePost(postInfo)
        }

        viewModel.isFavoriteRedditPost(postInfo)?.let{
            if (it) {
                Log.d(javaClass.simpleName, ">>> Make post favorite on bind: ${postInfo.key}")
                rowBinding.rowFav.setImageResource(MainActivity.favoriteDrawable)
            } else {
                rowBinding.rowFav.setImageResource(MainActivity.unfavoriteDrawable)
            }
        }

        rowBinding.rowFav.setOnClickListener() {
            Log.d(javaClass.simpleName, ">>> rowFav setOnClickListener")
            viewModel.isFavoriteRedditPost(postInfo)?.let{
                if (it) {
                    viewModel.setFavoriteRedditPost(postInfo, false)
                    rowBinding.rowFav.setImageResource(MainActivity.unfavoriteDrawable)
                } else {
                    Log.d(javaClass.simpleName, ">>> Make post favorite on click: ${postInfo.key}")
                    viewModel.setFavoriteRedditPost(postInfo, true)
                    rowBinding.rowFav.setImageResource(MainActivity.favoriteDrawable)
                }
            }


            /*
            if (viewModel.getFavoriteRedditPosts().contains(postInfo.key)) {
                viewModel.setFavoriteRedditPost(postInfo, false)
                rowBinding.rowFav.setImageResource(MainActivity.unfavoriteDrawable)
            } else {
                viewModel.setFavoriteRedditPost(postInfo, true)
                rowBinding.rowFav.setImageResource(MainActivity.favoriteDrawable)
            }
             */
        }
    }

    class RedditDiff : DiffUtil.ItemCallback<RedditPost>() {
        override fun areItemsTheSame(oldItem: RedditPost, newItem: RedditPost): Boolean {
            return oldItem.key == newItem.key
        }
        override fun areContentsTheSame(oldItem: RedditPost, newItem: RedditPost): Boolean {
            return RedditPost.spannableStringsEqual(oldItem.title, newItem.title) &&
                    RedditPost.spannableStringsEqual(oldItem.selfText, newItem.selfText) &&
                    RedditPost.spannableStringsEqual(oldItem.publicDescription, newItem.publicDescription) &&
                    RedditPost.spannableStringsEqual(oldItem.displayName, newItem.displayName)

        }
    }
}

