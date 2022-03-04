package com.art.fartapp.ui

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.art.fartapp.R
import com.art.fartapp.dialogs.SendBackDialogFragmentDirections
import com.art.fartapp.ui.addeditfarter.MainViewModel
import com.art.fartapp.util.ConnectionLiveData
import com.art.fartapp.util.ConnectivityManager
import com.art.fartapp.util.NdefMessageParser
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "AddEditFarterFragment"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private var observed: Boolean = false

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    private lateinit var connectionLiveData: ConnectionLiveData
    private val viewModel by viewModels<MainViewModel>()

    private var mNfcAdapter: NfcAdapter? = null
    private var mPendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        observed = false
        MobileAds.initialize(this) {}
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.findNavController()

        setupActionBarWithNavController(navController)

        navigateToSendBackFragmentIfNeeded(intent)

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)

        mPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, this.javaClass)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE
        )

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

    override fun onResume() {
        super.onResume()
        mNfcAdapter?.enableForegroundDispatch(this, mPendingIntent, null, null)
    }


    override fun onPause() {
        super.onPause()
        mNfcAdapter?.disableForegroundDispatch(this)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navigateToSendBackFragmentIfNeeded(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action && navController.currentDestination?.id == R.id.addBottomSheet) {
            viewModel.intent.set(intent)
        }
    }

    private fun navigateToSendBackFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SEND_BACK_FRAGMENT) {
            navController.navigate(
                SendBackDialogFragmentDirections.actionGlobalSendBackDialogFragment(
                    intent.getStringExtra("token")!!,
                    intent.getStringExtra("rawRes")!!
                )
            )
        }
    }
}

const val EDIT_FARTER_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val ACTION_SEND_BACK_FRAGMENT = "ACTION_SEND_BACK_FRAGMENT"
