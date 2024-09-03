package com.andreasmichaelides.mastodonfeed.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.andreasmichaelides.api.domain.GetFeedItemsUseCase
import com.andreasmichaelides.mastodonfeed.ui.theme.MastodonFeedTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var getFeedItemsUseCase: GetFeedItemsUseCase

    @Inject
    lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        lifecycleScope.launch {
//            withContext(Dispatchers.IO) {
//                try {
//                    getFeetItemsUseCase().collect {
//                        Log.d("Pafto", "Item: $it")
//                    }
//                } catch (e: Exception) {
//                    Log.d("Pafto", e.toString())
//                }
//            }
//        }

//        viewModel.loadFeedItemsStream()

        setContent {
            MastodonFeedTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MastodonFeedTheme {
        Greeting("Android")
    }
}