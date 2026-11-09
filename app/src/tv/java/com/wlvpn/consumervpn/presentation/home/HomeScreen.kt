package com.wlvpn.consumervpn.presentation.home

import android.net.VpnService
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonColors
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.domain.value.ConnectionTarget.City
import com.wlvpn.consumervpn.domain.value.ConnectionTarget.Country
import com.wlvpn.consumervpn.domain.value.ConnectionTarget.Fastest
import com.wlvpn.consumervpn.domain.value.ConnectionTarget.Server
import com.wlvpn.consumervpn.domain.value.ServerLocation
import com.wlvpn.consumervpn.presentation.home.FailureEvent.ExpiredWireGuardAccount
import com.wlvpn.consumervpn.presentation.home.FailureEvent.InactiveWireGuardAccount
import com.wlvpn.consumervpn.presentation.home.FailureEvent.InvalidWireGuardApiResponse
import com.wlvpn.consumervpn.presentation.home.FailureEvent.NoNetworkError
import com.wlvpn.consumervpn.presentation.home.FailureEvent.UnableToConnect
import com.wlvpn.consumervpn.presentation.home.FailureEvent.UserNotLogged
import com.wlvpn.consumervpn.presentation.home.GeoLocationEvent.GeoLocationChanged
import com.wlvpn.consumervpn.presentation.home.HomeEvent.Connected
import com.wlvpn.consumervpn.presentation.home.HomeEvent.Connecting
import com.wlvpn.consumervpn.presentation.home.HomeEvent.Disconnected
import com.wlvpn.consumervpn.presentation.home.HomeEvent.DisconnectedError
import com.wlvpn.consumervpn.presentation.home.SelectedTargetEvent.SelectedTargetUpdated
import com.wlvpn.consumervpn.presentation.settings.SettingsScreen
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.LocalDimens
import com.wlvpn.consumervpn.presentation.ui.theme.extended
import com.wlvpn.consumervpn.presentation.util.countryNameFromCode
import java.util.Locale

private const val POST_NOTIFICATIONS_PERMISSION = "android.permission.POST_NOTIFICATIONS"
private const val VPN_PERMISSION_GRANTED = -1

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    startConnection: Boolean,
    onBackPressed: () -> Unit,
    onLocationsClicked: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.homeEvent.observeAsState()
    val failureState by viewModel.failureEvent.collectAsState()
    val geoState by viewModel.geoLocationEvent.observeAsState()
    val selectedTargetState by viewModel.selectedTargetEvent.observeAsState()
    val notificationPermission = rememberPermissionState(
        permission = POST_NOTIFICATIONS_PERMISSION
    )
    val homeRequester = FocusRequester()
    var showSettings by remember { mutableStateOf(false) }
    var requestConnection by remember { mutableStateOf(startConnection) }

    val requestVpnPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            if(it.resultCode == VPN_PERMISSION_GRANTED){
                viewModel.connect()
            }
        }
    )

    // Reload state when starting with a connection request so the new
    // selected location is updated in the UI
    LaunchedEffect(startConnection) {
        viewModel.loadState()
    }

    /* To handle on back pressed manually */
    BackHandler(true) {
        onBackPressed()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(homeRequester)
            // Disable home screen focus when settings drawer is showing
            .focusProperties { canFocus = showSettings.not() }
            .background(LocalColors.current.scheme.background)
    ) {

        if (state is Connected) {
            Image(
                modifier = Modifier
                    .padding(top = LocalDimens.current.extended.homeScreenConnectLogoPadding)
                    .align(Alignment.TopCenter)
                    .size(LocalDimens.current.extended.homeScreenConnectedLogoSize),
                painter = painterResource(id = R.drawable.ic_connected),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(LocalColors.current.extendedColors.accentIcon)
            )

            VisibleLocationRow(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = LocalDimens.current.extended.homeScreenLocationPadding),
                server = (state as Connected).server,
            )
        }

        LocationSelectedRow(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = LocalDimens.current.extended.homeScreenLocationPadding),
            location = selectedTargetState,
            visibility = state is Disconnected || state is DisconnectedError
        )

        if (state is Connecting) {
            ConnectingRow(
                modifier = Modifier
                    .padding(
                        top =
                        LocalDimens.current.extended.homeScreenConnectingContainerPadding
                    )
                    .align(Alignment.TopCenter)
            )
        }

        ConnectivityButton(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(
                    top = LocalDimens.current.extended.homeScreenConnectivityButtonPadding
                ),
            colors = when (state) {
                is Connected, Connecting -> ButtonDefaults.disconnectColors()
                else -> ButtonDefaults.connectColors()
            },
            text = when (state) {
                is Connected, Connecting ->
                    stringResource(R.string.home_screen_disconnect_button_label)

                else ->
                    stringResource(R.string.home_screen_connect_button_label)
            }
        ) {
            when (state) {
                is Connected, Connecting -> viewModel.disconnect()
                else -> requestConnection = true
            }
        }

        IpRow(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = LocalDimens.current.extended.homeScreenIpRowPadding),
            geoLocationEvent = geoState,
            visibility = state is Connected
        )

        MenuRow(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = LocalDimens.current.extended.homeScreenMenuRowPadding),
            onLocationsClick = { onLocationsClicked () },
            onSettingsClick = {
                showSettings = !showSettings
            },
            isEnabled = !(state is Connected || state is Connecting)
        )

    }

    SettingsScreen(
        modifier = Modifier
            .fillMaxSize(),
        visibility = showSettings,
        onBackPressed = { showSettings = false }
    )

    when (failureState) {
        ExpiredWireGuardAccount -> ShowToast(R.string.home_screen_expired_account_label)
        InactiveWireGuardAccount -> ShowToast(R.string.home_screen_invalid_account_label)
        InvalidWireGuardApiResponse -> ShowToast(R.string.home_screen_invalid_api_response_label)
        NoNetworkError -> ShowToast(R.string.home_screen_no_network_error_label)
        UnableToConnect -> ShowToast(R.string.home_screen_unable_to_connect_label)
        UserNotLogged -> ShowToast(R.string.home_screen_user_not_logged_in_label)
        null -> {
            // no - op
        }
    }

    if (state == DisconnectedError) {
        Toast.makeText(
            LocalContext.current,
            stringResource(R.string.home_screen_connection_error_label),
            Toast.LENGTH_LONG
        ).show()
    }

    // Side effects
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        && notificationPermission.status.isGranted.not()
    ) {
        LaunchedEffect(Unit) {
            notificationPermission.launchPermissionRequest()
        }
    }

    if (showSettings.not()) {
        LaunchedEffect(Unit) {
            homeRequester.requestFocus()
        }
    }

    if (requestConnection) {
        LaunchedEffect(Unit) {
            VpnService.prepare(context)?.let {
                requestVpnPermissionsLauncher.launch(it)
            } ?: run {
                viewModel.connect()
            }
        }
        requestConnection = false
    }
}

