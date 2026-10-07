package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.model.NewsEntity
import com.example.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    private val repository = NewsRepository(db.dao())

    val rawDailyNews: StateFlow<List<NewsEntity>> = repository.dailyNews
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val filteredNews: StateFlow<List<NewsEntity>> = combine(
        rawDailyNews,
        _selectedCategory,
        _searchQuery
    ) { newsList, category, query ->
        newsList.filter { news ->
            val matchesCategory = (category == "All" || news.category.contains(category, ignoreCase = true))
            val matchesQuery = query.isBlank() ||
                    news.title.contains(query, ignoreCase = true) ||
                    news.titleTe.contains(query, ignoreCase = true) ||
                    news.content.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        fetchDailyNews()
    }

    fun fetchDailyNews() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.fetchAndCacheDailyNews()
            } catch (e: Exception) {
                // Graceful handling
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshNews() {
        fetchDailyNews()
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addNews(title: String, content: String, category: String) {
        viewModelScope.launch {
            repository.addNews(
                NewsEntity(
                    title = title,
                    content = content,
                    category = category,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
