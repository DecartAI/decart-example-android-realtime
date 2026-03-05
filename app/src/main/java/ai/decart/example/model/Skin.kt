package ai.decart.example.model

import androidx.annotation.DrawableRes

data class Skin(
    val title: String,
    val prompt: String,
    @DrawableRes val thumbnailRes: Int
)