@Composable
fun ConnectingRow(
    modifier: Modifier
) {
    Box(
        modifier = modifier
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(LocalDimens.current.extended.homeScreenProgressIndicatorSize)
                .align(Alignment.Center),
            color = LocalColors.current.extendedColors.connectingProgressIndicatorColor
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(LocalDimens.current.extended.homeScreenProgressStrokeSize)
                .border(
                    width = LocalDimens.current.extended.homeScreenProgressStrokeWidth,
                    color = LocalColors.current.extendedColors.connectingProgressIndicatorColor,
                    shape = CircleShape
                ),

            ) {}

        Text(
            modifier = Modifier
                .align(Alignment.Center),
            text = stringResource(R.string.home_screen_connecting_progress_label),
            color = LocalColors.current.extendedColors.connectingProgressIndicatorColor
        )
    }
}

@Composable
fun IpRow(
    modifier: Modifier,
    geoLocationEvent: GeoLocationEvent?,
    visibility: Boolean
) {
    if (!visibility)
        return
    Row(
        modifier = modifier
    ) {
        Spacer(Modifier.weight(2f))
        Text(
            text = stringResource(R.string.home_screen_public_ip_label),
            color = LocalColors.current.extendedColors.ipRowLabelColor,
            fontSize = LocalDimens.current.extended.homeScreenIpLabelFontSize
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = when (geoLocationEvent) {
                is GeoLocationChanged -> geoLocationEvent.geoLocation.ip

                else -> stringResource(R.string.home_screen_loading_ip_label)
            },
            color = LocalColors.current.extendedColors.ipRowIpColor,
            fontSize = LocalDimens.current.extended.homeScreenIpFontSize
        )
        Spacer(Modifier.weight(2f))
    }
}

