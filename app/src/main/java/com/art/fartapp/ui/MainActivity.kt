package com.art.fartapp.ui

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.art.fartapp.R
import com.art.fartapp.util.ConnectionLiveData
import com.art.fartapp.util.ConnectivityManager
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private var observed: Boolean = false

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    private lateinit var connectionLiveData: ConnectionLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this) {}
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.findNavController()

        setupActionBarWithNavController(navController)

        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this) {
            if (observed) {
                when (it) {
                    true -> {
                        Snackbar.make(
                            findViewById(R.id.nav_host_fragment),
                            "Your internet connection has been restored",
                            Snackbar.LENGTH_SHORT
                        ).also { snackbar ->
                            snackbar.view.setBackgroundColor(Color.parseColor("#34C859"))
                            snackbar.show()
                        }
                    }
                    false -> {
                        Snackbar.make(
                            findViewById(R.id.nav_host_fragment),
                            "No Internet Connection",
                            Snackbar.LENGTH_LONG
                        ).also { snackbar ->
                            snackbar.view.setBackgroundColor(Color.parseColor("#EE5260"))
                            snackbar.show()
                        }
                    }
                }
            }
            observed = true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        observed = false
        connectivityManager.registerConnectionObserver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterConnectionObserver(this)
        observed = false
    }
}

const val EDIT_FARTER_RESULT_OK = Activity.RESULT_FIRST_USER + 1
