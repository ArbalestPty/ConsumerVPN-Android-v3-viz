package com.wlvpn.consumervpn.presentation.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import com.wlvpn.consumervpn.R.drawable
import com.wlvpn.consumervpn.R.string
import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import com.wlvpn.consumervpn.domain.value.settings.InternetProtocol.TCP
import com.wlvpn.consumervpn.domain.value.settings.InternetProtocol.UDP
import com.wlvpn.consumervpn.domain.value.settings.OpenVpnPort.Normal
import com.wlvpn.consumervpn.domain.value.settings.OpenVpnPort.Scramble
import com.wlvpn.consumervpn.domain.value.settings.Protocol
import com.wlvpn.consumervpn.domain.value.settings.Protocol.IKEv2
import com.wlvpn.consumervpn.domain.value.settings.Protocol.WireGuard
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings.OpenVpn
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings.Wireguard
import com.wlvpn.consumervpn.domain.value.settings.StartupConnectOption.FastestServer
import com.wlvpn.consumervpn.domain.value.settings.StartupConnectOption.LastServer
import com.wlvpn.consumervpn.domain.value.settings.StartupConnectOption.None
import com.wlvpn.consumervpn.presentation.MainActivity
import com.wlvpn.consumervpn.presentation.settings.SettingsEvent.ErrorGettingSettings
import com.wlvpn.consumervpn.presentation.settings.SettingsEvent.ErrorPreparingThreatProtection
import com.wlvpn.consumervpn.presentation.settings.SettingsEvent.ExpiredAccessToken
import com.wlvpn.consumervpn.presentation.settings.SettingsEvent.InvalidAccessToken
import com.wlvpn.consumervpn.presentation.settings.SettingsEvent.LoadingDataEvent
import com.wlvpn.consumervpn.presentation.settings.SettingsEvent.SettingsReceived
import com.wlvpn.consumervpn.presentation.settings.SettingsEvent.SuccessLogout
import com.wlvpn.consumervpn.presentation.settings.SettingsEvent.UnableToLogout
import com.wlvpn.consumervpn.presentation.splittunneling.SplitTunnelingScreen
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.LocalDimens
import com.wlvpn.consumervpn.presentation.ui.theme.displayNormalFontFamily
import com.wlvpn.consumervpn.presentation.ui.theme.displayThinFontFamily
import com.wlvpn.consumervpn.presentation.ui.theme.extended

