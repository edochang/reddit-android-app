package edu.cs371m.reddit.ui.subreddits

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.cs371m.reddit.api.RedditPost
import edu.cs371m.reddit.databinding.RowSubredditBinding
import edu.cs371m.reddit.glide.Glide
import edu.cs371m.reddit.ui.MainViewModel
import edu.cs371m.reddit.ui.PostRowAdapter

// NB: Could probably unify with PostRowAdapter if we had two
// different VH and override getItemViewType
// https://medium.com/@droidbyme/android-recyclerview-with-multiple-view-type-multiple-view-holder-af798458763b
class SubredditListAdapter(private val viewModel: MainViewModel,
                           private val navController: NavController
)
    : ListAdapter<RedditPost, SubredditListAdapter.VH>(PostRowAdapter.RedditDiff()) {

    // ViewHolder pattern
    inner class VH(val rowSubredditBinding: RowSubredditBinding)
        : RecyclerView.ViewHolder(rowSubredditBinding.root) {

        // XXX Write me
        //init {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val rowBinding = RowSubredditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return VH(rowBinding)
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        // XXX Write me
        val subreddit = getItem(position)
        val rowBinding = holder.rowSubredditBinding
        holder.itemView.setOnClickListener() {
            Log.d(javaClass.simpleName, "subreddit post data: $subreddit")
            val subredditName = subreddit.displayName.toString()
            //val title = String.format("r/%s", subredditName)
            //viewModel.setTitle(title)
            viewModel.setSubreddit(subredditName)
            navController.navigateUp()
        }

        subreddit.iconURL?.let {
            Glide.glideFetch(subreddit.iconURL, subreddit.iconURL, rowBinding.subRowPic)
        }
        rowBinding.subRowHeading.text = subreddit.displayName
        rowBinding.subRowDetails.text = subreddit.publicDescription
    }
}
