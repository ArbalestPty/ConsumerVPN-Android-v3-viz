package com.wlvpn.consumervpn.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.R.string
import com.wlvpn.consumervpn.presentation.MainScreenEvent.Loading
import com.wlvpn.consumervpn.presentation.MainScreenEvent.MigrationError
import com.wlvpn.consumervpn.presentation.MainScreenEvent.UserLoggedIn
import com.wlvpn.consumervpn.presentation.MainScreenEvent.UserNotLoggedIn
import com.wlvpn.consumervpn.presentation.navigation.AppNavigation
import com.wlvpn.consumervpn.presentation.ui.theme.AppTheme
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.LocalDimens
import com.wlvpn.consumervpn.presentation.ui.theme.extended
import com.wlvpn.consumervpn.presentation.util.Routes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

private const val SPLASH_DELAY = 1200L

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setUpNotificationChannel()

        setContent {
            var showSplash by remember { mutableStateOf(true) }

            AppTheme {
                // Android's SplashScreen doesn't work with TV, we use a custom splash instead
                if (showSplash) {
                    SplashScreen(Modifier.fillMaxSize())

                    LaunchedEffect(Unit) {
                        delay(SPLASH_DELAY)
                        showSplash = false
                    }
                } else {
                    val state by viewModel.mainViewEvent.collectAsState(MainScreenEvent.Loading)
                    val navController = rememberNavController()

                    /* Only display the start destination when the splash screen defines it*/
                    when (state) {
                        Loading -> LoadingView()

                        MigrationError -> {
                            Toast.makeText(
                                this,
                                R.string.splash_label_migration_error,
                                Toast.LENGTH_LONG
                            ).show()

                            AppNavigation(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background),
                                navController = navController,
                                startDestination = Routes.Login.route,
                                closeApp = { this.finish() },
                                onForgotPassword = {
                                    navController.navigate(Routes.ForgotPassword.route)
                                },
                                onSignUp = {
                                    openWebViewWithUrl(getString(string.url_sign_up)) {
                                        showNoLinksSupportedError()
                                    }
                                }
                            )
                        }

                        UserLoggedIn -> AppNavigation(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            navController = navController,
                            startDestination = Routes.Home.route,
                            closeApp = { this.finish() },
                            onForgotPassword = {
                                navController.navigate(Routes.ForgotPassword.route)
                            },
                            onSignUp = {
                                openWebViewWithUrl(getString(string.url_sign_up)) {
                                    showNoLinksSupportedError()
                                }
                            }
                        )

                        UserNotLoggedIn -> AppNavigation(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            navController = navController,
                            startDestination = Routes.Login.route,
                            closeApp = { this.finish() },
                            onForgotPassword = {
                                navController.navigate(Routes.ForgotPassword.route)
                            },
                            onSignUp = {
                                openWebViewWithUrl(getString(string.url_sign_up)) {
                                    showNoLinksSupportedError()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun SplashScreen(modifier: Modifier) {
        Box(modifier = modifier.background(LocalColors.current.scheme.background)) {
            Image(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(LocalDimens.current.extended.splashScreenLogoSize),
                painter = painterResource(R.drawable.ic_splash_logo),
                contentDescription = null
            )
        }
    }

    @Composable
    fun LoadingView() {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    private fun setUpNotificationChannel() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val notificationManager =
                application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(
                NotificationChannel(
                    resources.getString(string.notification_channel_id),
                    getString(string.notification_channel_label_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = getString(R.string.notification_channel_label_description)
                    enableLights(false)
                    setShowBadge(false)
                }
            )
        }
    }

    private fun openWebViewWithUrl(url: String, onWebViewNotSupported: () -> Unit) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) })
        } catch (e: Exception) {
            onWebViewNotSupported()
        }
    }

    private fun showNoLinksSupportedError() {
        Toast.makeText(
            this,
            getString(string.error_links_not_supported),
            Toast.LENGTH_SHORT
        ).show()
    }
}