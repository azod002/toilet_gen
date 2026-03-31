package com.toiletgen.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Long.toFormattedDate(): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(this))
}

fun <T> Flow<T>.catchWithMessage(onError: (String) -> Unit): Flow<T> =
    catch { onError(it.message ?: "Неизвестная ошибка") }
