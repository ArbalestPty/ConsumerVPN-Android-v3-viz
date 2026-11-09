package com.wlvpn.consumervpn.presentation.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.Text
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.LocalDimens
import com.wlvpn.consumervpn.presentation.ui.theme.extended

@Composable
fun AlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    description: String = "",
    dismissButtonTitle: String,
    confirmButtonTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onBackPressed: () -> Unit
) {
    val requester = FocusRequester()

    BackHandler(true) {
        onBackPressed()
    }

    Row(
        modifier
            .background(LocalColors.current.extendedColors.settingsBackgroundColor)
            .focusRequester(requester)
    ) {

        Column(
            modifier = Modifier
                .weight(2f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                modifier = Modifier
                    .padding(start = LocalDimens.current.normal, end = LocalDimens.current.normal)
                    .align(Alignment.CenterHorizontally),
                letterSpacing = LocalDimens.current.extended.settingsDialogTitleSpacing,
                lineHeight = LocalDimens.current.extended.settingsDialogTitleLineHeight,
                text = title,
                textAlign = TextAlign.Center,
                fontSize = LocalDimens.current.extended.settingsDialogTitleSize,
                color = LocalColors.current.extendedColors.contentPrimary
            )
            if (description.isNotBlank()) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(
                            top = LocalDimens.current.normal,
                            start = LocalDimens.current.xxLarge,
                            end = LocalDimens.current.xxLarge
                        ),
                    text = description,
                    fontSize = LocalDimens.current.extended.settingsDialogDescriptionSize,
                    textAlign = TextAlign.Justify,
                    color = LocalColors.current.extendedColors.contentSecondary
                )
            }
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
                item {
                    AlertDialogButton(
                        modifier = Modifier,
                        title = confirmButtonTitle
                    ) {
                        onConfirm()
                    }

                    AlertDialogButton(
                        modifier = Modifier.padding(top = LocalDimens.current.normal),
                        title = dismissButtonTitle
                    ) {
                        onDismiss()
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) { requester.requestFocus() }
}

@Composable
private fun AlertDialogButton(
    modifier: Modifier,
    title: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = modifier.height(LocalDimens.current.xxxWide),
        selected = false,
        shape = ListItemDefaults.shape(
            shape = RoundedCornerShape(LocalDimens.current.zero)
        ),
        enabled = true,
        onClick = { onClick() },
        colors = ListItemDefaults.colors(
            containerColor =
            LocalColors.current.extendedColors.settingsBackgroundColor,
            focusedContainerColor =
            LocalColors.current.extendedColors.settingsDialogButtonFocusedColor
        ),
        headlineContent = {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    text = title.uppercase()
                )
            }
        }
    )
}