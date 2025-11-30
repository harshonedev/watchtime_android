package com.app.watchtime.tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.popular.domain.entities.Media
import com.app.popular.domain.repository.PopularRepository
import com.app.discover.domain.repository.DiscoverRepository
import com.app.collections.domain.repository.CollectionRepository
import com.app.collections.domain.models.Collection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class TvHomeUiState(
    val popularMovies: List<Media> = emptyList(),
    val popularTvShows: List<Media> = emptyList(),
    val trendingDaily: List<Media> = emptyList(),
    val collections: List<Collection> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TvHomeViewModel(
    private val popularRepository: PopularRepository,
    private val discoverRepository: DiscoverRepository,
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TvHomeUiState())
    val uiState: StateFlow<TvHomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load data in parallel
                val popularMovies = try {
                    popularRepository.getPopularMovies()
                } catch (e: Exception) {
                    emptyList()
                }

                val popularTvShows = try {
                    popularRepository.getPopularTvShows()
                } catch (e: Exception) {
                    emptyList()
                }

                val trendingDaily = try {
                    popularRepository.getTrendingDaily()
                } catch (e: Exception) {
                    emptyList()
                }

                val collections = try {
                    collectionRepository.getCollections().firstOrNull() ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }

                _uiState.value = TvHomeUiState(
                    popularMovies = popularMovies,
                    popularTvShows = popularTvShows,
                    trendingDaily = trendingDaily,
                    collections = collections,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun retry() {
        loadData()
    }
}

