package com.wlvpn.consumervpn.presentation.login.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.presentation.login.LoginViewModel
import com.wlvpn.consumervpn.presentation.login.validation.LoginEvent
import com.wlvpn.consumervpn.presentation.login.view.components.LoginErrorLabel
import com.wlvpn.consumervpn.presentation.login.view.components.LoginTextField
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.LocalDimens
import com.wlvpn.consumervpn.presentation.ui.theme.extended

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onBackPressed: () -> Unit,
    onLogin: () -> Unit,
    onSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    val orientation = LocalConfiguration.current.orientation
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val state by viewModel.loginEvent.observeAsState()

    var isLoading by remember { mutableStateOf(false) }
    var displayMessage by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    var interactionStarted by rememberSaveable { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    //scroll state for the Column
    val scrollState = rememberScrollState()

    val (
        usernameFocus,
        passwordFocus,
        togglePasswordFocus,
        loginButtonFocus,
        forgotPasswordFocus
    ) = remember { FocusRequester.createRefs() }

    /* To handle on back pressed manually */
    BackHandler(true) {
        onBackPressed()
    }

    LaunchedEffect(key1 = state) {
        if (interactionStarted) {
            when (state) {
                LoginEvent.NoNetwork -> {
                    displayMessage = true
                    message = context.getString(
                        R.string.not_connected_error
                    )
                    isLoading = false
                    interactionStarted = false
                }

                LoginEvent.InvalidCredentials -> {
                    displayMessage = true
                    message = context.getString(
                        R.string.login_screen_validation_invalid_credentials
                    )
                    isLoading = false
                    interactionStarted = false
                }

                LoginEvent.TooManyAttempts -> {
                    displayMessage = true
                    message = context.getString(
                        R.string.login_screen_validation_too_many_attempts
                    )
                    isLoading = false
                    interactionStarted = false
                }

                is LoginEvent.UnableToLogin -> {
                    displayMessage = true
                    message = context.getString(
                        R.string.login_screen_validation_unable_to_login
                    )
                    isLoading = false
                    interactionStarted = false
                }

                is LoginEvent.Error -> {
                    displayMessage = true
                    message = context.getString(
                        R.string.login_screen_validation_unable_to_login
                    )
                    isLoading = false
                    interactionStarted = false
                }

                LoginEvent.Success -> {
                    onLogin()
                }

                LoginEvent.ExecutingLogin -> {
                    isLoading = true
                }

                LoginEvent.EmptyPassword, LoginEvent.EmptyUsername -> {
                    // No - Op
                    isLoading = false
                    interactionStarted = false
                }

                null -> {
                    // No - Op
                    isLoading = false
                    interactionStarted = false
                }
            }
        }
    }

    Box (
        modifier = Modifier.background(
            color = LocalColors.current.scheme.background
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = LocalDimens.current.normal)
                .verticalScroll(scrollState)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(LocalDimens.current.normal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            var username by rememberSaveable { mutableStateOf("") }
            var password by rememberSaveable { mutableStateOf("") }

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                Spacer(
                    modifier = Modifier.height(
                        height = LocalDimens.current.extended.loginScreenPortraitPadding
                    )
                )
            }

            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = LocalDimens.current.extended.loginScreenLogoPadding)
                    .widthIn(
                        min = dimensionResource(id = R.dimen.login_header_min_width),
                        max = dimensionResource(id = R.dimen.login_header_max_width)
                    )
                    .height(dimensionResource(id = R.dimen.login_header_height)),
                painter = painterResource(id = R.drawable.brand_logo_dark_theme),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.weight(1f))

            if (displayMessage) {
                LoginErrorLabel(
                    modifier = Modifier
                        .padding(
                            horizontal = LocalDimens.current.extended.loginScreenLabelPadding
                        ),
                    horizontalArrangement = Arrangement.Center,
                    text = message,
                    textAlign = TextAlign.Center
                )
            }

            val emptyUsernameError = state == LoginEvent.EmptyUsername

            LoginTextField(
                modifier = Modifier
                    .padding(top = LocalDimens.current.normal)
                    .padding(horizontal = LocalDimens.current.extended.loginScreenLabelPadding)
                    .focusRequester(usernameFocus)
                    .focusProperties {
                        right = forgotPasswordFocus
                        left = usernameFocus
                        down = passwordFocus
                    },
                value = username,
                label = stringResource(id = R.string.login_screen_username_hint),
                showError = emptyUsernameError || displayMessage,
                trailingIcon = { },
                leadingIcon = {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(LocalDimens.current.large)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_account),
                            contentDescription = null
                        )
                    }
                },
                onValueChange = { text ->
                    username = text
                },
                onAction = {
                    keyboardController?.hide()
                }
            )

            if (emptyUsernameError) {
                LoginErrorLabel(
                    modifier = Modifier
                        .padding(horizontal = LocalDimens.current.extended.loginScreenLabelPadding),
                    text = stringResource(R.string.login_screen_validation_username_empty),
                    textAlign = TextAlign.Center
                )
            }

            val emptyPasswordError = state == LoginEvent.EmptyPassword
            var passwordVisible by rememberSaveable { mutableStateOf(false) }
            Box(
              modifier = Modifier
                  .padding(top = LocalDimens.current.normal)
                  .padding(horizontal = LocalDimens.current.extended.loginScreenLabelPadding),
            ){
                LoginTextField(
                    modifier = Modifier
                        .focusRequester(passwordFocus)
                        .focusProperties {
                            right = togglePasswordFocus
                            left = usernameFocus
                            down = forgotPasswordFocus
                        },
                    value = password,
                    label = stringResource(id = R.string.login_screen_password_hint),
                    showError = emptyPasswordError || displayMessage,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {

                    },
                    leadingIcon = {
                        IconButton(
                            onClick = {},
                            modifier = Modifier.size(LocalDimens.current.large)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = null
                            )
                        }
                    },
                    onValueChange = { text ->
                        password = text
                    },
                    onAction = {
                        keyboardController?.hide()
                    }
                )

                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = LocalDimens.current.xSmall, top = LocalDimens.current.xSmall)
                        .focusRequester(togglePasswordFocus)
                        .focusProperties {
                            right = forgotPasswordFocus
                            left = passwordFocus
                            down = forgotPasswordFocus
                        },
                    colors = IconButtonDefaults.colors(
                        containerColor = LocalColors.current.scheme.background,
                        focusedContainerColor =
                            LocalColors.current.extendedColors.inputFocusedContainer
                    ),
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {
                    Icon(
                        imageVector =
                        if (passwordVisible) Filled.Visibility
                        else Filled.VisibilityOff, "",
                        tint = LocalColors.current.scheme.onBackground
                    )
                }
            }

            if (emptyPasswordError) {
                LoginErrorLabel(
                    modifier = Modifier
                        .padding(horizontal = LocalDimens.current.extended.loginScreenLabelPadding),
                    text = stringResource(R.string.login_screen_validation_password_empty)
                )
            }

            Box(
                modifier = Modifier
                    .padding(
                        start = LocalDimens.current.extended.loginScreenLabelPadding,
                        end = LocalDimens.current.extended.loginScreenLabelPadding
                    )
                    .widthIn(
                        min = LocalDimens.current.extended.loginScreenLabelPadding,
                        max = LocalDimens.current.extended.loginScreenForgotPasswordButtonHeight
                    )
                    .focusRequester(forgotPasswordFocus)
                    .focusProperties {
                        right = forgotPasswordFocus
                        left = togglePasswordFocus
                        down = loginButtonFocus
                    }
                    .fillMaxWidth()
            ) {
                Button(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = {
                        displayMessage = false
                        onForgotPassword()
                    },
                    colors = ButtonDefaults.colors(
                        focusedContainerColor =
                        LocalColors.current.extendedColors.backgroundForgotPasswordHighlightedColor,
                        containerColor = Color.Transparent
                    ),
                    shape =
                    ButtonDefaults.shape(shape = RoundedCornerShape(LocalDimens.current.zero))
                ) {
                    Text(
                        text =
                        stringResource(id = R.string.login_screen_forgot_password).uppercase(),
                        color = LocalColors.current.scheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            ButtonWithLoadingIndicator(
                isLoading = isLoading,
                text = stringResource(R.string.login_screen_button_login).uppercase(),
                modifier = Modifier
                    .focusRequester(loginButtonFocus)
                    .fillMaxWidth()
                    .padding(horizontal = LocalDimens.current.extended.loginScreenLabelPadding),
                onClick = {
                    displayMessage = false
                    focusManager.clearFocus()
                    viewModel.login(username, password)
                    interactionStarted = true
                    keyboardController?.hide()
                }
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ButtonWithLoadingIndicator(
    isLoading: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(LocalDimens.current.xxSmall),
        tonalElevation = LocalDimens.current.xxSmall,
        colors = SurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(LocalDimens.current.extended.loginScreenButtonHeight)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if(isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(LocalDimens.current.xxLarge),
                    color = LocalColors.current.scheme.onBackground,
                    strokeWidth = LocalDimens.current.extended.loginScreenCircularIndicatorStrokeWidth
                )
            } else {
                Button(
                    modifier = Modifier.fillMaxSize(),
                    onClick = {
                        onClick()
                    },
                    shape =  ButtonDefaults.shape(
                        shape = RoundedCornerShape(LocalDimens.current.xxSmall)
                    ),
                    colors = ButtonDefaults.colors(
                        containerColor = LocalColors.current.scheme.primaryContainer,
                        focusedContainerColor = LocalColors.current.scheme.primaryContainer,
                        pressedContentColor = LocalColors.current.scheme.onPrimaryContainer,
                        focusedContentColor = LocalColors.current.scheme.onPrimaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text, textAlign = TextAlign.Center,fontWeight = FontWeight.Bold)
                    }
                }
            }

        }
    }
}