@Composable
fun VisibleLocationRow(
    modifier: Modifier,
    server: ServerLocation.Server?,
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            text = stringResource(R.string.home_screen_visible_location_label),
            textAlign = TextAlign.Center,
            color = LocalColors.current.extendedColors.locationRowLabelColor
        )
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = LocalDimens.current.small)
        ) {
            Image(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = LocalDimens.current.xSmall),
                painter = painterResource(id = R.drawable.ic_location),
                contentDescription = null,
                colorFilter = ColorFilter.tint(LocalColors.current.extendedColors.accentIcon)
            )

            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = server?.let { "${it.city.name}, ${it.city.country.name}" } ?: "",
                textAlign = TextAlign.Start,
                fontSize = LocalDimens.current.extended.homeScreenLocationFontSize,
                color = LocalColors.current.extendedColors.locationRowLocationColor
            )
        }
    }
}

@Composable
fun LocationSelectedRow(
    modifier: Modifier,
    location: SelectedTargetEvent?,
    visibility: Boolean
) {
    if (!visibility)
        return
    Column(modifier = modifier) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            text = stringResource(R.string.home_screen_location_selected_label),
            textAlign = TextAlign.Center,
            color = LocalColors.current.extendedColors.locationRowLabelColor
        )
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = LocalDimens.current.small)
        ) {
            Image(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = LocalDimens.current.xSmall),
                painter = painterResource(id = R.drawable.ic_location),
                contentDescription = null,
                colorFilter = ColorFilter.tint(LocalColors.current.extendedColors.accentIcon)
            )

            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = when (location) {
                    is SelectedTargetUpdated ->
                        when (val target = location.selectedTarget) {
                            is City ->
                                stringResource(
                                    R.string.home_screen_best_available_in_label,
                                    target.name
                                )

                            is Country ->
                                stringResource(
                                    R.string.home_screen_best_available_in_label,
                                    Locale.US.countryNameFromCode(target.code)
                                )

                            Fastest ->
                                stringResource(R.string.home_screen_best_available_label)

                            is Server ->
                                target.name
                        }

                    null -> "-"
                },
                textAlign = TextAlign.Start,
                fontSize = LocalDimens.current.extended.homeScreenLocationFontSize,
                color = LocalColors.current.extendedColors.locationRowLocationColor
            )
        }
    }
}

@Composable
fun MenuRow(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    onSettingsClick: () -> Unit,
    onLocationsClick: () -> Unit
) {
    Row(
        modifier = modifier
    ) {
        Spacer(Modifier.weight(1f))
        MenuButton(
            isEnabled = isEnabled,
            modifier = Modifier
                .size(LocalDimens.current.extended.homeScreenMenuButtonSize)
                .align(Alignment.CenterVertically),
            icon = R.drawable.ic_tv_locations
        ) {
            onLocationsClick()
        }
        Spacer(Modifier.weight(2f))
        MenuButton(
            isEnabled = isEnabled,
            modifier = Modifier
                .size(LocalDimens.current.extended.homeScreenMenuButtonSize)
                .align(Alignment.CenterVertically),
            icon = R.drawable.ic_tv_settings
        ) {
            onSettingsClick()
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun ConnectivityButton(
    modifier: Modifier = Modifier,
    colors: ButtonColors,
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.width(LocalDimens.current.extended.homeScreenConnectivityButtonSize),
        shape = ButtonDefaults.shape(
            shape = RoundedCornerShape(LocalDimens.current.xxSmall)
        ),
        colors = colors,
        onClick = { onClick() }) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = text,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ButtonDefaults.connectColors(): ButtonColors = colors(
    containerColor = LocalColors.current.extendedColors.connectButtonColor,
    contentColor = LocalColors.current.scheme.secondary,
    focusedContainerColor = LocalColors.current.extendedColors.connectHighlightedButtonColor,
    focusedContentColor = LocalColors.current.scheme.scrim,
    pressedContainerColor = LocalColors.current.extendedColors.connectSelectedButtonColor,
    pressedContentColor = LocalColors.current.scheme.scrim,
)

@Composable
fun ButtonDefaults.disconnectColors(): ButtonColors = colors(
    containerColor = LocalColors.current.extendedColors.disconnectButtonColor,
    contentColor = LocalColors.current.scheme.secondary,
    focusedContainerColor = LocalColors.current.extendedColors.disconnectHighlightedButtonColor,
    focusedContentColor = LocalColors.current.scheme.secondary,
    pressedContainerColor = LocalColors.current.extendedColors.disconnectSelectedButtonColor,
    pressedContentColor = LocalColors.current.scheme.secondary,
)

@Composable
fun ShowToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(
        LocalContext.current,
        stringResource(resId),
        duration
    ).show()
}