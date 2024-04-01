package edu.cs371m.reddit.api

import android.text.SpannableString
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import edu.cs371m.reddit.MainActivity

class RedditPostRepository(private val redditApi: RedditApi) {
    // NB: This is for our testing.
    private val gson : Gson = GsonBuilder().registerTypeAdapter(
            SpannableString::class.java, RedditApi.SpannableDeserializer()
        ).create()

    private fun unpackPosts(response: RedditApi.ListingResponse): List<RedditPost> {
        // XXX Write me.
        return List(response.data.children.size) { index ->
            response.data.children[index].data
        }
    }

    suspend fun getPosts(subreddit: String): List<RedditPost> {
        val response : RedditApi.ListingResponse?
        if (MainActivity.globalDebug) {
            response = gson.fromJson(
                MainActivity.jsonAww100,
                RedditApi.ListingResponse::class.java)
        } else {
            // XXX Write me.
            response = redditApi.getPosts(subreddit)
        }
        return unpackPosts(response!!)
    }

    suspend fun getSubreddits(): List<RedditPost> {
        val response : RedditApi.ListingResponse?
        if (MainActivity.globalDebug) {
            response = gson.fromJson(
                MainActivity.subreddit1,
                RedditApi.ListingResponse::class.java)
        } else {
            // XXX Write me.
            response = redditApi.getSubreddits()
        }
        return unpackPosts(response!!)
    }
}
