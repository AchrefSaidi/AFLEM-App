package com.example.movielist

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.RatingBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_single_movie_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class SingleMovieDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_movie_details)
        window.statusBarColor = ContextCompat.getColor(this, R.color.grey_700)
        iv_back_arrow_single_movie.setOnClickListener { finish() }

        val singleMoviePoster = intent.getStringExtra("movie_poster")
        val singleMovieTitle = intent.getStringExtra("movie_title")
        val singleMovieReleaseDate = intent.getStringExtra("movie_release_date")
        val singleMovieRating = intent.getStringExtra("movie_rating")
        val singleMovieOverview = intent.getStringExtra("movie_overview")
        // val movie_id = intent.getStringExtra("id")

        Glide.with(this).load(singleMoviePoster).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                progress_bar_single_movie.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                progress_bar_single_movie.visibility = View.GONE
                return false
            }
        }).into(iv_single_movie_poster)


        tv_single_movie_title.text = singleMovieTitle
        tv_release_date_single.text = singleMovieReleaseDate
        tv_rating_single.text = singleMovieRating
        tv_overview_single.text = singleMovieOverview

        // rating of the film is handled here

        val movieId = intent.getStringExtra("id")

        val guestSessionId = SessionManager.getSessionId(this)

        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            if (movieId != null && guestSessionId != null) {
                submitRating(movieId, guestSessionId, rating)
            } else {
                Toast.makeText(this, "Error: Movie ID or Session ID is missing", Toast.LENGTH_SHORT).show()
            }
        }


    }
    private fun submitRating(movieId: String, guestSessionId: String, rating: Float) {
// Convert the rating to a scale of 10 as required by the TMDb API
        val scaledRating = rating * 2

        // Create the Rating object
        val ratingRequest = Rating(scaledRating.toDouble())

        // Use the existing movieInstance to make the API call
        val call = MovieService.movieInstance.rate_movie(movieId, guestSessionId, ratingRequest)

        call.enqueue(object : Callback<RatingResponse> {
            override fun onResponse(call: Call<RatingResponse>, response: Response<RatingResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@SingleMovieDetailsActivity, "Rating submitted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle API error response
                    Toast.makeText(this@SingleMovieDetailsActivity, "Failed to submit rating", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RatingResponse>, t: Throwable) {
                // Handle network error
                Toast.makeText(this@SingleMovieDetailsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })}


    @TargetApi(Build.VERSION_CODES.M)
    fun askPermissions() {
        val singleMoviePoster = intent.getStringExtra("movie_poster")

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Permission required")
                    .setMessage("Permission required to save photos from the Web.")
                    .setPositiveButton("Allow") { dialog, id ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                        )
                        finish()
                    }
                    .setNegativeButton("Deny") { dialog, id -> dialog.cancel() }
                    .show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                )
                // MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.

            }
        } else {
            // Permission has already been granted
            if (singleMoviePoster != null) {
                downloadImage(singleMoviePoster)
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                val singleMoviePoster = intent.getStringExtra("movie_poster")
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                    // Download the Image
                    if (singleMoviePoster != null) {
                        downloadImage(singleMoviePoster)
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }


    private var msg: String? = ""
    private var lastMsg = ""

    @SuppressLint("Range")
    private fun downloadImage(url: String) {
        val directory = File(Environment.DIRECTORY_PICTURES)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val downloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadUri = Uri.parse(url)

        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(url.substring(url.lastIndexOf("/") + 1))
                .setDescription("")
                .setDestinationInExternalPublicDir(
                    directory.toString(),
                    url.substring(url.lastIndexOf("/") + 1)
                )
        }

        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)
        Thread(Runnable {
            var downloading = true
            while (downloading) {
                val cursor: Cursor = downloadManager.query(query)
                cursor.moveToFirst()
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                msg = statusMessage(url, directory, status)
                if (msg != lastMsg) {
                    this.runOnUiThread {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                    lastMsg = msg ?: ""
                }
                cursor.close()
            }
        }).start()
    }

    private fun statusMessage(url: String, directory: File, status: Int): String? {
        var msg = ""
        msg = when (status) {
            DownloadManager.STATUS_FAILED -> "Download has been failed, please try again"
            DownloadManager.STATUS_PAUSED -> "Paused"
            DownloadManager.STATUS_PENDING -> "Pending"
            DownloadManager.STATUS_RUNNING -> "Downloading..."
            DownloadManager.STATUS_SUCCESSFUL -> "Image downloaded successfully in $directory" + File.separator + url.substring(
                url.lastIndexOf("/") + 1
            )
            else -> "There's nothing to download"
        }
        return msg
    }


    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
    }


}

