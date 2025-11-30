package com.app.watchtime.tv.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.auth.domain.repository.AuthRepository
import com.app.core.tvui.components.TvContentItem
import com.app.core.tvui.components.TvContentRow
import com.app.watchtime.tv.viewmodel.TvHomeViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import java.util.Locale

@Composable
fun TvHomeScreen(
    onSignOut: () -> Unit,
    authRepository: AuthRepository = koinInject(),
    viewModel: TvHomeViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    // Content
    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.retry() }) {
                    Text("Retry")
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                contentPadding = PaddingValues(vertical = 48.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WatchTime TV",
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Browse Content",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(onClick = {
                            scope.launch {
                                authRepository.logout()
                                onSignOut()
                            }
                        }) {
                            Text("Sign Out")
                        }
                    }
                }

                // Trending Daily
                if (uiState.trendingDaily.isNotEmpty()) {
                    item {
                        TvContentRow(
                            title = "Trending Today",
                            items = uiState.trendingDaily.map {
                                TvContentItem(
                                    id = it.id,
                                    title = it.title,
                                    subtitle = "★ ${String.format(Locale.US, "%.1f", it.voteAverage)}",
                                    imageUrl = it.posterUrl
                                )
                            },
                            onItemClick = { /* TODO: Navigate to detail */ }
                        )
                    }
                }

                // Popular Movies
                if (uiState.popularMovies.isNotEmpty()) {
                    item {
                        TvContentRow(
                            title = "Popular Movies",
                            items = uiState.popularMovies.map {
                                TvContentItem(
                                    id = it.id,
                                    title = it.title,
                                    subtitle = "★ ${String.format(Locale.US, "%.1f", it.voteAverage)}",
                                    imageUrl = it.posterUrl
                                )
                            },
                            onItemClick = { /* TODO: Navigate to detail */ }
                        )
                    }
                }

                // Popular TV Shows
                if (uiState.popularTvShows.isNotEmpty()) {
                    item {
                        TvContentRow(
                            title = "Popular TV Shows",
                            items = uiState.popularTvShows.map {
                                TvContentItem(
                                    id = it.id,
                                    title = it.title,
                                    subtitle = "★ ${String.format(Locale.US, "%.1f", it.voteAverage)}",
                                    imageUrl = it.posterUrl
                                )
                            },
                            onItemClick = { /* TODO: Navigate to detail */ }
                        )
                    }
                }

                // Collections
                if (uiState.collections.isNotEmpty()) {
                    item {
                        TvContentRow(
                            title = "Your Collections",
                            items = uiState.collections.map {
                                TvContentItem(
                                    id = it.id.hashCode(),
                                    title = it.name,
                                    subtitle = it.description ?: "",
                                    imageUrl = null
                                )
                            },
                            onItemClick = { /* TODO: Navigate to collection */ }
                        )
                    }
                }

                // Empty state
                if (uiState.popularMovies.isEmpty() &&
                    uiState.popularTvShows.isEmpty() &&
                    uiState.trendingDaily.isEmpty() &&
                    uiState.collections.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No content available. Please check your connection.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

