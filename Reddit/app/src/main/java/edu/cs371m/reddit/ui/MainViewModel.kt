package edu.cs371m.reddit.ui


import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs371m.reddit.api.RedditApi
import edu.cs371m.reddit.api.RedditPost
import edu.cs371m.reddit.api.RedditPostRepository
import edu.cs371m.reddit.databinding.ActionBarBinding
import edu.cs371m.reddit.ui.subreddits.Subreddits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableMap

// XXX Much to write
class MainViewModel : ViewModel() {
    private var title = MutableLiveData<String>()
    private var searchTerm = MutableLiveData<String>()
    private var subreddit = MutableLiveData<String>().apply {
        value = "aww"
    }
    private var actionBarBinding : ActionBarBinding? = null
    // XXX Write me, api, repository, favorites
    // netSubreddits fetches the list of subreddits
    // We only do this once, so technically it does not need to be
    // MutableLiveData, or even really LiveData.  But maybe in the future
    // we will refetch it.
    private val redditAPI = RedditApi.create()
    private val redditPostRepository = RedditPostRepository(redditAPI)
    var fetchDone: MutableLiveData<Boolean> = MutableLiveData(false)
    val fetch429: MutableLiveData<Boolean> = MutableLiveData(false)

    private val favoriteRedditPosts = MutableLiveData<List<RedditPost>>().apply {
        postValue(mutableListOf())
    }
    /*
    private val favoriteRedditPosts = MutableLiveData<Map<String, RedditPost>>().apply {
        postValue(mutableMapOf())
    }
     */

    private var netSubreddits = MutableLiveData<List<RedditPost>>().apply {
        // XXX Write me, viewModelScope.launch getSubreddits()
        viewModelScope.launch(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            try {
                Log.d(javaClass.simpleName, "Calling netSubreddits...")
                postValue(redditPostRepository.getSubreddits())
            } catch (e: Exception) {
                Log.w(javaClass.simpleName, "HTTP request failed.")
                Log.d(javaClass.simpleName, "Error: $e")
            }
        }
    }
    // netPosts fetches the posts for the current subreddit, when that
    // changes
    private var netPosts = MediatorLiveData<List<RedditPost>>().apply {
        addSource(subreddit) { subreddit: String ->
            Log.d("repoPosts", subreddit)
            // XXX Write me, viewModelScope.launch getPosts
            viewModelScope.launch(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                try {
                    Log.d(javaClass.simpleName, "Calling netPosts with $subreddit...")
                    postValue(redditPostRepository.getPosts(subreddit))
                } catch (e: Exception) {
                    Log.d(javaClass.simpleName, "HTTP request failed. Try again!")
                    fetch429.postValue(true)
                    fetchDone.postValue(true)
                    postValue(emptyList())
                }
            }
        }
    }

    fun observeLivePosts(): LiveData<List<RedditPost>> {
        return searchPosts
    }

    fun observeLiveSubreddits(): LiveData<List<RedditPost>> {
        return searchSubreddits
    }

    fun setSubreddit(newSubreddit: String) {
        subreddit.value = newSubreddit
    }

    // XXX Write me MediatorLiveData searchSubreddit, searchFavorites
    // searchPosts

    // Looks pointless, but if LiveData is set up properly, it will fetch posts
    // from the network
    fun repoFetch() {
        val fetch = subreddit.value
        subreddit.value = fetch
    }

    fun observeTitle(): LiveData<String> {
        return title
    }
    fun setTitle(newTitle: String) {
        title.value = newTitle
    }
    
    fun observeSubreddit(): LiveData<String> {
        return subreddit
    }

    fun setSearchTerm(s: String) {
        searchTerm.value = s
    }

    /*
    fun setNetSubreddits() {
        viewModelScope.launch(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            try {
                val netSubredditsIsEmpty = netSubreddits.value?.isEmpty() ?: true
                if (netSubredditsIsEmpty) {
                    Log.d(javaClass.simpleName, "Calling netSubreddits...")
                    netSubreddits.postValue(redditPostRepository.getSubreddits())
                } else {
                    Log.d(javaClass.simpleName, "netSubreddits is not empty, so do not set")
                }
            } catch (e: Exception) {
                Log.w(javaClass.simpleName, "HTTP request failed.")
                Log.d(javaClass.simpleName, "Error: $e")
            }
        }
    }
     */