@Composable
fun SettingsScreen(
    modifier: Modifier,
    visibility: Boolean,
    onBackPressed: () -> Unit
) {

    if (visibility.not())
        return

    val viewmodel: SettingsViewModel = hiltViewModel()

    val requester = remember { FocusRequester() }

    val state by viewmodel.settingsEvent.collectAsStateWithLifecycle()

    var protocolSettings: ProtocolSettings by remember { mutableStateOf(Wireguard()) }
    var connectionSettings by remember { mutableStateOf(ConnectionSettings()) }
    var availablePorts by remember { mutableStateOf(emptyList<Int>()) }

    var showVpnProtocolDialog by remember { mutableStateOf(false) }
    var showPortDialog by remember { mutableStateOf(false) }
    var showInternetProtocolDialog by remember { mutableStateOf(false) }
    var showConnectAtStartupDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showTermsOfServiceDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showAboutUs by remember { mutableStateOf(false) }
    var showSplitTunneling by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val isDialogVisible by remember {
        derivedStateOf {
            showVpnProtocolDialog ||
                    showPortDialog ||
                    showInternetProtocolDialog ||
                    showConnectAtStartupDialog ||
                    showLogoutDialog ||
                    showTermsOfServiceDialog ||
                    showPrivacyPolicyDialog ||
                    showAboutUs ||
                    showSplitTunneling
        }
    }

    when (state) {
        ErrorGettingSettings ->
            ShowToast(string.service_error.stringRes)

        ErrorPreparingThreatProtection ->
            ShowToast(string.settings_error_preparing_threat_protection_label.stringRes)

        ExpiredAccessToken,
        UnableToLogout,
        InvalidAccessToken -> {
            ShowToast(string.not_logged_in_error.stringRes)
            NavigateToLogin()
        }

        SuccessLogout -> NavigateToLogin()

        is SettingsReceived -> {
            protocolSettings = (state as SettingsReceived).protocolSettings
            connectionSettings = (state as SettingsReceived).connectionSettings
            availablePorts = (state as SettingsReceived).availableVpnPorts
        }

        LoadingDataEvent -> {
            //no-op
        }
    }


    BackHandler(isDialogVisible.not()) {
        onBackPressed()
    }

    Row(
        modifier = modifier
            .focusRequester(requester)
            .background(LocalColors.current.extendedColors.settingsScreenScrimColor)
    ) {
        Spacer(Modifier.weight(2f))

        val itemPaddingModifier =
            Modifier.padding(
                top = LocalDimens.current.normal,
                start = LocalDimens.current.large,
                end = LocalDimens.current.large
            )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(LocalColors.current.extendedColors.settingsBackgroundColor)
                .weight(1.5f)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalColors.current.extendedColors.settingsBackgroundColor)
            ) {

                item {
                    Text(
                        modifier = itemPaddingModifier.padding(top = LocalDimens.current.xLarge),
                        text = string.settings_screen_label_title.stringRes,
                        fontSize = LocalDimens.current.extended.settingsTitleSize,
                        color = LocalColors.current.extendedColors.contentPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = displayNormalFontFamily
                    )
                }

                item {
                    SettingsSection(
                        modifier = itemPaddingModifier,
                        title = string.settings_screen_label_auto_connect_category.stringRes
                    )

                    // Auto connect
                    SettingsItem(
                        modifier = Modifier.padding(
                            start = LocalDimens.current.xSmall,
                            end = LocalDimens.current.xSmall
                        ),
                        iconPainter = painterResource(drawable.ic_connect_startup),
                        title = string.settings_screen_label_auto_connect_title.stringRes,
                        description = when (connectionSettings.startupConnectOption) {
                            None ->
                                string.settings_screen_label_startup_do_not.stringRes

                            LastServer ->
                                string.settings_screen_label_startup_last_server.stringRes

                            FastestServer ->
                                string.settings_screen_label_startup_fastest.stringRes
                        }
                    ) {
                        showConnectAtStartupDialog = true
                    }
                }

                item {
                    SettingsSection(
                        modifier = itemPaddingModifier,
                        title = string.settings_screen_label_split_tunneling_title.stringRes
                    )

                    // Split tunneling
                    SettingsItem(
                        iconPainter = painterResource(drawable.ic_split_tunnel),
                        modifier = Modifier.padding(
                            start = LocalDimens.current.xSmall,
                            end = LocalDimens.current.xSmall
                        ),
                        title = string.settings_screen_label_split_tunneling_title.stringRes,
                        description =
                            string.settings_screen_label_split_tunneling_description.stringRes
                    ) {
                        showSplitTunneling = true
                    }
                }

                item {
                    SettingsSection(
                        modifier = itemPaddingModifier,
                        title = string.settings_screen_label_connection_category.stringRes
                    )
                }
                item {
                    // VPN protocol
                    SettingsItem(
                        iconPainter = painterResource(drawable.ic_vpn_protocol),
                        modifier = Modifier.padding(
                            start = LocalDimens.current.xSmall,
                            end = LocalDimens.current.xSmall
                        ),
                        title = string.settings_screen_label_vpn_protocol_title.stringRes,
                        description = when (connectionSettings.selectedProtocol) {
                            IKEv2 -> string.settings_screen_label_ikev2.stringRes
                            Protocol.OpenVpn -> string.settings_screen_label_openvpn.stringRes
                            WireGuard -> string.settings_screen_label_wireguard.stringRes
                        }
                    ) {
                        showVpnProtocolDialog = true
                    }
                }
                item {
                    // Allow Lan
                    SettingsSwitch(
                        iconPainter = painterResource(drawable.ic_lan),
                        modifier = Modifier.padding(
                            start = LocalDimens.current.xSmall,
                            end = LocalDimens.current.xSmall
                        ),
                        title = string.settings_screen_label_allow_lan_title.stringRes,
                        description = string.settings_screen_label_allow_lan_description.stringRes,
                        checked = when (protocolSettings) {
                            is ProtocolSettings.IKEv2 ->
                                (protocolSettings as ProtocolSettings.IKEv2).allowLan

                            is OpenVpn ->
                                (protocolSettings as OpenVpn).allowLan

                            is Wireguard ->
                                (protocolSettings as Wireguard).allowLan
                        }
                    ) {
                        viewmodel.onAllowLanSelected(it)
                    }
                }
                item {
                    // Threat protection
                    SettingsSwitch(
                        iconPainter = painterResource(drawable.ic_advanced_protection),
                        modifier = Modifier.padding(
                            start = LocalDimens.current.xSmall,
                            end = LocalDimens.current.xSmall
                        ),
                        title = string.settings_screen_label_threat_protection_title.stringRes,
                        description = string.settings_screen_label_threat_protection_description.stringRes,
                        checked = connectionSettings.isThreatProtectionEnabled
                    ) {
                        viewmodel.onThreatProtectionSelected(it)
                    }
                }

                item {
                    // OpenVPN: Scramble
                    SettingsSwitch(
                        iconPainter = painterResource(drawable.ic_scramble),
                        modifier = Modifier.padding(
                            start = LocalDimens.current.xSmall,
                            end = LocalDimens.current.xSmall
                        ),
                        title = string.settings_screen_label_scramble_title.stringRes,
                        visibility = protocolSettings is OpenVpn,
                        description = string.settings_screen_label_scramble_description.stringRes,
                        checked = (protocolSettings as? OpenVpn)?.scramble ?: false
                    ) {
                        viewmodel.onScrambleClick(it)
                    }
                }

                item {
                    // Internet protocol
                    SettingsItem(
                        visibility = protocolSettings is OpenVpn,
                        iconPainter = painterResource(drawable.ic_protocol),
                        modifier = Modifier.padding(
                            start = LocalDimens.current.xSmall,
                            end = LocalDimens.current.xSmall
                        ),
                        title = string.settings_screen_label_protocol_title.stringRes,
                        description = when ((protocolSettings as? OpenVpn)?.internetProtocol) {
                            TCP -> string.settings_screen_label_tcp.stringRes
                            UDP -> string.settings_screen_label_udp.stringRes
                            null -> "-"
                        }
                    ) {
                        showInternetProtocolDialog = true
                    }
                }
                item {

                    // Port
                    SettingsItem(
                        visibility = protocolSettings is OpenVpn,
                        iconPainter = painterResource(drawable.ic_port),
                        modifier = Modifier.padding(
                            start = LocalDimens.current.xSmall,
                            end = LocalDimens.current.xSmall
                        ),
                        title = string.settings_screen_label_port_title.stringRes,
                        description = when (val port = (protocolSettings as? OpenVpn)?.port) {
                            is Normal -> port.value.toString()
                            is Scramble -> port.value.toString()
                            null -> "-"
                        }
                    ) {
                        showPortDialog = true
                    }
                }
                item {

                    // MTU
                    SettingsSwitch(
                        visibility = protocolSettings is OpenVpn,
                        iconPainter = painterResource(drawable.ic_reset_mtu),
                        modifier = Modifier.padding(
                            start = LocalDimens.current.xSmall,
                            end = LocalDimens.current.xSmall
                        ),
                        title = string.settings_screen_label_override_mtu_title.stringRes,
                        description =
                        string.settings_screen_label_override_mtu_description.stringRes,
                        checked = (protocolSettings as? OpenVpn)?.overrideMtu ?: false
                    ) {
                        viewmodel.onOverrideMtuSelected(it)
                    }
                }

                item {

                    //About ConsumerVPN
                    SettingsSection(
                        modifier = itemPaddingModifier,
                        title = string.settings_screen_label_general_category.stringRes
                    )
                    SettingsItem(
                        iconPainter = painterResource(drawable.ic_about_us),
                        modifier = Modifier.padding(
                            start = LocalDimens.current.xSmall,
                            end = LocalDimens.current.xSmall
                        ),
                        title = "About ConsumerVPN"
                    ) {
                        showAboutUs = true
                    }

                    SettingsItem(
                        iconPainter = painterResource(drawable.ic_terms_service),
                        modifier = Modifier.padding(
                            start = LocalDimens.current.xSmall,
                            end = LocalDimens.current.xSmall
                        ),
                        title = string.settings_screen_label_terms_of_service.stringRes
                    ) {
                        showTermsOfServiceDialog = true
                    }

                    SettingsItem(
                        iconPainter = painterResource(drawable.ic_policy),
                        modifier = Modifier.padding(
                            start = LocalDimens.current.xSmall,
                            end = LocalDimens.current.xSmall
                        ),
                        title = string.settings_screen_label_privacy_policy.stringRes
                    ) {
                        showPrivacyPolicyDialog = true
                    }

                    SettingsItem(
                        iconPainter = painterResource(drawable.ic_logout),
                        modifier = Modifier.padding(
                            start = LocalDimens.current.xSmall,
                            end = LocalDimens.current.xSmall
                        ),
                        title = string.settings_screen_label_logout_title.stringRes
                    ) {
                        showLogoutDialog = true
                    }
                }
            }

        }
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = isDialogVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        when {
            showLogoutDialog ->
                AlertDialog(
                    modifier = modifier.fillMaxSize(),
                    title = string.logout_dialog_label_message.stringRes,
                    dismissButtonTitle = string.generic_button_cancel.stringRes,
                    confirmButtonTitle = string.logout_dialog_button_logout.stringRes,
                    onDismiss = {
                        showLogoutDialog = false
                    },
                    onConfirm = {
                        viewmodel.onLogout()
                    }) {
                    showLogoutDialog = false
                }

            showConnectAtStartupDialog ->
                RadioButtonListDialog(
                    modifier = modifier.fillMaxSize(),
                    title = string.settings_screen_startup_dialog_title.stringRes,
                    items = listOf(
                        FastestServer,
                        LastServer,
                        None
                    ),
                    selectedItem = connectionSettings.startupConnectOption,
                    onBackPressed = {
                        showConnectAtStartupDialog = false
                    },
                    itemTitle = { item ->
                        when (item) {
                            None ->
                                string.settings_screen_label_startup_do_not.stringRes

                            LastServer ->
                                string.settings_screen_label_startup_last_server.stringRes

                            FastestServer ->
                                string.settings_screen_label_startup_fastest.stringRes
                        }
                    },
                    onClick = { item ->
                        viewmodel.onAutoStartupSelected(item)
                    }
                )

            showVpnProtocolDialog -> {
                RadioButtonListDialog(
                    modifier = modifier.fillMaxSize(),
                    title = string.settings_screen_vpn_protocol_dialog_title.stringRes,
                    items = listOf(
                        WireGuard,
                        Protocol.OpenVpn,
                        IKEv2,
                    ),
                    selectedItem = connectionSettings.selectedProtocol,
                    onBackPressed = {
                        showVpnProtocolDialog = false
                    },
                    itemTitle = { item ->
                        when (item) {
                            IKEv2 -> string.settings_screen_label_ikev2.stringRes
                            Protocol.OpenVpn -> string.settings_screen_label_openvpn.stringRes
                            WireGuard -> string.settings_screen_label_wireguard.stringRes
                        }
                    },
                    onClick = { item ->
                        viewmodel.onVpnProtocolSelected(item)
                        showVpnProtocolDialog = false
                    }
                )
            }

            showPortDialog ->
                RadioButtonListDialog(
                    modifier = modifier.fillMaxSize(),
                    title = string.settings_screen_port_dialog_title.stringRes,
                    items = availablePorts,
                    selectedItem = when (val port = (protocolSettings as? OpenVpn)?.port) {
                        is Normal -> port.value
                        is Scramble -> port.value
                        null -> 0
                    },
                    onBackPressed = {
                        showPortDialog = false
                    },
                    itemTitle = { item ->
                        item.toString()
                    },
                    onClick = { item ->
                        viewmodel.onPortClick(item)
                    }
                )

            showInternetProtocolDialog ->
                RadioButtonListDialog(
                    modifier = modifier.fillMaxSize(),
                    title = string.settings_screen_internet_protocol_dialog_title.stringRes,
                    items = listOf(
                        TCP,
                        UDP
                    ),
                    selectedItem = (protocolSettings as? OpenVpn)?.internetProtocol ?: TCP,
                    onBackPressed = {
                        showInternetProtocolDialog = false
                    },
                    itemTitle = { item ->
                        when (item) {
                            TCP -> string.settings_screen_label_tcp.stringRes
                            UDP -> string.settings_screen_label_udp.stringRes
                        }
                    },
                    onClick = { item ->
                        viewmodel.onProtocolClick(item)
                    }
                )

            showTermsOfServiceDialog -> {
                WebViewDialog(
                    modifier = Modifier.fillMaxSize(),
                    url = string.url_terms_service.stringRes,
                    onBackPressed = {
                        showTermsOfServiceDialog = false
                    }
                )
            }

            showPrivacyPolicyDialog -> {
                WebViewDialog(
                    modifier = Modifier.fillMaxSize(),
                    url = string.url_policy.stringRes,
                    onBackPressed = {
                        showPrivacyPolicyDialog = false
                    }
                )
            }
            showAboutUs -> {
               AboutUsScreen(onBackPressed = { showAboutUs = false})
            }

            showSplitTunneling -> {
                SplitTunnelingScreen(
                    onBackPressed = { showSplitTunneling = false }
                )
            }

        }

    }

    LaunchedEffect(Unit) {
        requester.requestFocus()
    }
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    title: String = "",
    description: String = "",
    iconPainter: Painter = painterResource(id = drawable.ic_location),
    visibility: Boolean = true,
    onClick: () -> Unit = {}
) {

    if (!visibility) {
        return
    }

    ListItem(
        modifier = modifier,
        enabled = true,
        selected = false,
        onClick = { onClick() },
        shape = ListItemDefaults.shape(shape = RoundedCornerShape(LocalDimens.current.zero)),
        colors = ListItemDefaults.colors(
            focusedContainerColor =
            LocalColors.current.extendedColors.settingsItemFocusedContainerColor,
            focusedContentColor = LocalColors.current.extendedColors.contentPrimary
        ),
        supportingContent = {
            if (description.isNotBlank()) {
                Text(
                    modifier = Modifier.padding(top = LocalDimens.current.xSmall),
                    text = description,
                    fontSize = LocalDimens.current.extended.settingsItemDescriptionSize,
                    color = LocalColors.current.extendedColors.contentPrimary,
                    fontFamily = displayThinFontFamily
                )
            }
        },
        leadingContent = {
            Icon(
                painter = iconPainter,
                contentDescription = null,
            )
        },
        headlineContent = {
            Text(
                modifier = Modifier,
                text = title,
                fontSize = LocalDimens.current.extended.settingsItemTitleSize,
                color = LocalColors.current.extendedColors.contentPrimary,
                fontWeight = FontWeight.Bold,
                fontFamily = displayNormalFontFamily
            )
        }
    )
}

