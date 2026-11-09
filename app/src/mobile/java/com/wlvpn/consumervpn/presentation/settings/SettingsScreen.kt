package com.wlvpn.consumervpn.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.R.string
import com.wlvpn.consumervpn.domain.value.settings.InternetProtocol
import com.wlvpn.consumervpn.domain.value.settings.OpenVpnPort
import com.wlvpn.consumervpn.domain.value.settings.Protocol
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings.IKEv2
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings.OpenVpn
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings.Wireguard
import com.wlvpn.consumervpn.domain.value.settings.StartupConnectOption
import com.wlvpn.consumervpn.presentation.MainActivity
import com.wlvpn.consumervpn.presentation.dialog.DialogWithRadioButtons
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors

private const val DEVICE_VPN_SETTINGS = "android.net.vpn.SETTINGS"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackPressed: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSplitTunneling: () -> Unit,
    onNavigateDiagnostics: () -> Unit
) {
    /* To handle on back pressed manually */
    BackHandler(true) {
        onBackPressed()
    }

    val settingsEvent by viewModel.settingsEvent.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { /* No op. */ }
    )

    when (val screenSettingsEvent = settingsEvent) {
        SettingsEvent.ErrorPreparingThreatProtection -> {
            Text(text = stringResource(string.settings_error_preparing_threat_protection_label))
        }

        SettingsEvent.ErrorGettingSettings -> {
            Text(text = "Error")
        }

        SettingsEvent.LoadingDataEvent -> {
            Text(text = "Loading")
        }

        // Go to Login screen anyway logout session fails
        SettingsEvent.ExpiredAccessToken -> {
            navigateToLogin(context = context)
        }

        SettingsEvent.InvalidAccessToken -> {
            navigateToLogin(context = context)
        }

        SettingsEvent.SuccessLogout -> {
            navigateToLogin(context = context)
        }

        SettingsEvent.UnableToLogout -> {
            navigateToLogin(context = context)
        }

        is SettingsEvent.SettingsReceived -> {

            var showVpnProtocolDialog by remember { mutableStateOf(false) }
            var showStartupDialog by remember { mutableStateOf(false) }
            var showProtocolDialog by remember { mutableStateOf(false) }
            var showPortDialog by remember { mutableStateOf(false) }
            var showKillSwitchDialog by remember { mutableStateOf(false) }
            var navigateToVpnSettings by remember { mutableStateOf(false) }
            var openContactSupport by remember { mutableStateOf(false) }
            var openUrlTermsService by remember { mutableStateOf(false) }
            var openUrlPolicy by remember { mutableStateOf(false) }
            var showLogoutDialog by remember { mutableStateOf(false) }
            var logoutUser by remember { mutableStateOf(false) }
            var isLoading by rememberSaveable { mutableStateOf(false) }

            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = LocalColors.current.scheme.background,
                            titleContentColor = LocalColors.current.scheme.onBackground,
                        ),
                        title = {
                            Text(stringResource(id = string.settings_screen_label_title))
                        },
                        navigationIcon = {
                            IconButton(onClick = { onNavigateBack() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        })
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                ) {
                    SettingsContent(
                        startupClick = {
                            showStartupDialog = true
                        },
                        vpnProtocolClick = {
                            showVpnProtocolDialog = true
                        },

                        scrambleClick = {
                            viewModel.onScrambleClick(it)
                        },
                        protocolClick = {
                            showProtocolDialog = true
                        },
                        portClick = {
                            showPortDialog = true
                        },
                        killSwitchClick = {
                            showKillSwitchDialog = true
                        },
                        contactClick = {
                            openContactSupport = true
                        },
                        termsServiceClick = {
                            openUrlTermsService = true
                        },
                        policyClick = {
                            openUrlPolicy = true
                        },
                        logoutClick = {
                            showLogoutDialog = true
                        },
                        isScrambleEnabled = (screenSettingsEvent.protocolSettings as?
                                OpenVpn)?.scramble ?: false,
                        selectedStartup = stringResource(
                            id =
                            when (screenSettingsEvent.connectionSettings.startupConnectOption) {
                                StartupConnectOption.None ->
                                    string.settings_screen_label_startup_do_not
                                StartupConnectOption.LastServer ->
                                    string.settings_screen_label_startup_last_server
                                StartupConnectOption.FastestServer ->
                                    string.settings_screen_label_startup_fastest
                            }
                        ),
                        selectedVpnProtocol =
                            screenSettingsEvent.connectionSettings.selectedProtocol,
                        selectedVpnProtocolName = stringResource(
                            id =
                            when (screenSettingsEvent.connectionSettings.selectedProtocol) {
                                Protocol.OpenVpn -> string.settings_screen_label_openvpn
                                Protocol.IKEv2 -> string.settings_screen_label_ikev2
                                Protocol.WireGuard -> string.settings_screen_label_wireguard
                            }
                        ),
                        selectedProtocol = stringResource(
                            id =
                            when ((screenSettingsEvent.protocolSettings as?
                                    OpenVpn)?.internetProtocol) {
                                InternetProtocol.TCP -> string.settings_screen_label_tcp
                                InternetProtocol.UDP -> string.settings_screen_label_udp
                                else -> string.settings_screen_label_tcp
                            }
                        ),
                        selectedPort = (screenSettingsEvent.protocolSettings as?
                                OpenVpn)?.port,
                        allowLan = when (screenSettingsEvent.protocolSettings) {
                            is IKEv2 -> screenSettingsEvent.protocolSettings.allowLan
                            is OpenVpn -> screenSettingsEvent.protocolSettings.allowLan
                            is Wireguard -> screenSettingsEvent.protocolSettings.allowLan
                        },
                        allowLanClick = viewModel::onAllowLanSelected,
                        overrideMtu = (screenSettingsEvent.protocolSettings as?
                                OpenVpn)?.overrideMtu ?: false,
                        overrideMtuClick = viewModel::onOverrideMtuSelected,
                        threatProtection =
                            screenSettingsEvent.connectionSettings.isThreatProtectionEnabled,
                        threatProtectionClick = viewModel::onThreatProtectionSelected,
                        splitTunnelingClick = onNavigateToSplitTunneling,
                        onDiagnosticsClick = onNavigateDiagnostics
                    )

                    if (showStartupDialog) {
                        val context = LocalContext.current

                        DialogWithRadioButtons(title = stringResource(
                            id =
                            string.settings_screen_label_auto_connect_title
                        ),
                            cancelText = stringResource(
                                id = string.generic_button_cancel
                            ), radioOptions = listOf(
                                stringResource(
                                    id = string.settings_screen_label_startup_last_server
                                ),
                                stringResource(id = string.settings_screen_label_startup_fastest),
                                stringResource(id = string.settings_screen_label_startup_do_not)
                            ),
                            initialSelectedValue = when (
                                screenSettingsEvent.connectionSettings.startupConnectOption) {

                                StartupConnectOption.None ->
                                    context.getString(string.settings_screen_label_startup_do_not)

                                StartupConnectOption.FastestServer ->
                                    context.getString(string.settings_screen_label_startup_fastest)

                                StartupConnectOption.LastServer ->
                                    context.getString(
                                        string.settings_screen_label_startup_last_server
                                    )
                            },
                            onOptionSelected = {
                                viewModel.onAutoStartupSelected(
                                    when (it) {
                                        context.getString(
                                            string.settings_screen_label_startup_do_not
                                        ) -> StartupConnectOption.None

                                        context.getString(
                                            string.settings_screen_label_startup_fastest
                                        ) -> StartupConnectOption.FastestServer

                                        context.getString(
                                            string.settings_screen_label_startup_last_server
                                        ) -> StartupConnectOption.LastServer

                                        else -> StartupConnectOption.None
                                    }
                                )
                            },
                            onDismissRequest = {
                                showStartupDialog = false
                            }
                        )
                    }

                    if (showVpnProtocolDialog) {
                        val context = LocalContext.current

                        DialogWithRadioButtons(title = stringResource(
                            id =
                            string.settings_screen_label_vpn_protocol_title
                        ),
                            cancelText = stringResource(
                                id = string.generic_button_cancel
                            ), radioOptions = listOf(
                                stringResource(id = string.settings_screen_label_wireguard),
                                stringResource(id = string.settings_screen_label_openvpn),
                                stringResource(id = string.settings_screen_label_ikev2)

                            ),
                            initialSelectedValue =
                            when (screenSettingsEvent.connectionSettings.selectedProtocol) {
                                Protocol.OpenVpn -> context.getString(
                                    string.settings_screen_label_openvpn
                                )

                                Protocol.IKEv2 -> context.getString(
                                    string.settings_screen_label_ikev2
                                )

                                Protocol.WireGuard ->
                                    context.getString(string.settings_screen_label_wireguard)
                            },
                            onOptionSelected = {
                                viewModel.onVpnProtocolSelected(
                                    when (it) {
                                        context.getString(string.settings_screen_label_openvpn) ->
                                            Protocol.OpenVpn

                                        context.getString(string.settings_screen_label_ikev2) ->
                                            Protocol.IKEv2

                                        context.getString(string.settings_screen_label_wireguard) ->
                                            Protocol.WireGuard

                                        else -> Protocol.WireGuard
                                    }
                                )
                            },
                            onDismissRequest = {
                                showVpnProtocolDialog = false
                            }
                        )
                    }

                    if (showProtocolDialog) {
                        val context = LocalContext.current

                        DialogWithRadioButtons(
                            title = stringResource(
                                id = string.settings_screen_label_protocol_title
                            ),
                            cancelText = stringResource(
                                id = string.generic_button_cancel
                            ), radioOptions = listOf(
                                stringResource(id = string.settings_screen_label_tcp),
                                stringResource(id = string.settings_screen_label_udp),
                            ),
                            initialSelectedValue = when ((screenSettingsEvent.protocolSettings as?
                                    OpenVpn)?.internetProtocol) {
                                InternetProtocol.TCP -> context.getString(
                                    string.settings_screen_label_tcp
                                )

                                InternetProtocol.UDP -> context.getString(
                                    string.settings_screen_label_udp
                                )

                                else -> context.getString(string.settings_screen_label_tcp)
                            },
                            onOptionSelected = {
                                viewModel.onProtocolClick(
                                    when (it) {
                                        context.getString(string.settings_screen_label_tcp) ->
                                            InternetProtocol.TCP

                                        context.getString(string.settings_screen_label_udp) ->
                                            InternetProtocol.UDP

                                        else -> InternetProtocol.TCP
                                    }
                                )
                            },
                            onDismissRequest = {
                                showProtocolDialog = false
                            }
                        )
                    }

                    if (showPortDialog) {

                        DialogWithRadioButtons(title = stringResource(
                            id = string.settings_screen_label_port_title
                        ),
                            cancelText = stringResource(id = string.generic_button_cancel),
                            radioOptions =
                            screenSettingsEvent.availableVpnPorts.sorted().map { it.toString() },
                            initialSelectedValue =
                            when (val port =
                                (screenSettingsEvent.protocolSettings as?
                                        OpenVpn)?.port) {
                                is OpenVpnPort.Normal -> port.value.toString()
                                is OpenVpnPort.Scramble -> port.value.toString()
                                else -> ""
                            },
                            onOptionSelected = {
                                viewModel.onPortClick(it.toInt())
                            },
                            onDismissRequest = {
                                showPortDialog = false
                            }
                        )
                    }

                    if (showKillSwitchDialog) {

                        AlertDialog(
                            title = {
                                Text(
                                    text = stringResource(
                                        id = string.kill_switch_dialog_label_title
                                    )
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(
                                        id = string.kill_switch_dialog_label_message,
                                        stringResource(id = string.app_name)
                                    )
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    showKillSwitchDialog = false
                                    navigateToVpnSettings = true
                                }
                                ) {
                                    Text(
                                        text = stringResource(
                                            id = string.kill_switch_dialog_button_take_me_there
                                        )
                                    )
                                }
                            },

                            dismissButton = {
                                TextButton(onClick = { showKillSwitchDialog = false }) {
                                    Text(
                                        text = stringResource(
                                            id = string.generic_button_cancel
                                        )
                                    )
                                }
                            },
                            onDismissRequest = { showKillSwitchDialog = false },
                        )
                    }

                    if (showLogoutDialog) {
                        AlertDialog(
                            title = {
                                Text(
                                    text = stringResource(
                                        id = string.logout_dialog_label_title
                                    )
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(
                                        id = string.logout_dialog_label_message
                                    )
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    isLoading = true
                                    showLogoutDialog = false
                                    logoutUser = true
                                }
                                ) {
                                    Text(
                                        text = stringResource(
                                            id = string.logout_dialog_button_logout
                                        )
                                    )
                                }
                            },

                            dismissButton = {
                                TextButton(onClick = { showLogoutDialog = false }) {
                                    Text(
                                        text = stringResource(
                                            id = string.generic_button_cancel
                                        )
                                    )
                                }
                            },
                            onDismissRequest = { showLogoutDialog = false },
                        )
                    }

                    if (navigateToVpnSettings) {
                        navigateToVpnSettings = false
                        navigateToVpnSettings(context)
                    }

                    if (openContactSupport) {
                        openContactSupport = false
                        openWebViewWithUrl(
                            url = stringResource(id = string.url_contact_support),
                            launcher
                        )
                    }

                    if (openUrlTermsService) {
                        openUrlTermsService = false
                        openWebViewWithUrl(
                            url = stringResource(id = string.url_terms_service),
                            launcher
                        )
                    }

                    if (openUrlPolicy) {
                        openUrlPolicy = false
                        openWebViewWithUrl(
                            url = stringResource(id = string.url_policy),
                            launcher
                        )
                    }

                    if (logoutUser) {
                        logoutUser = false
                        viewModel.onLogout()
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = LocalColors.current.scheme.background.copy(
                                        alpha = 0.6f
                                    )
                                )
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsContent(
    startupClick: () -> Unit = {},
    vpnProtocolClick: () -> Unit = {},
    scrambleClick: (Boolean) -> Unit = {},
    protocolClick: () -> Unit = {},
    portClick: () -> Unit = {},
    killSwitchClick: () -> Unit = {},
    contactClick: () -> Unit = {},
    termsServiceClick: () -> Unit = {},
    policyClick: () -> Unit = {},
    logoutClick: () -> Unit = {},
    isScrambleEnabled: Boolean = false,
    selectedStartup: String? = null,
    selectedVpnProtocolName: String? = null,
    selectedVpnProtocol: Protocol = Protocol.WireGuard,
    selectedProtocol: String? = null,
    selectedPort: OpenVpnPort? = null,
    allowLan: Boolean = false,
    allowLanClick: (Boolean) -> Unit = {},
    overrideMtu: Boolean = false,
    overrideMtuClick: (Boolean) -> Unit = {},
    threatProtection: Boolean = false,
    threatProtectionClick: (Boolean) -> Unit = {},
    splitTunnelingClick: () -> Unit = {},
    onDiagnosticsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsCategory(string.settings_screen_label_auto_connect_category)
        SettingsPreference(
            icon = R.drawable.ic_connect_startup,
            title = string.settings_screen_label_auto_connect_title,
            body = selectedStartup,
            onClick = startupClick
        )
        Divider()
        SettingsCategory(title = string.settings_screen_label_connection_category)

        SettingsPreference(
            icon = R.drawable.ic_vpn_protocol,
            title = string.settings_screen_label_vpn_protocol_title,
            body = selectedVpnProtocolName,
            onClick = vpnProtocolClick
        )

        SwitchSettingsPreference(
            icon = R.drawable.ic_lan,
            title = string.settings_screen_label_allow_lan_title,
            body = string.settings_screen_label_allow_lan_description,
            checked = allowLan,
            onClick = allowLanClick
        )

        SettingsPreference(
            icon = R.drawable.ic_split_tunnel,
            title = string.settings_screen_label_split_tunneling_title,
            body = stringResource(string.settings_screen_label_split_tunneling_description),
            onClick = splitTunnelingClick
        )

        SwitchSettingsPreference(
            icon = R.drawable.ic_advanced_protection,
            title = string.settings_screen_label_threat_protection_title,
            body = string.settings_screen_label_threat_protection_description,
            checked = threatProtection,
            onClick = threatProtectionClick
        )

        if (selectedVpnProtocol == Protocol.OpenVpn) {
            SwitchSettingsPreference(
                icon = R.drawable.ic_scramble,
                title = string.settings_screen_label_scramble_title,
                body = string.settings_screen_label_scramble_description,
                onClick = scrambleClick,
                checked = isScrambleEnabled
            )

            SettingsPreference(
                icon = R.drawable.ic_protocol,
                title = string.settings_screen_label_protocol_title,
                body = selectedProtocol,
                onClick = protocolClick
            )

            SettingsPreference(
                icon = R.drawable.ic_port,
                title = string.settings_screen_label_port_title,
                body = when (selectedPort) {
                    is OpenVpnPort.Normal -> selectedPort.value.toString()
                    is OpenVpnPort.Scramble -> selectedPort.value.toString()
                    else -> ""
                },
                onClick = portClick
            )

            SwitchSettingsPreference(
                icon = R.drawable.ic_reset_mtu,
                title = string.settings_screen_label_override_mtu_title,
                body = string.settings_screen_label_override_mtu_description,
                checked = overrideMtu,
                onClick = overrideMtuClick
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SettingsPreference(
                icon = R.drawable.ic_kill_switch,
                title = string.settings_screen_label_kill_switch_title,
                body = stringResource(string.settings_screen_label_kill_switch_description),
                onClick = killSwitchClick
            )
        }
        Divider()
        SettingsCategory(title = string.settings_screen_label_general_category)

        SettingsPreference(
            icon = R.drawable.ic_contact_24dp,
            title = string.settings_screen_label_support_title,
            onClick = contactClick
        )

        SettingsPreference(
            icon = R.drawable.ic_diagnosis,
            title = string.settings_screen_label_diagnostics_title,
            onClick = onDiagnosticsClick
        )

        SettingsPreference(
            icon = R.drawable.ic_terms_service,
            title = string.settings_screen_label_terms_of_service,
            onClick = termsServiceClick
        )

        SettingsPreference(
            icon = R.drawable.ic_policy,
            title = string.settings_screen_label_privacy_policy,
            onClick = policyClick
        )

        SettingsPreference(
            icon = R.drawable.ic_logout,
            title = string.settings_screen_label_logout_title,
            onClick = logoutClick
        )
    }
}

