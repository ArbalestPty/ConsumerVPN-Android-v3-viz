package com.wlvpn.consumervpn.presentation.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import kotlin.math.floor

private const val QUIET_ZONE_MODULES = 2

/**
 * Draws a QR code for [content], so the code always matches the configured URL and no QR image
 * has to be made for each brand.
 */
@Composable
fun QrCode(
    content: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    foreground: Color = LocalColors.current.extendedColors.qrCodeForeground,
    background: Color = LocalColors.current.extendedColors.qrCodeBackground,
) {
    val matrix = remember(content) { encodeQrCode(content) }

    Canvas(modifier = modifier.semantics { this.contentDescription = contentDescription }) {
        drawRect(color = background)

        // Whole-pixel modules avoid hairline gaps between neighbouring squares
        val moduleSize = floor(minOf(size.width / matrix.width, size.height / matrix.height))
        val origin = Offset(
            x = (size.width - moduleSize * matrix.width) / 2,
            y = (size.height - moduleSize * matrix.height) / 2
        )

        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (matrix[x, y]) {
                    drawRect(
                        color = foreground,
                        topLeft = origin + Offset(x * moduleSize, y * moduleSize),
                        size = Size(moduleSize, moduleSize)
                    )
                }
            }
        }
    }
}

private fun encodeQrCode(content: String): BitMatrix =
    // A requested size of 0 returns one matrix cell per QR module
    QRCodeWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        0,
        0,
        mapOf(
            EncodeHintType.MARGIN to QUIET_ZONE_MODULES,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.CHARACTER_SET to Charsets.UTF_8.name()
        )
    )
