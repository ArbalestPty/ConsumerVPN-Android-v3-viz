package com.wlvpn.consumervpn.presentation.login.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.presentation.ui.component.QrCode
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.LocalDimens
import com.wlvpn.consumervpn.presentation.ui.theme.extended

@Composable
fun ForgotPasswordScreen(
    onBackPressed: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    /* To handle on back pressed manually */
    BackHandler(true) {
        onBackPressed()
    }

    Box (
        modifier = Modifier.background(
            color = LocalColors.current.scheme.background
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    vertical = LocalDimens.current.normal,
                    horizontal = LocalDimens.current.normal
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(LocalDimens.current.normal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.forgot_password_screen_title),
                color = LocalColors.current.scheme.onBackground,
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                modifier = Modifier
                    .padding(top = LocalDimens.current.normal),
                text = stringResource(
                    id = R.string.forgot_password_screen_label_description,
                    stringResource(id = R.string.app_name)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = LocalColors.current.scheme.onBackground,
            )

            Text(
                text = stringResource(R.string.url_forgot_password),
                style = MaterialTheme.typography.bodySmall,
                color = LocalColors.current.scheme.onBackground,
            )

            Spacer(
                modifier = Modifier.padding(top = LocalDimens.current.normal)
            )

            //QR code
            QrCode(
                content = stringResource(id = R.string.url_forgot_password),
                contentDescription = stringResource(
                    id = R.string.forgot_password_screen_qr_code_content_description
                ),
                modifier = Modifier.size(
                    size = LocalDimens.current.extended.forgotPasswordScreenQrCodeImageSize
                )
            )

            Spacer(
                modifier = Modifier.padding(top = LocalDimens.current.normal)
            )

            Button(
                modifier = Modifier
                    .size(
                        height = LocalDimens.current.xxLarge,
                        width = LocalDimens.current.extended.forgotPasswordScreenButtonSize
                    ),
                shape = ButtonDefaults.shape(shape = RectangleShape),
                colors =  ButtonDefaults.colors(
                    containerColor = LocalColors.current.scheme.secondaryContainer,
                    focusedContainerColor = LocalColors.current.extendedColors.forgotPasswordCloseHighlightedButtonColor,
                    pressedContainerColor = LocalColors.current.extendedColors.forgotPasswordCloseHighlightedButtonColor,
                    pressedContentColor = LocalColors.current.scheme.onSecondaryContainer,
                    contentColor = LocalColors.current.scheme.onSecondaryContainer,
                    focusedContentColor = LocalColors.current.scheme.onSecondaryContainer,

                ),
                onClick = {
                    onBackPressed()
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center

                ) {
                    Text (
                        text = stringResource(R.string.generic_button_close).uppercase(),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}