package com.wlvpn.consumervpn.presentation.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.RadioButton
import androidx.tv.material3.Text
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.LocalDimens
import com.wlvpn.consumervpn.presentation.ui.theme.displayNormalFontFamily
import com.wlvpn.consumervpn.presentation.ui.theme.extended

@Composable
fun <T> RadioButtonListDialog(
    modifier: Modifier,
    title: String = "",
    items: List<T> = emptyList(),
    selectedItem: T,
    onBackPressed: () -> Unit = {},
    itemTitle: @Composable (T) -> String,
    onClick: (T) -> Unit
) {
    BackHandler(true) {
        onBackPressed()
    }

    val requester = FocusRequester()

    var currentSelected by remember { mutableStateOf(selectedItem) }

    Row(
        modifier
            .background(
                LocalColors.current.extendedColors.settingsBackgroundColor
            )
            .focusRequester(requester)
    ) {

        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxSize()
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                letterSpacing = LocalDimens.current.extended.settingsDialogTitleSpacing,
                lineHeight = LocalDimens.current.extended.settingsDialogTitleLineHeight,
                text = title,
                textAlign = TextAlign.Center,
                fontSize = LocalDimens.current.extended.settingsDialogTitleSize,
                color = LocalColors.current.extendedColors.contentPrimary
            )
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(LocalColors.current.scheme.background)
                .weight(1.5f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = LocalDimens.current.xxWide, end = LocalDimens.current.xxWide)
                    .background(LocalColors.current.scheme.background)

            ) {
                items(items) { item ->

                    RadioButtonItem(
                        modifier = Modifier.padding(top = LocalDimens.current.normal),
                        title = itemTitle(item),
                        isChecked = currentSelected == item,
                        onClick = {
                            currentSelected = item
                            onClick(item)
                        }
                    )
                }
            }
        }
    }
    LaunchedEffect(Unit) { requester.requestFocus() }
}

@Composable
fun RadioButtonItem(
    modifier: Modifier = Modifier,
    title: String = "",
    isChecked: Boolean = false,
    onClick: (Boolean) -> Unit = {}
) {

    ListItem(
        modifier = modifier,
        enabled = true,
        selected = false,
        onClick = { onClick(isChecked.not()) },
        colors = ListItemDefaults.colors(
            focusedContainerColor =
            LocalColors.current.extendedColors.settingsDialogBackgroundColor,
            focusedContentColor = LocalColors.current.extendedColors.contentPrimary
        ),
        leadingContent = {
            RadioButton(
                selected = isChecked,
                onClick = {
                    // No - op
                }
            )
        },
        headlineContent = {
            Text(
                modifier = Modifier,
                text = title,
                fontSize = LocalDimens.current.extended.settingsDialogRadioTextSize,
                color = LocalColors.current.extendedColors.contentPrimary,
                fontWeight = FontWeight.Bold,
                fontFamily = displayNormalFontFamily
            )
        }
    )
}