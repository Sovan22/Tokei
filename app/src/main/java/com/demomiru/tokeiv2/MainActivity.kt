

package com.demomiru.tokeiv2
import android.annotation.SuppressLint
import android.os.Bundle

import android.view.KeyEvent

import android.view.View
import android.widget.TextView
import androidx.activity.viewModels

import androidx.appcompat.app.AppCompatActivity


import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope

import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.utils.ContinueWatchingViewModel2
import com.demomiru.tokeiv2.utils.ContinueWatchingViewModelFactory2
import com.demomiru.tokeiv2.utils.addRecyclerAnimation
import com.demomiru.tokeiv2.utils.passData
import com.demomiru.tokeiv2.watching.ContinueWatching
import com.demomiru.tokeiv2.watching.ContinueWatchingAdapter
import com.demomiru.tokeiv2.watching.ContinueWatchingDatabase
import com.demomiru.tokeiv2.watching.ContinueWatchingRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var watchHistoryRc : RecyclerView
    private val database by lazy { ContinueWatchingDatabase.getInstance(this) }
    private val watchHistoryDao by lazy { database.watchDao() }
    private lateinit var viewModelFactory: ContinueWatchingViewModelFactory2
    private val viewModel: ContinueWatchingViewModel2 by viewModels(
        factoryProducer = {
            viewModelFactory
        }
    )
    private var currentFragment = R.id.moviesFragment
    private lateinit var continueText: TextView
//    private lateinit var continueWatchingObserver: Observer<List<ContinueWatching>>
    private var nestedScrollView : NestedScrollView? = null
    private lateinit var adapter: ContinueWatchingAdapter
    private lateinit var continueWatchingRepository: ContinueWatchingRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModelFactory = ContinueWatchingViewModelFactory2(watchHistoryDao)
        val options = NavOptions.Builder()
            .setEnterAnim(R.anim.enter_from_bottom)
            .setExitAnim(R.anim.exit_to_top)
            .build()

        nestedScrollView = findViewById(R.id.nestedScrollView)
        val navController = findNavController(R.id.nav_host_fragment)
        val bottomNavigationView : BottomNavigationView = findViewById(R.id.bottom_nav_bar)
        continueWatchingRepository = ContinueWatchingRepository(watchHistoryDao)
        watchHistoryRc = findViewById(R.id.watch_history_rc)
        watchHistoryRc.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        continueText = findViewById(R.id.continue_watching_text)

        adapter = ContinueWatchingAdapter{it,delete->
            if(delete){
                lifecycleScope.launch  (Dispatchers.IO) {
                    continueWatchingRepository.delete(it)
//                    continueWatchingRepository.loadData()
                }
            }
            else {
                startActivity(passData(it, this))
            }

        }


        watchHistoryRc.adapter = adapter

        //TODO Remove comment if continue watching not working
//        GlobalScope.launch  (Dispatchers.IO) {
//            continueWatchingRepository.loadData()
//        }



//        continueWatchingObserver = Observer{
//            if(it.isNotEmpty()){
//                watchHistoryRc.visibility = View.VISIBLE
//                continueText.visibility = View.VISIBLE
//                adapter.submitList(it)
//                addRecyclerAnimation(watchHistoryRc,adapter)
//            }
//            else{
//                watchHistoryRc.visibility = View.GONE
//                continueText.visibility = View.GONE
//            }
//        }

//        continueWatchingRepository.allWatchHistory.observe(this,continueWatchingObserver)


       bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.moviesFragment -> {
                    navController.navigate(R.id.moviesFragment, null, options)
                    if (viewModel.allWatchHistory.value?.size !=0){
                        watchHistoryRc.visibility = View.VISIBLE
                        continueText.visibility = View.VISIBLE
                        currentFragment = R.id.moviesFragment
                    }

                }
                R.id.searchFragment -> {
                    navController.navigate(R.id.searchFragment, null, options)
                    watchHistoryRc.visibility = View.GONE
                    continueText.visibility = View.GONE
                    currentFragment = R.id.searchFragment
                }
                R.id.TVShowFragment -> {
                    navController.navigate(R.id.TVShowFragment, null, options)
                    if (viewModel.allWatchHistory.value?.size !=0){
                        watchHistoryRc.visibility = View.VISIBLE
                        continueText.visibility = View.VISIBLE
                        currentFragment = R.id.TVShowFragment
                    }

                }
            }
            true
        }
        bottomNavigationView.setOnNavigationItemReselectedListener {
            return@setOnNavigationItemReselectedListener
        }



    }
    fun triggerSearchKeyPress() {
        val enterKeyEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
        dispatchKeyEvent(enterKeyEvent)
    }


    @SuppressLint("SetTextI18n")
    override fun onResume() {

        val viewStateObserver = Observer<List<ContinueWatching>> {watchFrom ->
            if(watchFrom.isNotEmpty()){
                watchHistoryRc.visibility = View.VISIBLE
                continueText.visibility = View.VISIBLE
                adapter.submitList(watchFrom)
                addRecyclerAnimation(watchHistoryRc,adapter)
                if(currentFragment == R.id.searchFragment){
                    watchHistoryRc.visibility = View.GONE
                    continueText.visibility = View.GONE
                }else
                {
                    watchHistoryRc.visibility = View.VISIBLE
                    continueText.visibility = View.VISIBLE
                }
            }
            else{
                watchHistoryRc.visibility = View.GONE
                continueText.visibility = View.GONE
            }

        }
        viewModel.allWatchHistory.observe(this,viewStateObserver)

        super.onResume()
    }

}