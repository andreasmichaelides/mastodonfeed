package com.andreasmichaelides.mastodonfeed.presentation

import android.R
import android.os.Bundle
import android.text.util.Linkify
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.parseAsHtml
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.andreasmichaelides.mastodonfeed.ui.theme.MastodonFeedTheme
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiModel: FeedUiModel by viewModel.uiModel.collectAsStateWithLifecycle()
            MainScreenComponent(
                onSearch = viewModel::onSearch,
                feedItems = uiModel.uiFeedItems
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        modifier = modifier
    )
}

@Composable
private fun MainScreenComponent(onSearch: (String) -> Unit, feedItems: List<UiFeedItem>) {
    MastodonFeedTheme {
        Surface(tonalElevation = 5.dp) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                ) {
                    SearchComponent(onSearch)
                    FeedItemsListComponent(feedItems = feedItems)
                }
            }
        }
    }
}

@Composable
private fun SearchComponent(onSearch: (String) -> Unit) {
    var searchFilter by remember { mutableStateOf("") }
    TextField(
        modifier = Modifier.fillMaxWidth(),
        prefix = {
            Icon(painter = painterResource(id = R.drawable.ic_menu_search), contentDescription = "")
        },
        value = searchFilter, onValueChange = {
            searchFilter = it
            onSearch(it)
        })
}

@Composable
private fun FeedItemsListComponent(modifier: Modifier = Modifier, feedItems: List<UiFeedItem>) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(feedItems.size) { index ->
            val feedItem = feedItems[index]
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
                    Row {
                        AsyncImage(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(size = 8.dp)),
                            model = feedItem.avatarUrl,
                            // Should use string from resources
                            contentDescription = "User Avatar"
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Greeting(name = feedItem.displayName)
                            Greeting(name = feedItem.userName)
                        }
                    }
                    HtmlContentView(modifier, feedItem.content)
                }
            }
        }
    }
}

@Composable
private fun HtmlContentView(modifier: Modifier, content: String) {
    AndroidView(
        modifier = modifier,
        factory = {
            MaterialTextView(it).apply {
                autoLinkMask = Linkify.WEB_URLS
                linksClickable = true
                setLinkTextColor(Color.Blue.toArgb())
            }
        },
        update = {
            it.text = content.parseAsHtml()
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MastodonFeedTheme {
        MainScreenComponent(
            onSearch = {},
            feedItems = listOf(
                UiFeedItem(
                    displayName = "Tester",
                    content = "some Content",
                    avatarUrl = "",
                    userName = "MegaMan"
                )
            )
        )
    }
}