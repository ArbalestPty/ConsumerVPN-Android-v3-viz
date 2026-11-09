package com.wlvpn.consumervpn.presentation.home

import android.net.VpnService
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.wlvpn.consumervpn.R.dimen
import com.wlvpn.consumervpn.R.drawable
import com.wlvpn.consumervpn.R.string
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
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.LocalDimens
import com.wlvpn.consumervpn.presentation.util.Routes
import com.wlvpn.consumervpn.presentation.util.countryNameFromCode
import java.util.Locale

private const val POST_NOTIFICATIONS_PERMISSION = "android.permission.POST_NOTIFICATIONS"
private const val VPN_PERMISSION_GRANTED = -1

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    onBackPressed: () -> Unit,
    startConnection: Boolean,
) {
    val context = LocalContext.current
    val state by viewModel.homeEvent.observeAsState()
    val geoState by viewModel.geoLocationEvent.observeAsState()
    val selectedTargetState by viewModel.selectedTargetEvent.observeAsState()
    val failureEvents by viewModel.failureEvent.collectAsState()
    val notificationPermission = rememberPermissionState(
        permission = POST_NOTIFICATIONS_PERMISSION
    )
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

    when (failureEvents) {
        ExpiredWireGuardAccount -> ShowToast(string.home_screen_expired_account_label)
        InactiveWireGuardAccount -> ShowToast(string.home_screen_invalid_account_label)
        InvalidWireGuardApiResponse -> ShowToast(string.home_screen_invalid_api_response_label)
        NoNetworkError -> ShowToast(string.home_screen_no_network_error_label)
        UnableToConnect -> ShowToast(string.home_screen_unable_to_connect_label)
        UserNotLogged -> ShowToast(string.home_screen_user_not_logged_in_label)
        null -> {
            // no - op
        }
    }

    Scaffold { paddingValues ->
        when (state) {
            is Connected ->
                ConnectedContent(
                    modifier = Modifier.padding(paddingValues),
                    geoLocationEvent = geoState,
                    server = (state as Connected).server
                ) {
                    viewModel.disconnect()
                }

            Connecting ->
                ConnectingContent(
                    modifier = Modifier.padding(paddingValues),
                ) {
                    viewModel.disconnect()
                }

            is Disconnected ->
                DisconnectedContent(
                    modifier = Modifier.padding(paddingValues),
                    selectedTargetEvent = selectedTargetState,
                    onLocationClick = { navController.navigate(Routes.Locations.route) }
                ) {
                    requestConnection = true
                }

            is DisconnectedError, null -> {
                DisconnectedContent(
                    modifier = Modifier.padding(paddingValues),
                    selectedTargetEvent = selectedTargetState,
                    onLocationClick = { navController.navigate(Routes.Locations.route) }
                ) {
                    requestConnection = true
                }

                Toast.makeText(
                    LocalContext.current,
                    stringResource(string.home_screen_connection_error_label),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        && notificationPermission.status.isGranted.not()
    ) {
        LaunchedEffect(Unit) {
            notificationPermission.launchPermissionRequest()
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
inline fun ConnectedContent(
    modifier: Modifier = Modifier,
    geoLocationEvent: GeoLocationEvent?,
    server: ServerLocation.Server?,
    crossinline onDisconnectClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(id = dimen.spacing_xlarge),
                    end = dimensionResource(id = dimen.spacing_xlarge),
                    top = dimensionResource(id = dimen.spacing_small)
                )
        ) {
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = painterResource(id = drawable.bg_dotmap),
                contentDescription = null
            )
            Image(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(dimensionResource(id = dimen.home_connection_ic_connected_size)),
                painter = painterResource(id = drawable.ic_connected),
                contentDescription = null,
                colorFilter = ColorFilter.tint(LocalColors.current.extendedColors.accentIcon)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .wrapContentWidth()
        ) {
            InformativeRow(
                modifier = Modifier
                    .padding(dimensionResource(id = dimen.spacing_normal)),
                icon = drawable.ic_public,
                title = stringResource(string.home_screen_public_ip_label),
                subTitle = when (geoLocationEvent) {
                    is GeoLocationChanged -> geoLocationEvent.geoLocation.ip

                    else -> stringResource(string.home_screen_loading_ip_label)
                }
            )

            InformativeRow(
                modifier = Modifier
                    .padding(dimensionResource(id = dimen.spacing_normal)),
                icon = drawable.ic_location,
                title = stringResource(string.home_screen_visible_location_label),
                subTitle = server?.let { "${it.city.name}, ${it.city.country.name}" } ?: ""
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        DisconnectButton(
            modifier = Modifier
                .padding(
                    top = dimensionResource(id = dimen.spacing_normal),
                    bottom = dimensionResource(id = dimen.spacing_xxlarge),
                    start = dimensionResource(id = dimen.spacing_xxwide),
                    end = dimensionResource(id = dimen.spacing_xxwide)
                ),
            onClick = { onDisconnectClick() }
        )
    }
}

@Composable
inline fun DisconnectedContent(
    modifier: Modifier = Modifier,
    selectedTargetEvent: SelectedTargetEvent?,
    crossinline onLocationClick: () -> Unit,
    crossinline onConnectClick: () -> Unit
) {

    Box(modifier = modifier.fillMaxSize()) {

        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = dimensionResource(id = dimen.spacing_xxxxlarge)),
            text = stringResource(string.home_screen_location_selected_label),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .clickable {
                    onLocationClick()
                }
        ) {
            Image(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(end = dimensionResource(id = dimen.spacing_xsmall)),
                painter = painterResource(id = drawable.ic_location),
                contentDescription = null,
                colorFilter = ColorFilter.tint(LocalColors.current.extendedColors.accentIcon)
            )

            Text(
                modifier = Modifier.align(Alignment.Bottom),
                color = LocalColors.current.extendedColors.clickableTextColor,
                text = when (selectedTargetEvent) {
                    is SelectedTargetUpdated ->
                        when (val target = selectedTargetEvent.selectedTarget) {
                            is City ->
                                stringResource(
                                    string.home_screen_best_available_in_label,
                                    target.name
                                )

                            is Country ->
                                stringResource(
                                    string.home_screen_best_available_in_label,
                                    Locale.US.countryNameFromCode(target.code)
                                )

                            Fastest ->
                                stringResource(string.home_screen_best_available_label)

                            is Server ->
                                target.name
                        }

                    null -> "-"
                },
                textAlign = TextAlign.Center
            )
        }

        ConnectButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = dimensionResource(id = dimen.spacing_xlarge),
                    start = dimensionResource(id = dimen.spacing_xxwide),
                    end = dimensionResource(id = dimen.spacing_xxwide)
                ),
            onClick = {
                onConnectClick()
            }
        )
    }
}

@Composable
inline fun ConnectingContent(
    modifier: Modifier = Modifier,
    crossinline onDisconnectClick: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(
                    top = dimensionResource(id = dimen.home_connection_connection_box_padding_top)
                )
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(
                        dimensionResource(id = dimen.home_connection_progress_connection_size)
                    )
                    .align(Alignment.Center),
            )

            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = stringResource(string.home_screen_connecting_progress_label)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        DisconnectButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(
                    bottom = dimensionResource(id = dimen.spacing_xlarge),
                    start = dimensionResource(id = dimen.spacing_xxwide),
                    end = dimensionResource(id = dimen.spacing_xxwide)
                ),
            onClick = {
                onDisconnectClick()
            }
        )
    }
}

