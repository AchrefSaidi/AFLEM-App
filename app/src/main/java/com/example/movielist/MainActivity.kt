package com.example.movielist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.grey_500)

        mainMovieLoad()
        imageSlider()
        refreshLayout.setOnRefreshListener {
            mainMovieLoad()
            imageSlider()
            refreshLayout.isRefreshing = false
        }

        // On genere le guest session id
        fetchGuestSessionId()

        cl_trending_movies.setOnClickListener {
            val intent = Intent(this, MoreDetailsActivity::class.java)
            startActivity(intent)
        }

        cl_popular_movies.setOnClickListener {
            val intent = Intent(this, MorePopularActivity::class.java)
            startActivity(intent)
        }

        cl_now_playing_movies.setOnClickListener {
            val intent = Intent(this, NowPlayingActivity::class.java)
            startActivity(intent)
        }

        cl_top_rated_movies.setOnClickListener {
            val intent = Intent(this, MoreTopRatedActivity::class.java)
            startActivity(intent)
        }

    }

    private fun fetchGuestSessionId() {

        MovieService.movieInstance.getGuestSessionID().enqueue(object :
            Callback<GuestSessionResponse> {
            override fun onResponse(call: Call<GuestSessionResponse>, response: Response<GuestSessionResponse>) {
                if (response.isSuccessful) {
                    val sessionId = response.body()?.guest_session_id
                    sessionId?.let {
                        SessionManager.saveSessionId(applicationContext, it)
                        Toast.makeText(applicationContext, "Session started", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    // Handle API error here
                    Toast.makeText(applicationContext, "Failed to fetch session ID", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GuestSessionResponse>, t: Throwable) {
                // Handle network errors here
                Toast.makeText(applicationContext, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mainMovieLoad() {
        val viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        if (!NetworkUtils.isNetworkAvailable(this)) {
            // Handle no internet connection error
            Toast.makeText(applicationContext,"No internet connection. Please check your network settings.",Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.adapter = MovieAdapter(this, viewModel.resultsTrending)
        recycler_view_trending_movies.adapter = viewModel.adapter
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler_view_trending_movies.layoutManager = layoutManager
        viewModel.getTrendingMovies()

        viewModel.adapter2 = MovieAdapter(this, viewModel.resultsPopular)
        recycler_view_popular_movies.adapter = viewModel.adapter2
        val layoutManager2 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler_view_popular_movies.layoutManager = layoutManager2
        viewModel.getPopularMovies()

        viewModel.adapter3 = MovieAdapter(this, viewModel.resultsNowPlaying)
        recycler_view_now_playing_movies.adapter = viewModel.adapter3
        val layoutManager3 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler_view_now_playing_movies.layoutManager = layoutManager3
        viewModel.getNowPlayingMovies()

        viewModel.adapter4 = MovieAdapter(this, viewModel.resultsTopRated)
        recycler_view_top_rated_movies.adapter = viewModel.adapter4
        val layoutManager4 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler_view_top_rated_movies.layoutManager = layoutManager4
        viewModel.getTopRatedMovies()
    }

    private fun imageSlider() {
        val viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        val imageList: ArrayList<Int> = ArrayList()
        imageList.add(R.drawable.image3)
        imageList.add(R.drawable.image2)
        imageList.add(R.drawable.image1)
        viewModel.setImageInSlider(imageList, imageSlider,this)
    }
}


