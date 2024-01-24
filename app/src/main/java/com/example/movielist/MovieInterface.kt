package com.example.movielist

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query

const val BASE_URL = "https://api.themoviedb.org/"

const val API_KEY = BuildConfig.MOVIE_API_KEY

interface MovieInterface {
    @POST("/3/movie/{movie_id}/rating?api_key=$API_KEY")
    fun rate_movie(
        @Path("movie_id") movieId: String,
        @Query("guest_session_id") guestSessionId: String,
        @Body rating: Rating
    ): Call<RatingResponse>

    @GET("3/authentication/guest_session/new?api_key=$API_KEY")
    fun getGuestSessionID(): Call<GuestSessionResponse>
    @GET("3/trending/all/day?api_key=$API_KEY&language=ar-SA")
    fun getTrendingMovies(@Query("page") page: Int): Call<Movies>

    @GET("3/movie/popular?api_key=$API_KEY&language=ar-SA")
    fun getPopularMovies(@Query("page") page: Int): Call<Movies>

    @GET("3/movie/now_playing?api_key=$API_KEY&language=ar-SA")
    fun getNowPlayingMovies(@Query("page") page: Int): Call<Movies>

    @GET("3/movie/top_rated?api_key=$API_KEY&language=ar-SA")
    fun getTopRatedMovies(@Query("page") page: Int): Call<Movies>

}

object MovieService {
    val movieInstance: MovieInterface

    init {
        val retrofit =
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        movieInstance = retrofit.create(MovieInterface::class.java)
    }
}