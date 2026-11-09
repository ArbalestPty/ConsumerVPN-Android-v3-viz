package com.wlvpn.consumervpn.presentation.splittunneling

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Checkbox
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.FilterChip
import androidx.tv.material3.FilterChipDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.Text
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.R.string
import com.wlvpn.consumervpn.domain.value.settings.SplitTunnelSettings
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.LocalDimens
import com.wlvpn.consumervpn.presentation.ui.theme.displayNormalFontFamily
import com.wlvpn.consumervpn.presentation.ui.theme.displayThinFontFamily
import com.wlvpn.consumervpn.presentation.ui.theme.extended

private const val FIRST_ITEM_INDEX = 0

private fun FocusRequester.tryRequestFocus() {
    try {
        requestFocus()
    } catch (e: IllegalStateException) {
        // No op.
    }
}

@Composable
fun SplitTunnelingScreen(
    onBackPressed: () -> Unit,
    viewModel: SplitTunnelingViewModel = hiltViewModel()
) {
    val event by viewModel.splitTunnelingEvent
        .observeAsState(SplitTunnelingEvent.LoadingInProgress)

    val state = event
    val appsLoaded = state as? SplitTunnelingEvent.AppsLoaded
    val visibleApps = appsLoaded?.visibleApps.orEmpty()
    val isSearchActive = appsLoaded?.isSearchActive == true

    val topBarFocusRequester = remember { FocusRequester() }
    val searchButtonFocusRequester = remember { FocusRequester() }
    val searchFieldFocusRequester = remember { FocusRequester() }
    val filtersFocusRequester = remember { FocusRequester() }
    val appsFocusRequester = remember { FocusRequester() }

    var wasSearchActive by remember { mutableStateOf(false) }
    var hasFocusedAppList by remember { mutableStateOf(false) }

    // Back leaves the search mode first so the user is not thrown out of the screen, both the
    // dialog dismiss request and the back handler go through here so the behavior is the same
    // no matter which one the platform delivers the back press to
    val onBack: () -> Unit = {
        if (isSearchActive) {
            viewModel.toggleSearch(false)
        } else {
            onBackPressed()
        }
    }

    // Hosted in its own window, otherwise the settings list rendered behind this screen stays
    // composed and D-pad focus can travel into it. Matches how the rest of the settings
    // screens are presented, see AboutUsScreen
    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onBack
    ) {
        LaunchedEffect(Unit) {
            viewModel.loadApps()
        }

        BackHandler { onBack() }

        // Keeps a focusable target whenever the one in use disappears, the app list is only
        // focused on the first load, pushing the focus into it while it is being laid out
        // again makes the lazy layout place its items twice
        LaunchedEffect(appsLoaded != null, isSearchActive, visibleApps.isEmpty()) {
            val requester = when {
                appsLoaded == null -> topBarFocusRequester

                isSearchActive -> searchFieldFocusRequester.takeIf { !wasSearchActive }

                wasSearchActive -> searchButtonFocusRequester
                visibleApps.isEmpty() -> filtersFocusRequester

                !hasFocusedAppList -> {
                    hasFocusedAppList = true
                    appsFocusRequester
                }

                else -> null
            }

            wasSearchActive = isSearchActive

            requester?.tryRequestFocus()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = LocalColors.current.scheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = LocalDimens.current.large)
            ) {
                SplitTunnelingTopBar(
                    modifier = Modifier.focusRequester(topBarFocusRequester),
                    searchButtonFocusRequester = searchButtonFocusRequester,
                    searchFieldFocusRequester = searchFieldFocusRequester,
                    isSearchActive = isSearchActive,
                    searchQuery = appsLoaded?.searchQuery.orEmpty(),
                    areActionsEnabled = appsLoaded != null,
                    onBackPressed = onBack,
                    onSearchToggled = { isActive -> viewModel.toggleSearch(isActive) },
                    onSearchQueryChanged = { query -> viewModel.onSearchQueryChanged(query) },
                    onSelectAll = { viewModel.onSelectAll() },
                    onDeselectAll = { viewModel.onDeselectAll() }
                )

                when (state) {
                    is SplitTunnelingEvent.LoadingInProgress ->
                        LoadingContent()

                    is SplitTunnelingEvent.UnableToLoadFailure ->
                        MessageContent(
                            message =
                                stringResource(string.split_tunneling_screen_error_unable_to_load)
                        )

                    is SplitTunnelingEvent.AppsLoaded -> {
                        FiltersRow(
                            modifier = Modifier.focusRequester(filtersFocusRequester),
                            availableFilters = state.availableFilters,
                            selectedFilter = state.selectedFilter,
                            isSearchActive = state.isSearchActive,
                            searchButtonFocusRequester = searchButtonFocusRequester,
                            onFilterSelected = { filter -> viewModel.onFilterSelected(filter) }
                        )

                        if (visibleApps.isEmpty()) {
                            MessageContent(
                                message =
                                    stringResource(string.split_tunneling_screen_search_no_results)
                            )
                        } else {
                            AppsContent(
                                modifier = Modifier.focusRequester(appsFocusRequester),
                                apps = visibleApps,
                                excludedPackages = state.excludedPackages,
                                onAppSelected = { packageName, isChecked ->
                                    viewModel.onAppSelected(packageName, isChecked)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitTunnelingTopBar(
    modifier: Modifier = Modifier,
    searchButtonFocusRequester: FocusRequester,
    searchFieldFocusRequester: FocusRequester,
    isSearchActive: Boolean,
    searchQuery: String,
    areActionsEnabled: Boolean,
    onBackPressed: () -> Unit,
    onSearchToggled: (Boolean) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit
) {
    val clearButtonFocusRequester = remember { FocusRequester() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .focusGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton(onClick = onBackPressed)

        if (isSearchActive) {
            SearchTextField(
                modifier = Modifier
                    .padding(start = LocalDimens.current.normal)
                    .width(LocalDimens.current.extended.splitTunnelingSearchFieldWidth)
                    .focusRequester(searchFieldFocusRequester),
                searchQuery = searchQuery,
                onSearchQueryChanged = onSearchQueryChanged,
                onNext = { clearButtonFocusRequester.tryRequestFocus() }
            )

            IconButton(
                modifier = Modifier
                    .padding(start = LocalDimens.current.xSmall)
                    .focusRequester(clearButtonFocusRequester)
                    .focusProperties { right = FocusRequester.Cancel },
                colors = transparentIconButtonColors(),
                onClick = {
                    if (searchQuery.isNotEmpty()) {
                        onSearchQueryChanged("")
                    } else {
                        onSearchToggled(false)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = null,
                    tint = LocalColors.current.scheme.onBackground
                )
            }
        } else {
            Text(
                modifier = Modifier.padding(start = LocalDimens.current.normal),
                text = stringResource(string.settings_screen_label_split_tunneling_title),
                color = LocalColors.current.scheme.onBackground,
                fontFamily = displayNormalFontFamily,
                fontSize = LocalDimens.current.extended.splitTunnelingTitleFontSize
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (areActionsEnabled && !isSearchActive) {
            IconButton(
                modifier = Modifier.focusRequester(searchButtonFocusRequester),
                colors = transparentIconButtonColors(),
                onClick = { onSearchToggled(true) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = LocalColors.current.scheme.onBackground
                )
            }

            PopUpMenu(
                onSelectAll = onSelectAll,
                onDeselectAll = onDeselectAll
            )
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(
        modifier = Modifier
            .focusProperties { left = FocusRequester.Cancel },
        colors = transparentIconButtonColors(),
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.size(LocalDimens.current.large),
            painter = painterResource(id = R.drawable.ic_arrow_back),
            contentDescription = null,
            tint = LocalColors.current.scheme.onBackground
        )
    }
}

@Composable
private fun PopUpMenu(
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var wasMenuExpanded by remember { mutableStateOf(false) }

    val menuButtonFocusRequester = remember { FocusRequester() }
    val menuFocusRequester = remember { FocusRequester() }

    val menuOffset = with(LocalDensity.current) {
        IntOffset(x = 0, y = LocalDimens.current.extended.splitTunnelingMenuButtonSize.roundToPx())
    }

    LaunchedEffect(isMenuExpanded) {
        if (!isMenuExpanded && wasMenuExpanded) {
            menuButtonFocusRequester.tryRequestFocus()
        }

        wasMenuExpanded = isMenuExpanded
    }

    Box {
        IconButton(
            modifier = Modifier
                .focusRequester(menuButtonFocusRequester)
                .focusProperties { right = FocusRequester.Cancel },
            colors = transparentIconButtonColors(),
            onClick = { isMenuExpanded = true }
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = null,
                tint = LocalColors.current.scheme.onBackground
            )
        }

        if (isMenuExpanded) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = menuOffset,
                onDismissRequest = { isMenuExpanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Column(
                    modifier = Modifier
                        .width(LocalDimens.current.extended.splitTunnelingMenuWidth)
                        .background(
                            color = LocalColors.current.extendedColors.settingsBackgroundColor,
                            shape = RoundedCornerShape(LocalDimens.current.xxSmall)
                        )
                        .padding(vertical = LocalDimens.current.xxSmall)
                        .focusRequester(menuFocusRequester)
                        .focusGroup()
                ) {
                    LaunchedEffect(Unit) {
                        menuFocusRequester.tryRequestFocus()
                    }

                    PopUpMenuItem(
                        text = stringResource(string.split_tunneling_screen_menu_select_all),
                        onClick = {
                            onSelectAll()
                            isMenuExpanded = false
                        }
                    )

                    PopUpMenuItem(
                        text = stringResource(string.split_tunneling_screen_menu_deselect_all),
                        onClick = {
                            onDeselectAll()
                            isMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PopUpMenuItem(
    text: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.focusProperties { right = FocusRequester.Cancel },
        enabled = true,
        selected = false,
        onClick = onClick,
        shape = ListItemDefaults.shape(shape = RoundedCornerShape(LocalDimens.current.zero)),
        colors = ListItemDefaults.colors(
            focusedContainerColor =
            LocalColors.current.extendedColors.settingsItemFocusedContainerColor,
            focusedContentColor = LocalColors.current.extendedColors.contentPrimary
        ),
        headlineContent = {
            Text(
                text = text,
                fontSize = LocalDimens.current.extended.splitTunnelingActionLabelSize,
                fontFamily = displayNormalFontFamily
            )
        }
    )
}

@Composable
private fun SearchTextField(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onNext: () -> Unit
) {
    OutlinedTextField(
        modifier = modifier,
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        placeholder = {
            Text(
                text = stringResource(string.split_tunneling_screen_search_placeholder),
                color = LocalColors.current.scheme.onSurfaceVariant
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedTextColor = LocalColors.current.scheme.onBackground,
            unfocusedTextColor = LocalColors.current.scheme.onBackground,
            focusedContainerColor = LocalColors.current.scheme.background,
            unfocusedContainerColor = LocalColors.current.scheme.background,
            cursorColor = LocalColors.current.scheme.onBackground,
            focusedIndicatorColor = LocalColors.current.scheme.primaryContainer,
            unfocusedIndicatorColor = LocalColors.current.scheme.onBackground
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { onNext() })
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FiltersRow(
    modifier: Modifier = Modifier,
    availableFilters: List<SplitTunnelSettings.Filter>,
    selectedFilter: SplitTunnelSettings.Filter?,
    isSearchActive: Boolean,
    searchButtonFocusRequester: FocusRequester,
    onFilterSelected: (SplitTunnelSettings.Filter) -> Unit
) {
    val chipFocusedContainerColor = LocalColors.current.scheme.onSurface
    val chipFocusedContentColor = LocalColors.current.scheme.inverseOnSurface

    val filterChipColors = FilterChipDefaults.colors(
        focusedContainerColor = chipFocusedContainerColor,
        focusedContentColor = chipFocusedContentColor,
        selectedContainerColor = chipFocusedContainerColor,
        selectedContentColor = chipFocusedContentColor,
        focusedSelectedContainerColor = chipFocusedContainerColor,
        focusedSelectedContentColor = chipFocusedContentColor
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = LocalDimens.current.small,
            vertical = LocalDimens.current.normal
        ),
        horizontalArrangement = Arrangement.spacedBy(LocalDimens.current.normal)
    ) {
        itemsIndexed(availableFilters) { index, filter ->
            val filterLabel = when (filter) {
                SplitTunnelSettings.Filter.System ->
                    stringResource(string.split_tunneling_screen_system_apps_label)

                SplitTunnelSettings.Filter.User ->
                    stringResource(string.split_tunneling_screen_user_apps_label)
            }

            FilterChip(
                modifier = Modifier
                    .focusProperties {
                        if (index == FIRST_ITEM_INDEX) left = FocusRequester.Cancel
                        if (index == availableFilters.lastIndex) {
                            // The search button is replaced by the search field while the
                            // search is active, redirecting focus to a requester that is not
                            // attached to any composable crashes the focus search
                            right = if (isSearchActive) {
                                FocusRequester.Cancel
                            } else {
                                searchButtonFocusRequester
                            }
                        }
                    },
                selected = selectedFilter == filter,
                colors = filterChipColors,
                leadingIcon = if (selectedFilter == filter) {
                    {
                        Icon(
                            modifier = Modifier.size(LocalDimens.current.normal),
                            imageVector = Icons.Filled.Check,
                            contentDescription = null
                        )
                    }
                } else {
                    null
                },
                onClick = { onFilterSelected(filter) }
            ) {
                Text(
                    text = filterLabel,
                    fontSize = LocalDimens.current.extended.splitTunnelingActionLabelSize,
                    fontFamily = displayNormalFontFamily
                )
            }
        }
    }
}

@Composable
private fun AppsContent(
    modifier: Modifier = Modifier,
    apps: List<SplitTunnelSettings.App>,
    excludedPackages: Set<String>,
    onAppSelected: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = rememberLazyListState()
    ) {
        items(apps, key = { app -> app.packageName }) { app ->
            val isChecked = excludedPackages.contains(app.packageName)

            AppListItem(
                app = app,
                isChecked = isChecked,
                onClick = { onAppSelected(app.packageName, !isChecked) }
            )
        }
    }
}

@Composable
private fun AppListItem(
    app: SplitTunnelSettings.App,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.focusProperties { right = FocusRequester.Cancel },
        enabled = true,
        selected = isChecked,
        onClick = onClick,
        shape = ListItemDefaults.shape(shape = RoundedCornerShape(LocalDimens.current.zero)),
        colors = ListItemDefaults.colors(
            focusedContainerColor =
            LocalColors.current.extendedColors.settingsItemFocusedContainerColor,
            focusedContentColor = LocalColors.current.extendedColors.contentPrimary
        ),
        leadingContent = {
            AppIcon(
                appName = app.name,
                packageName = app.packageName,
                modifier = Modifier.size(
                    size = LocalDimens.current.extended.splitTunnelingAppIconSize
                )
            )
        },
        headlineContent = {
            Text(
                text = app.name,
                fontSize = LocalDimens.current.extended.splitTunnelingItemTitleSize,
                fontWeight = FontWeight.Bold,
                fontFamily = displayNormalFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = app.packageName,
                fontSize = LocalDimens.current.extended.splitTunnelingItemDescriptionSize,
                fontFamily = displayThinFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            Checkbox(
                checked = isChecked,
                onCheckedChange = null
            )
        }
    )
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(LocalDimens.current.extended.splitTunnelingProgressIndicatorSize),
            color = LocalColors.current.extendedColors.connectingProgressIndicatorColor
        )
    }
}

@Composable
private fun MessageContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = LocalColors.current.scheme.onSurfaceVariant,
            fontSize = LocalDimens.current.extended.splitTunnelingMessageFontSize,
            fontFamily = displayNormalFontFamily
        )
    }
}

@Composable
private fun transparentIconButtonColors() = IconButtonDefaults.colors(
    containerColor = Color.Transparent,
    focusedContainerColor = LocalColors.current.scheme.inverseOnSurface
)