    private fun searchSubredditList(): List<RedditPost> {
        val searchTermValue = searchTerm.value ?: ""
        return netSubreddits.value?.filter {
            it.searchFor(searchTermValue)
        } ?: emptyList()
    }

    private var searchSubreddits = MediatorLiveData<List<RedditPost>>().apply{
        addSource(netSubreddits) { value = searchSubredditList() }
        addSource(searchTerm) { value = searchSubredditList() }
    }

    private fun searchPostList(): List<RedditPost> {
        val searchTermValue = searchTerm.value ?: ""
        return netPosts.value?.filter {
            it.searchFor(searchTermValue)
        } ?: emptyList()
    }

    private var searchPosts = MediatorLiveData<List<RedditPost>>().apply{
        addSource(netPosts) { value = searchPostList() }
        addSource(searchTerm) { value = searchPostList() }
    }

    private fun searchFavoriteList(): List<RedditPost> {
        val searchTermValue = searchTerm.value ?: ""
        return favoriteRedditPosts.value?.filter {
            it.searchFor(searchTermValue)
        } ?: emptyList()
        /*
        return favoriteRedditPosts.value?.values?.filter {
            it.searchFor(searchTermValue)
        } ?: emptyList()
         */
    }

    private var searchFavorites = MediatorLiveData<List<RedditPost>>().apply{
        addSource(favoriteRedditPosts) { value = searchFavoriteList() }
        addSource(searchTerm) { value = searchFavoriteList() }
    }

    // ONLY call this from OnePostFragment, otherwise you will have problems.
    fun observeSearchTerm(): LiveData<String> {
        return searchTerm
    }

    /////////////////////////
    // Action bar
    fun initActionBarBinding(it: ActionBarBinding) {
        // XXX Write me, one liner
        actionBarBinding = it
    }
    fun hideActionBarFavorites() {
        // XXX Write me, one liner
        actionBarBinding?.actionFavorite?.visibility = View.GONE
    }
    fun showActionBarFavorites() {
        // XXX Write me, one liner
        actionBarBinding?.actionFavorite?.visibility = View.VISIBLE
    }

    // XXX Write me, set, observe, deal with favorites
    /*
    fun getFavoriteRedditPosts(): Map<String, RedditPost>? {
        //Log.d(javaClass.simpleName, ">>> ${favoriteRedditPosts.value}")
        return favoriteRedditPosts.value
    }
     */

    fun isFavoriteRedditPost(post: RedditPost): Boolean? {
        return favoriteRedditPosts.value?.contains(post)
        //return favoriteRedditPosts.value?.containsKey(post.key)
    }

    fun observeLiveFavoriteRedditPosts(): LiveData<List<RedditPost>> {
        return searchFavorites
    }

    fun setFavoriteRedditPost(redditPost: RedditPost, isFavorite: Boolean) {
        if (isFavorite) {
            // Add favorite post
            favoriteRedditPosts.apply {
                this.value?.let {
                    val tempFavoriteRedditPosts = it.toMutableList()
                    tempFavoriteRedditPosts.add(redditPost)
                    //val tempFavoriteRedditPosts = it.toMutableMap()
                    //tempFavoriteRedditPosts[redditPost.key] = redditPost
                    this.value = tempFavoriteRedditPosts
                }
            }
        } else {
            // Remove favorite post
            favoriteRedditPosts.apply {
                this.value?.let {
                    val tempFavoriteRedditPosts = it.toMutableList()
                    tempFavoriteRedditPosts.remove(redditPost)
                    //val tempFavoriteRedditPosts = it.toMutableMap()
                    //tempFavoriteRedditPosts.remove(redditPost.key)
                    this.value = tempFavoriteRedditPosts
                }
            }
        }
    }
}