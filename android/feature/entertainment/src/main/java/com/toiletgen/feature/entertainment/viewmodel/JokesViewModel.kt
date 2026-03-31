package com.toiletgen.feature.entertainment.viewmodel

import androidx.lifecycle.ViewModel
import com.toiletgen.feature.entertainment.data.jokes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JokesViewModel : ViewModel() {
    private val _currentJoke = MutableStateFlow(jokes.random())
    val currentJoke: StateFlow<String> = _currentJoke.asStateFlow()

    private val _favorites = MutableStateFlow<List<String>>(emptyList())
    val favorites: StateFlow<List<String>> = _favorites.asStateFlow()

    fun nextJoke() {
        _currentJoke.value = jokes.random()
    }

    fun toggleFavorite(joke: String) {
        _favorites.value = if (joke in _favorites.value) {
            _favorites.value - joke
        } else {
            _favorites.value + joke
        }
    }
}
