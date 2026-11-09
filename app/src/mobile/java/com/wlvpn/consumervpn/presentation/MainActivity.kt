package com.wlvpn.consumervpn.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.R.string
import com.wlvpn.consumervpn.presentation.MainScreenEvent.Loading
import com.wlvpn.consumervpn.presentation.MainScreenEvent.MigrationError
import com.wlvpn.consumervpn.presentation.MainScreenEvent.UserLoggedIn
import com.wlvpn.consumervpn.presentation.MainScreenEvent.UserNotLoggedIn
import com.wlvpn.consumervpn.presentation.navigation.AppNavigation
import com.wlvpn.consumervpn.presentation.navigation.BottomNavigationBar
import com.wlvpn.consumervpn.presentation.ui.theme.AppTheme
import com.wlvpn.consumervpn.presentation.util.BottomNavItem
import com.wlvpn.consumervpn.presentation.util.Routes
import dagger.hilt.android.AndroidEntryPoint

private const val SPLASH_DELAY = 1200L

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private var keepSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        setupSplashScreen(splashScreen = splashScreen)

        super.onCreate(savedInstanceState)

        setUpNotificationChannel()

        setContent {
            val state by viewModel.mainViewEvent.collectAsState(MainScreenEvent.Loading)

            AppTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        BottomNavigationBar(
                            navController = navController,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                            onItemClick = {
                                navController.navigate(it.route) {
                                    popUpTo(Routes.Home.route) {
                                    }
                                    launchSingleTop = true
                                }
                            },
                            items = listOf(
                                BottomNavItem(
                                    name = getString(string.bottom_navigation_item_home),
                                    route = Routes.Home.route,
                                    icon = Icons.Default.Home
                                ),
                                BottomNavItem(
                                    name = getString(string.bottom_navigation_item_locations),
                                    route = Routes.Locations.route,
                                    icon = Icons.Default.LocationOn
                                ),
                                BottomNavItem(
                                    name = getString(string.bottom_navigation_item_settings),
                                    route = Routes.Settings.route,
                                    icon = Icons.Default.Settings
                                ),

                                )
                        )
                    }
                ) {

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
                                modifier = Modifier.padding(bottom = it.calculateBottomPadding()),
                                navController = navController,
                                startDestination = Routes.Login.route,
                                closeApp = { this.finish() },
                                onForgotPassword = {
                                    openWebViewWithUrl(getString(string.url_forgot_password)) {
                                        showNoLinksSupportedError()
                                    }
                                },
                                onSignUp = {
                                    openWebViewWithUrl(getString(string.url_sign_up)) {
                                        showNoLinksSupportedError()
                                    }
                                }
                            )
                        }

                        UserLoggedIn -> AppNavigation(
                            modifier = Modifier.padding(bottom = it.calculateBottomPadding()),
                            navController = navController,
                            startDestination = Routes.Home.route,
                            closeApp = { this.finish() },
                            onForgotPassword = {
                                openWebViewWithUrl(getString(string.url_forgot_password)) {
                                    showNoLinksSupportedError()
                                }
                            },
                            onSignUp = {
                                openWebViewWithUrl(getString(string.url_sign_up)) {
                                    showNoLinksSupportedError()
                                }
                            }
                        )

                        UserNotLoggedIn -> AppNavigation(
                            modifier = Modifier.padding(bottom = it.calculateBottomPadding()),
                            navController = navController,
                            startDestination = Routes.Login.route,
                            closeApp = { this.finish() },
                            onForgotPassword = {
                                openWebViewWithUrl(getString(string.url_forgot_password)) {
                                    showNoLinksSupportedError()
                                }
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

    private fun setupSplashScreen(splashScreen: SplashScreen) {
        // Replace this timer with your logic to load data on the splash screen.
        splashScreen.setKeepOnScreenCondition { keepSplash }
        Handler(Looper.getMainLooper()).postDelayed({
            keepSplash = false
        }, SPLASH_DELAY)
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