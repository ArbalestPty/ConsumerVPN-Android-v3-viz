package com.wlvpn.consumervpn.presentation.settings

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import com.wlvpn.consumervpn.R.drawable
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.LocalDimens
import com.wlvpn.consumervpn.presentation.ui.theme.extended

@Composable
fun WebViewDialog(
    modifier: Modifier,
    url: String,
    onBackPressed: () -> Unit
) {
    val requester = remember { FocusRequester() }
    val context = LocalContext.current
    val webView = remember { WebView(context) }
    var canGoBack by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    BackHandler(true) {
        if (canGoBack) {
            webView.goBack()
        } else {
            onBackPressed()
        }
    }

    LaunchedEffect(webView) {
        webView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    isLoading = true
                    canGoBack = view?.canGoBack() ?: false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    isLoading = false
                }
            }
            loadUrl(url)
        }
    }

    Column(
        modifier = modifier
            .focusRequester(requester)
            .background(LocalColors.current.scheme.background)
    ) {
        Row(Modifier.height(LocalDimens.current.xxxWide)) {
            IconButton(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(LocalDimens.current.xSmall),
                colors = IconButtonDefaults.colors(
                    containerColor = Color.Transparent,
                    focusedContainerColor = LocalColors.current.scheme.inverseOnSurface
                ),
                onClick = {
                    onBackPressed()
                }) {
                Icon(
                    modifier = Modifier.padding(LocalDimens.current.xSmall),
                    painter = painterResource(drawable.ic_back),
                    contentDescription = null,
                    tint = LocalColors.current.extendedColors.contentPrimary
                )
            }
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(LocalDimens.current.xSmall),
                text = url,
                color = LocalColors.current.extendedColors.contentSecondary,
                fontSize = LocalDimens.current.extended.webViewDialogUrlFontSize
            )

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(
                modifier = Modifier
                    .padding(LocalDimens.current.xxSmall)
                    .align(Alignment.CenterVertically),
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    color = LocalColors.current.scheme.primaryContainer
                )
            }
        }
        AndroidView(
            factory = { webView },
            modifier = Modifier
                .fillMaxWidth()
        )
    }

    LaunchedEffect(Unit) {
        requester.requestFocus()
    }
}