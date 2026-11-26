package com.prepflow.app.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent

object LinkLauncher {

    fun normalizeUrl(rawUrl: String): String? {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty() || trimmed.any { it.isWhitespace() }) return null

        val withScheme = if (
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            trimmed
        } else {
            "https://$trimmed"
        }

        val parsed = runCatching { Uri.parse(withScheme) }.getOrNull() ?: return null
        val host = parsed.host
        if (host.isNullOrBlank() || !host.contains(".")) return null
        if (!Patterns.WEB_URL.matcher(withScheme).matches()) return null

        return withScheme
    }

    fun openUrl(
        context: Context,
        rawUrl: String,
        onError: ((String) -> Unit)? = null
    ) {
        val normalized = normalizeUrl(rawUrl)
        if (normalized == null) {
            val message = "Geçersiz bağlantı adresi: $rawUrl"
            if (onError != null) {
                onError(message)
            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            return
        }

        val uri = Uri.parse(normalized)
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, uri)
        } catch (_: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (_: ActivityNotFoundException) {
                val message = "Bu bağlantıyı açabilecek bir tarayıcı bulunamadı."
                if (onError != null) {
                    onError(message)
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                val message = "Bağlantı açılırken bir hata oluştu."
                if (onError != null) {
                    onError(message)
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
