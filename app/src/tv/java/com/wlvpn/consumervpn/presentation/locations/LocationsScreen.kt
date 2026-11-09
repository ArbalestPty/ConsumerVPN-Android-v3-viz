package com.wlvpn.consumervpn.presentation.locations

import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.domain.value.ServerLocation
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.LocalDimens
import com.wlvpn.consumervpn.presentation.ui.theme.displayNormalFontFamily
import com.wlvpn.consumervpn.presentation.ui.theme.extended
import com.wlvpn.consumervpn.presentation.util.getUriForFlag

const val LOCATION_FIRST_ITEM_INDEX = 0

@Composable
inline fun LocationsScreen(
    viewModel: LocationsViewModel,
    noinline onBackPressed: () -> Unit,
    crossinline onConnect: () -> Unit
) {
    val locationCityEvent by viewModel.locationCityStateFlow.collectAsState()
    val locationCountryEvent by viewModel.locationCountryStateFlow.collectAsState()
    val saveLocationEvent by viewModel.saveLocationStateFlow.collectAsState()

    var cities by remember { mutableStateOf<List<ServerLocation.City>>(emptyList()) }
    var countries by remember { mutableStateOf<List<ServerLocation.Country>>(emptyList()) }

    when (locationCityEvent) {
        is LocationsEvent.CityLocationListLoaded -> {
            cities = (locationCityEvent as LocationsEvent.CityLocationListLoaded).cityLocationList
        }

        else -> {}
    }
    when (locationCountryEvent) {
        is LocationsEvent.CountryLocationListLoaded -> {
            countries = (locationCountryEvent as
                    LocationsEvent.CountryLocationListLoaded).countryLocationList
        }

        else -> {}
    }

    if (saveLocationEvent is LocationsEvent.SelectedLocationSaved) {
        // Fire the navigation once per save event instead of on every recomposition
        LaunchedEffect(saveLocationEvent) {
            onConnect()
        }
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

            TopTitleBar(onBackPressed)

            LazyColumn {
                //fastest available button content
                item {
                    FastestAvailableContent {
                       viewModel.saveServerLocationToConnect(ServerLocation.Fastest)
                    }
                }

                //cities content as a row
                item {
                    CitiesContent(
                        cities,
                        connect = { viewModel.saveServerLocationToConnect(it) }
                    )
                }

                //countries content as a row
                item {
                    CountriesContent(
                        countries,
                        connect = { viewModel.saveServerLocationToConnect(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun TopTitleBar(onBackPressed: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            colors = IconButtonDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = LocalColors.current.scheme.inverseOnSurface
            ),
            onClick = { onBackPressed() }) {
            Icon(
                modifier = Modifier.size(LocalDimens.current.large),
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "",
                tint = LocalColors.current.scheme.onBackground
            )
        }

        //screen title
        Text(
            modifier = Modifier
                .padding(start = LocalDimens.current.normal),
            text = stringResource(id = R.string.locations_screen_label_title),
            color = LocalColors.current.scheme.onBackground,
            fontFamily = displayNormalFontFamily,
            fontSize = LocalDimens.current.extended.locationsScreenTitleFontSize
        )
    }
}

@Composable
inline fun FastestAvailableContent(crossinline onConnect: () -> Unit) {
    var isFastestAvailableFocused by remember { mutableStateOf(false) }

    Text(
        modifier = Modifier
            .padding(
                top = LocalDimens.current.normal,
                bottom = LocalDimens.current.xSmall
            ),
        text = stringResource(id = R.string.locations_screen_recommended_title),
        color = LocalColors.current.scheme.onBackground,
        fontFamily = displayNormalFontFamily
    )

    Button(
        modifier = Modifier
            .onFocusChanged { focusState ->
                isFastestAvailableFocused = focusState.isFocused
            },
        colors = ButtonDefaults.colors(
            containerColor = LocalColors.current.scheme.primaryContainer,
            focusedContainerColor = LocalColors.current.scheme.primaryContainer
        ),
        shape = ButtonDefaults.shape(shape = RoundedCornerShape(LocalDimens.current.zero)),
        onClick = { onConnect() }
    ) {
        Row(
            modifier = Modifier
                .width(LocalDimens.current.extended.locationsScreenFastestAvailableBoxPadding)
                .height(height = LocalDimens.current.xxxWide),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier
                    .widthIn(
                        min = LocalDimens.current.large,
                        max = LocalDimens.current.large
                    ),
                painter = painterResource(id = R.drawable.ic_tv_locations),
                contentDescription = "",
                colorFilter = ColorFilter.tint(LocalColors.current.extendedColors.contentPrimary)
            )
            Text(
                modifier = Modifier
                    .padding(
                        start = LocalDimens.current.xSmall
                    ), text = stringResource(id = R.string.locations_screen_fastest_label_title),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = LocalColors.current.extendedColors.contentPrimary,
                    fontSize = LocalDimens.current.extended.locationsScreenLabelFontSize,
                    fontWeight = FontWeight.Bold,
                    lineHeight =
                    LocalDimens.current.extended.locationsScreenFastestAvailableLabelLineHeight
                )
            )
        }
    }
}

@Composable
fun CitiesContent(
    citiesLocationList: List<ServerLocation.City>,
    connect: (location: ServerLocation) -> Unit
) {
    Text(
        modifier = Modifier
            .padding(
                top = LocalDimens.current.large,
                bottom = LocalDimens.current.normal
            ),
        text = stringResource(id = R.string.locations_screen_cities_label_title),
        color = LocalColors.current.scheme.onBackground,
        fontFamily = displayNormalFontFamily
    )

    LazyRow(
        state = rememberLazyListState()
    ) {

        itemsIndexed(citiesLocationList) { index, city ->
            val rowFocusRequester = remember { FocusRequester() }

            LocationTvContent(
                modifier = Modifier
                    .size(
                        width = LocalDimens.current.extended.locationsScreenRowItemSize,
                        height = LocalDimens.current.extended.locationsScreenRowItemSize
                    )
                    .focusRequester(rowFocusRequester)
                    .onKeyEvent { keyEvent ->
                        index == LOCATION_FIRST_ITEM_INDEX &&
                                keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    }
                ,
                text = city.name,
                country = city.country
            ) {
                rowFocusRequester.requestFocus()
                connect(city)
            }
        }
    }
}

@Composable
fun CountriesContent(
    countryLocationList: List<ServerLocation.Country>,
    connect: (location: ServerLocation) -> Unit
) {
    Text(
        modifier = Modifier
            .padding(
                top = LocalDimens.current.large,
                bottom = LocalDimens.current.normal
            ),
        text = stringResource(id = R.string.locations_screen_countries_label_title),
        color = LocalColors.current.scheme.onBackground,
        fontFamily = displayNormalFontFamily
    )

    LazyRow(
        state = rememberLazyListState()
    ) {
        itemsIndexed(countryLocationList) { index, country ->
            val rowFocusRequester = remember { FocusRequester() }

            LocationTvContent(
                modifier = Modifier
                    .size(LocalDimens.current.extended.locationsScreenRowItemSize)
                    .focusRequester(rowFocusRequester)
                    .onKeyEvent { keyEvent ->
                        index == LOCATION_FIRST_ITEM_INDEX &&
                                keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    },
                text = country.name,
                country = country
            ) {
                rowFocusRequester.requestFocus()
                connect(country)
            }
        }
    }
}

@Composable
fun LocationTvContent(
    modifier: Modifier = Modifier,
    text: String,
    country: ServerLocation.Country,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .padding(start = LocalDimens.current.xxSmall, end = LocalDimens.current.xxSmall),
        colors = CardDefaults.colors(
            containerColor = LocalColors.current.scheme.secondaryContainer,
            focusedContainerColor = LocalColors.current.extendedColors.locationRowItemHighlightColor
        ),
        scale = CardDefaults.scale(focusedScale = 1.2f),
        shape = CardDefaults.shape(RoundedCornerShape(LocalDimens.current.zero)),
        border = CardDefaults.border(focusedBorder = Border.None),
        onClick = { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Flag(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f), country = country
            )
            Text(
                modifier = Modifier
                    .weight(1.5f)
                    .padding(LocalDimens.current.xSmall),
                overflow = TextOverflow.Ellipsis,
                text = text,
                color = LocalColors.current.scheme.onBackground,
                fontSize = LocalDimens.current.extended.locationsScreenLabelFontSize,
                fontFamily = displayNormalFontFamily,
                lineHeight =
                LocalDimens.current.extended.locationsScreenFastestAvailableLabelLineHeight
            )
        }

    }
}

@Composable
fun Flag(modifier: Modifier, country: ServerLocation.Country) {
    val context = LocalContext.current
    AsyncImage(
        model = country.getUriForFlag(context = context),
        contentDescription = null, contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.consumer_icon),
        modifier = modifier
            .background(LocalColors.current.scheme.surfaceVariant)
            .clip(RectangleShape)
    )
}