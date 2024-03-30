package edu.cs371m.reddit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import edu.cs371m.reddit.databinding.ActionBarBinding
import edu.cs371m.reddit.databinding.ActivityMainBinding
import edu.cs371m.reddit.ui.HomeFragmentDirections
import edu.cs371m.reddit.ui.MainViewModel

class MainActivity : AppCompatActivity() {
    // This allows us to do better testing
    companion object {
        var globalDebug = false
        lateinit var jsonAww100: String
        lateinit var subreddit1: String
        val favoriteDrawable = R.drawable.ic_favorite_black_24dp
        val unfavoriteDrawable = R.drawable.ic_favorite_border_black_24dp
    }
    private var actionBarBinding: ActionBarBinding? = null
    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController : NavController

    // An Android nightmare
    // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
    // https://stackoverflow.com/questions/7789514/how-to-get-activitys-windowtoken-without-view
    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
    }

    // https://stackoverflow.com/questions/24838155/set-onclick-listener-on-action-bar-title-in-android/29823008#29823008
    private fun initActionBar(actionBar: ActionBar) {
        // Disable the default and enable the custom
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayShowCustomEnabled(true)
        actionBarBinding = ActionBarBinding.inflate(layoutInflater)
        // Apply the custom view
        actionBar.customView = actionBarBinding?.root
        viewModel.initActionBarBinding(actionBarBinding!!)
    }

    // https://nezspencer.medium.com/navigation-components-a-fix-for-navigation-action-cannot-be-found-in-the-current-destination-95b63e16152e
    // You get a NavDirections object from a Directions object like
    // HomeFragmentDirections.
    // safeNavigate checks if you are in the source fragment for the directions
    // object and if not does nothing
    private fun NavController.safeNavigate(direction: NavDirections) {
        currentDestination?.
        getAction(direction.actionId)?.
        run {
            navigate(direction)
        }
    }
    private fun actionBarTitleLaunchSubreddit() {
        // XXX Write me actionBarBinding, safeNavigate
        actionBarBinding?.let {
            it.actionTitle.setOnClickListener() {
                val direction = HomeFragmentDirections.actionHomeFragmentToSubreddits()
                navController.safeNavigate(direction)
            }
        }
    }
    private fun actionBarLaunchFavorites() {
        // XXX Write me actionBarBinding, safeNavigate
        actionBarBinding?.let {
            it.actionFavorite.setOnClickListener() {
                val direction = HomeFragmentDirections.actionHomeFragmentToFavorites()
                navController.safeNavigate(direction)
            }
        }
    }

    // XXX check out addTextChangedListener
    private fun actionBarSearch() {
        // XXX Write me
        actionBarBinding?.let { actionBarBinding ->
            actionBarBinding.actionSearch.addTextChangedListener(
                object: TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        // not used
                    }

                    override fun onTextChanged(
                        s: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (s.isEmpty()) hideKeyboard()
                        viewModel.setSearchTerm(s.toString())
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // not used
                    }
                }
            )
        }
    }

    private fun initDebug() {
        if(globalDebug) {
            assets.list("")?.forEach {
                Log.d(javaClass.simpleName, "Asset file: $it" )
            }
            jsonAww100 = assets.open("aww.hot.1.100.json.transformed.txt").bufferedReader().use {
                it.readText()
            }
            subreddit1 = assets.open("subreddits.1.json.txt").bufferedReader().use {
                it.readText()
            }
        }
    }
    private fun initTitleObservers() {
        // Observe title changes
        viewModel.setTitle("r/aww")
        viewModel.observeTitle().observe(this) {
            actionBarBinding?.let { actionBarBinding ->
                actionBarBinding.actionTitle.text = it
            }
        }

        actionBarBinding?.let {
            it.actionTitle.setOnClickListener() {
                actionBarTitleLaunchSubreddit()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.toolbar)
        supportActionBar?.let{
            initActionBar(it)
        }

        initDebug()
        initTitleObservers()
        actionBarTitleLaunchSubreddit()
        actionBarLaunchFavorites()
        actionBarSearch()

        viewModel.repoFetch()

        viewModel.fetch429.observe(this) {
            if (it) {
                Snackbar.make(
                    activityMainBinding.root,
                    "HTTP request failed. Try Again!",
                    Snackbar.LENGTH_SHORT).show()
                viewModel.fetch429.value = false
            }
        }

        // Set up our nav graph
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.main_frame) as NavHostFragment
        navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        // If we have a toolbar (not actionbar) we don't need to override
        // onSupportNavigateUp().
        activityMainBinding.toolbar.setupWithNavController(navController, appBarConfiguration)
        //setupActionBarWithNavController(navController, appBarConfiguration)
    }
}
