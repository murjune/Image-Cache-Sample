package sample.image.cache

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import sample.image.cache.ui.theme.ImageCacheSampleTheme

class MainActivity : ComponentActivity() {
    private val imageLoader by lazy {
        ImageLoader(ImageService(), ImageSaver(applicationContext))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageCacheSampleTheme {
                var images by remember { mutableStateOf(emptyList<Bitmap>()) }
                val refreshState = rememberPullToRefreshState()
                val scope = rememberCoroutineScope()
                LaunchedEffect(key1 = refreshState.isRefreshing) {
                    if (refreshState.isRefreshing) {
                        imageLoader.bitmaps(pokemonUrls(20))
                        refreshState.endRefresh()
                    }
                }

                LaunchedEffect(key1 = Unit) {
                    images = imageLoader.bitmaps(pokemonUrls(20))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(refreshState.nestedScrollConnection)
                ) {
                    PokemonScreen(refreshState.isRefreshing, images = images, onClearCache = {
                        scope.launch {
                            imageLoader.clearCache()
                        }
                    })

                    PullToRefreshContainer(
                        state = refreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }
        }
    }

    private fun pokemonUrls(count: Int): List<String> =
        (1..count).map { pokemonImageUrl(it.toLong()) }

    private fun pokemonImageUrl(pokemonId: Long): String =
        FORMAT_POKEMON_IMAGE_URL + pokemonId + POSTFIX_PNG

    companion object {
        private const val FORMAT_POKEMON_IMAGE_URL =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other" +
                    "/official-artwork/"
        private const val POSTFIX_PNG = ".png"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokemonScreen(
    isLoading: Boolean,
    images: List<Bitmap>,
    onClearCache: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Step4 - LRU Cache", color = Color.White)
                },
                colors = TopAppBarColors(
                    containerColor = Color.Gray,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onClearCache()
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "캐시 비우기"
                )
            }
        }
    ) { innerPadding ->
        val pullRefreshState = rememberPullToRefreshState()
        pullRefreshState.startRefresh()
        Column(
            modifier = Modifier
                .nestedScroll(pullRefreshState.nestedScrollConnection)
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading || images.isEmpty()) Loading()
            Pokemons(bitmaps = images)
        }
    }
}

@Composable
private fun Pokemons(bitmaps: List<Bitmap>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        items(bitmaps.size) { index ->
            Pokemon(bitmaps[index])
        }
    }
}

@Composable
private fun Loading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Loading...", fontSize = 48.sp)
    }
}

@Composable
private fun Pokemon(bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(
                RoundedCornerShape(16.dp)
            )
    )

}

@Composable
@Preview
fun PokemonPreview() {
    ImageCacheSampleTheme {
        Pokemon(bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    }
}