@Composable
fun SettingsPreference(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    body: String? = null,
    onClick: () -> Unit = { }
) {
    Row(modifier = Modifier
        .clickable { onClick() }
        .padding(dimensionResource(id = R.dimen.spacing_small))) {

        Icon(
            modifier = Modifier.align(alignment = Alignment.CenterVertically),
            painter = painterResource(id = icon), contentDescription = null
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = dimensionResource(id = R.dimen.spacing_normal))
                .align(Alignment.CenterVertically)
        ) {
            Text(text = stringResource(id = title), style = MaterialTheme.typography.titleMedium)
            if (body != null) {
                Text(text = body)
            }
        }
    }
}

@Composable
fun SwitchSettingsPreference(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes body: Int? = null,
    checked: Boolean,
    onClick: (Boolean) -> Unit = { }
) {
    Row(modifier = Modifier
        .clickable { onClick(!checked) }
        .padding(dimensionResource(id = R.dimen.spacing_small))) {
        Icon(
            modifier = Modifier.align(alignment = Alignment.CenterVertically),
            painter = painterResource(id = icon), contentDescription = null
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = dimensionResource(id = R.dimen.spacing_normal))
                .align(Alignment.CenterVertically)
        ) {
            Text(text = stringResource(id = title), style = MaterialTheme.typography.titleMedium)
            if (body != null) {
                Text(text = stringResource(id = body))
            }
        }
        Switch(
            checked = checked, onCheckedChange = { value -> onClick(value) },
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun SettingsCategory(@StringRes title: Int) {
    Row(
        modifier = Modifier
            .padding(
                horizontal = dimensionResource(id = R.dimen.spacing_small),
                vertical = dimensionResource(id = R.dimen.spacing_xxsmall)
            )
    ) {
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.spacing_medium)))

        Text(
            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.spacing_normal)),
            text = stringResource(id = title),
            style = MaterialTheme.typography.titleSmall.copy(
                color = LocalColors.current.extendedColors.controlHighlightColor,
            ),
            fontWeight = FontWeight.Bold
        )
    }

}

@Composable
@Preview
fun SettingsHeaderPreview() {
    SettingsCategory(string.settings_screen_label_auto_connect_category)
}

@Composable
@Preview
fun SettingsScreenPreview() {
    SettingsContent()
}

@Composable
@Preview
fun SettingsScreenForOpenVpnPreview() {
    SettingsContent(
        selectedVpnProtocol = Protocol.OpenVpn,
    )
}

private fun openWebViewWithUrl(
    url: String,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    launcher.launch(intent)
}

@Composable
private fun navigateToVpnSettings(context: Context) {
    try {
        val intent = Intent(DEVICE_VPN_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    } catch (exception: ActivityNotFoundException) {
        showToast(stringResource(id = string.settings_screen_label_open_vpn_settings))
    }
}

@Composable
private fun navigateToLogin(context: Context) {
    LaunchedEffect(Unit) {
        Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }.also {
            context.startActivity(it)
        }
    }
}

@Composable
fun showToast(message: String) {
    Toast.makeText(
        LocalContext.current,
        message,
        Toast.LENGTH_LONG
    ).show()
}