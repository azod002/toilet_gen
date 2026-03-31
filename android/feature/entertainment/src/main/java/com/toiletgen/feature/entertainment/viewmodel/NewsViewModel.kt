package com.toiletgen.feature.entertainment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.feature.entertainment.data.NewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL

data class NewsUiState(
    val news: List<NewsItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class NewsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private val rssFeeds = listOf(
        "https://lenta.ru/rss/news" to "Лента.ру",
        "https://www.rbc.ru/v10/ajax/get-news-feed/project/rbcnews.touched/lastDate/1/limit/20" to "РБК",
    )

    init {
        loadNews()
    }

    fun loadNews() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val items = mutableListOf<NewsItem>()
                for ((url, source) in rssFeeds) {
                    try {
                        val rss = fetchUrl(url)
                        items.addAll(parseRss(rss, source))
                    } catch (_: Exception) { }
                }
                if (items.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Не удалось загрузить новости",
                        news = emptyList(),
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        news = items.take(30),
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка загрузки",
                )
            }
        }
    }

    private fun fetchUrl(urlStr: String): String {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        conn.setRequestProperty("User-Agent", "ToiletGen/1.0")
        return conn.inputStream.bufferedReader().readText()
    }

    private fun parseRss(xml: String, source: String): List<NewsItem> {
        val items = mutableListOf<NewsItem>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var inItem = false
            var title = ""
            var description = ""
            var link = ""
            var pubDate = ""
            var currentTag = ""

            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == "item" || currentTag == "entry") inItem = true
                    }
                    XmlPullParser.TEXT -> {
                        if (inItem) {
                            when (currentTag) {
                                "title" -> title += parser.text
                                "description", "summary" -> description += parser.text
                                "link" -> link += parser.text
                                "pubDate", "published", "updated" -> pubDate += parser.text
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item" || parser.name == "entry") {
                            if (title.isNotBlank()) {
                                items.add(
                                    NewsItem(
                                        title = title.trim(),
                                        description = description.trim()
                                            .replace(Regex("<[^>]*>"), "")
                                            .take(200),
                                        source = source,
                                        url = link.trim(),
                                        publishedAt = pubDate.trim(),
                                    )
                                )
                            }
                            title = ""
                            description = ""
                            link = ""
                            pubDate = ""
                            inItem = false
                        }
                        currentTag = ""
                    }
                }
                parser.next()
            }
        } catch (_: Exception) { }
        return items
    }
}