@Composable
fun SettingsSection(
    modifier: Modifier = Modifier,
    title: String = "",
) {
    Text(
        modifier = modifier,
        text = title,
        fontSize = LocalDimens.current.extended.settingsSectionTitleSize,
        color = LocalColors.current.scheme.primaryContainer,
        fontFamily = displayNormalFontFamily
    )
}

@Composable
fun SettingsSwitch(
    modifier: Modifier = Modifier,
    title: String = "",
    description: String = "",
    iconPainter: Painter = painterResource(id = drawable.ic_location),
    checked: Boolean = false,
    visibility: Boolean = true,
    onClick: (Boolean) -> Unit = {}
) {
    if (!visibility) {
        return
    }
    ListItem(
        modifier = modifier,
        enabled = true,
        selected = false,
        onClick = {
            onClick(!checked)
        },
        shape = ListItemDefaults.shape(shape = RoundedCornerShape(LocalDimens.current.zero)),
        colors = ListItemDefaults.colors(
            focusedContainerColor =
            LocalColors.current.extendedColors.settingsItemFocusedContainerColor,
            focusedContentColor = LocalColors.current.extendedColors.contentPrimary
        ),
        supportingContent = {
            if (description.isNotBlank()) {
                Text(
                    modifier = Modifier.padding(top = LocalDimens.current.xSmall),
                    text = description,
                    fontSize = LocalDimens.current.extended.settingsItemDescriptionSize,
                    color = LocalColors.current.extendedColors.contentPrimary,
                    fontFamily = displayThinFontFamily
                )
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = { onClick(it) }
            )
        },
        leadingContent = {
            Icon(
                painter = iconPainter,
                contentDescription = null,
            )
        },
        headlineContent = {
            Text(
                text = title,
                fontSize = LocalDimens.current.extended.settingsItemTitleSize,
                color = LocalColors.current.extendedColors.contentPrimary,
                fontWeight = FontWeight.Bold,
                fontFamily = displayNormalFontFamily
            )
        }
    )
}

@Composable
fun NavigateToLogin() {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }.also {
            context.startActivity(it)
        }
    }
}

@Composable
private fun ShowToast(message: String) {
    Toast.makeText(
        LocalContext.current,
        message,
        Toast.LENGTH_LONG
    ).show()
}

private val Int.stringRes @Composable get() = stringResource(this)