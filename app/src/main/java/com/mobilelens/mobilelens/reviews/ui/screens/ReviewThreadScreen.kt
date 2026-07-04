package com.mobilelens.mobilelens.reviews.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mobilelens.mobilelens.phones.data.PhoneCatalogue
import com.mobilelens.mobilelens.reviews.model.Review
import com.mobilelens.mobilelens.reviews.model.ReviewThread
import com.mobilelens.mobilelens.reviews.ui.components.ReviewCard

@Composable
fun ReviewThreadScreen(
    thread: ReviewThread,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        thread.reviews?.forEach { review ->
            ReviewCard(
                title = review.title,
                author = review.author,
                commentCount = review.commentCount,
                likeCount = review.likeCount
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewThreadScreenPreview() {
    MaterialTheme {
        ReviewThreadScreen(
            ReviewThread(
                id = 0,
                phone = PhoneCatalogue[0],
                reviews = listOf(
                    Review(
                        id = 1,
                        title = "Review 1",
                        author = "John Phone",
                        commentCount = 5,
                        likeCount = 12,
                        content = "",
                        createdAt = "1-07-2026",
                        updatedAt = "1-07-2026",
                        assets = null
                    )
                )
            )
        )
    }
}
