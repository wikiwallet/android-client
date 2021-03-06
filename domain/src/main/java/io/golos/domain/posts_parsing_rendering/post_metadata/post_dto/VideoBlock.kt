package io.golos.domain.posts_parsing_rendering.post_metadata.post_dto

import android.net.Uri
import android.util.Size

data class VideoBlock(
    val id: Long?,
    val content: Uri,
    val title: String?,
    val providerName: String?,
    val author: String?,
    val authorUrl: Uri?,
    val description: String?,
    val thumbnailUrl: Uri?,
    val thumbnailSize: Size?,
    val html: String?
): MediaBlock