@Composable
fun InformativeRow(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    title: String,
    subTitle: String
) {
    Row(
        modifier = modifier.
            widthIn(
                min = dimensionResource(id = dimen.home_connection_informative_min_width),
                max = dimensionResource(id = dimen.home_connection_informative_max_width)
            )
    ) {
        Image(
            modifier = Modifier.align(Alignment.CenterVertically),
            painter = painterResource(id = icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(LocalColors.current.extendedColors.accentIcon)
        )
        Column(
            modifier = Modifier.padding(start = dimensionResource(id = dimen.spacing_normal))
        ) {
            Text(
                text = title
            )
            Text(
                text = subTitle
            )
        }
    }
}

@Composable
inline fun ConnectButton(
    modifier: Modifier = Modifier,
    crossinline onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .width(dimensionResource(id = dimen.home_connection_button_width)),
        shape = RoundedCornerShape(LocalDimens.current.xxSmall),
        colors = buttonColors(
            containerColor = LocalColors.current.scheme.primaryContainer,
            contentColor = LocalColors.current.scheme.secondary,
        ),
        onClick = { onClick() }) {
        Text(text = stringResource(string.home_screen_connect_button_label))
    }
}

@Composable
inline fun DisconnectButton(
    modifier: Modifier = Modifier,
    crossinline onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .width(dimensionResource(id = dimen.home_connection_button_width)),
        shape = RoundedCornerShape(LocalDimens.current.xxSmall),
        colors = ButtonDefaults.buttonColors(
            containerColor = LocalColors.current.extendedColors.disconnectedColor,
            contentColor = LocalColors.current.extendedColors.controlNormalColor
        ),
        onClick = { onClick() }) {
        Text(text = stringResource(string.home_screen_disconnect_button_label))
    }
}

@Composable
fun ShowToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(
        LocalContext.current,
        stringResource(resId),
        duration
    ).show()
}