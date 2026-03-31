package com.toiletgen.core.common

import kotlinx.coroutines.flow.*

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true },
): Flow<Resource<ResultType>> = flow {
    emit(Resource.Loading)

    val data = query().first()

    val flow = if (shouldFetch(data)) {
        emit(Resource.Loading)
        try {
            val response = fetch()
            saveFetchResult(response)
            query().map { Resource.Success(it) }
        } catch (throwable: Throwable) {
            query().map { Resource.Error(throwable.message ?: "Ошибка сети") }
        }
    } else {
        query().map { Resource.Success(it) }
    }

    emitAll(flow)
}
