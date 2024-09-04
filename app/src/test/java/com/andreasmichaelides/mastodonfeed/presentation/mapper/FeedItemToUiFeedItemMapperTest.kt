package com.andreasmichaelides.mastodonfeed.presentation.mapper

import android.text.Spanned
import com.andreasmichaelides.api.domain.FeedItem
import com.andreasmichaelides.mastodonfeed.presentation.UiFeedItem
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

// Mappers can be also tested with Parameterized tests, which is great for easier testing multiple for inputs/cases
class FeedItemToUiFeedItemMapperTest {

    private val htmlStringToSpannedMapper: HtmlStringToSpannedMapper = mockk()

    private val feedItemToUiFeedItemMapper = FeedItemToUiFeedItemMapper(htmlStringToSpannedMapper)

    private val spanned: Spanned = mockk()

    @Before
    fun setUp() {
        every { htmlStringToSpannedMapper(any()) } returns spanned
    }

    @Test
    fun `given FeedItem when mapped matches the expected UiFeedItem`() {
        // Given
        val feedItem = FeedItem(
            id = "",
            content = "<summary>Snapshots of the development version are available in Sonatype's snapshots repository.</summary>",
            userName = "unknown",
            displayName = "MegaMan",
            avatarUrl = "https://vignette.wikia.nocookie.net/es.megaman/images/b/b8/Falcon_armor_x.jpg/revision/latest?cb=20091114153206",
            imageUrl = "",
            linkUrl = "",
            createdDate = LocalDate.now(),
            addedDateInMillis = 10
        )
        val expected = UiFeedItem(
            displayName = "MegaMan",
            content = spanned,
            avatarUrl = "https://vignette.wikia.nocookie.net/es.megaman/images/b/b8/Falcon_armor_x.jpg/revision/latest?cb=20091114153206",
            userName = "unknown"
        )

        // When
        val actual = feedItemToUiFeedItemMapper(feedItem)

        // Then
        assertEquals(expected, actual)
    }
}