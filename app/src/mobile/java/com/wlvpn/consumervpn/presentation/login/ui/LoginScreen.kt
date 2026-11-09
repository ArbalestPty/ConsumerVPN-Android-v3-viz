package com.wlvpn.consumervpn.presentation.login.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.presentation.login.LoginViewModel
import com.wlvpn.consumervpn.presentation.login.validation.LoginEvent
import com.wlvpn.consumervpn.presentation.login.view.components.LoginErrorLabel
import com.wlvpn.consumervpn.presentation.login.view.components.LoginTextField
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.isAppInDarkTheme
import com.wlvpn.consumervpn.presentation.util.isTablet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel,
    onBackPressed: () -> Unit,
    onLogin: () -> Unit,
    onSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    val orientation = LocalConfiguration.current.orientation
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current

    val state by viewModel.loginEvent.observeAsState()

    var isLoading by remember { mutableStateOf(false) }
    var displayMessage by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    var interactionStarted by rememberSaveable { mutableStateOf(false) }

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

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                LocalColors.current.extendedColors.gradientStartEnd,
                                LocalColors.current.extendedColors.gradientCenter,
                                LocalColors.current.extendedColors.gradientStartEnd
                            )
                        )
                    )
            ) {
                val isDarkTheme = isAppInDarkTheme()
                val imageShape =
                    if (isDarkTheme) {
                        R.drawable.ic_bg_shapes_dark
                    } else {
                        R.drawable.ic_bg_shapes_light
                    }.run { painterResource(id = this) }

                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (isTablet()) {
                        Image(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart),
                            painter = imageShape,
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth
                        )
                    } else {
                        Image(
                            modifier = Modifier
                                .height(280.dp)
                                .fillMaxWidth()
                                .align(Alignment.BottomStart),
                            painter = imageShape,
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth
                        )
                    }
                } else {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart),
                        painter = imageShape,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    val localFocusManager = LocalFocusManager.current

                    var username by rememberSaveable { mutableStateOf("") }
                    var password by rememberSaveable { mutableStateOf("") }

                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        Spacer(modifier = Modifier.height(64.dp))
                    }

                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = dimensionResource(id = R.dimen.spacing_normal))
                            .widthIn(
                                min = dimensionResource(id = R.dimen.login_header_min_width),
                                max = dimensionResource(id = R.dimen.login_header_max_width)
                            )
                            .height(dimensionResource(id = R.dimen.login_header_height)),
                        painter = painterResource(
                            id = if (isDarkTheme) {
                                R.drawable.brand_logo_dark_theme
                            } else {
                                R.drawable.brand_logo_light_theme
                            }
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (displayMessage) {
                        LoginErrorLabel(
                            modifier = Modifier,
                            text = message,
                            textAlign = TextAlign.Center
                        )
                    }

                    val emptyUsernameError = state == LoginEvent.EmptyUsername

                    LoginTextField(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .padding(horizontal = 16.dp),
                        value = username,
                        label = stringResource(id = R.string.login_screen_username_hint),
                        showError = emptyUsernameError || displayMessage,
                        trailingIcon = { },
                        leadingIcon = {
                            IconButton(
                                onClick = {},
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_account),
                                    contentDescription = null
                                )
                            }
                        },
                        onAction = { },
                        onValueChange = { text ->
                            username = text
                        },
                    )

                    if (emptyUsernameError) {
                        LoginErrorLabel(
                            modifier = Modifier
                                .padding(horizontal = 16.dp),
                            text = stringResource(R.string.login_screen_validation_username_empty)
                        )
                    }

                    val emptyPasswordError = state == LoginEvent.EmptyPassword
                    var passwordVisible by rememberSaveable { mutableStateOf(false) }

                    LoginTextField(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .padding(horizontal = 16.dp),
                        value = password,
                        label = stringResource(id = R.string.login_screen_password_hint),
                        showError = emptyPasswordError || displayMessage,
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            // Please provide localized description for accessibility services
                            val description = if (passwordVisible) {
                                stringResource(id = R.string.accessibility_password_hide)
                            } else {
                                stringResource(id = R.string.accessibility_password_hide)
                            }

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, description)
                            }
                        },
                        leadingIcon = {
                            IconButton(
                                onClick = {},
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lock),
                                    contentDescription = null
                                )
                            }
                        },
                        onAction = { },
                        onValueChange = { text ->
                            password = text
                        },
                    )

                    if (emptyPasswordError) {
                        LoginErrorLabel(
                            modifier = Modifier
                                .padding(horizontal = 16.dp),
                            text = stringResource(R.string.login_screen_validation_password_empty)
                        )
                    }

                    val forgotPasswordInteractionSource = remember { MutableInteractionSource() }
                    Text(
                        modifier = Modifier
                            .align(alignment = Alignment.End)
                            .clickable(
                                interactionSource = forgotPasswordInteractionSource,
                                indication = ripple()
                            ) {
                                displayMessage = false
                                onForgotPassword()
                            }
                            .padding(horizontal = 16.dp),
                        text = AnnotatedString(
                            stringResource(id = R.string.login_screen_forgot_password)
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.End,
                            color = LocalColors.current.extendedColors.clickableTextColor
                        ),
                    )

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 24.dp),
                        content = {
                            Text(text = stringResource(R.string.login_screen_button_login))
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        onClick = {
                            displayMessage = false
                            localFocusManager.clearFocus()
                            viewModel.login(username, password)
                            interactionStarted = true
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(end = 4.dp),
                            text = stringResource(id = R.string.login_screen_no_account),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        val signUpInteractionSource = remember { MutableInteractionSource() }
                        Text(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = signUpInteractionSource,
                                    indication = ripple()
                                ) { onSignUp() }
                                .padding(horizontal = 16.dp),
                            text = AnnotatedString(
                                stringResource(id = R.string.login_screen_sign_up)
                            ),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = LocalColors.current.extendedColors.clickableTextColor
                            ),
                        )
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .padding(paddingValues)
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
    )
}
