package com.toiletgen.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ConsentScreen(onAccepted: () -> Unit) {
    val context = LocalContext.current
    var checked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.weight(0.2f))

        Text(
            "ToiletGen",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Политика конфиденциальности",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(32.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Для использования приложения необходимо ознакомиться и согласиться с политикой конфиденциальности.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Мы собираем и обрабатываем следующие данные:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                DataPoint("Email и имя пользователя для авторизации")
                DataPoint("Данные геолокации для отображения точек на карте")
                DataPoint("Сообщения в чате и на форуме")
                DataPoint("Загруженные файлы (книги)")

                Spacer(Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://azod002.github.io/toilet_gen/privacy-policy.html"))
                        context.startActivity(intent)
                    },
                ) {
                    Text("Прочитать полную политику конфиденциальности")
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it },
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Я ознакомился(-ась) с политикой конфиденциальности и даю согласие на обработку персональных данных",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                context.getSharedPreferences("consent", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("privacy_accepted", true)
                    .apply()
                onAccepted()
            },
            enabled = checked,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text("Принять и продолжить", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.weight(0.3f))
    }
}

@Composable
private fun DataPoint(text: String) {
    Row(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
        Text("  \u2022  ", style = MaterialTheme.typography.bodySmall)
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}
