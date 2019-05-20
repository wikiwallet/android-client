package io.golos.domain.entities

import io.golos.domain.Entity
import java.math.BigInteger
import java.util.*

data class DiscussionCommentsCount(val count: Long) : Entity

data class PostContent(
    val title: String,
    val body: ContentBody
) : Entity

data class CommentContent(val body: ContentBody) : Entity

data class ContentBody(
    val preview: String,
    val full: List<ContentRowEntity>,
    val embeds: List<EmbedEntity>,
    val mobilePreview : List<ContentRowEntity>
) : Entity

data class EmbedEntity(
    val type: String,
    val title: String,
    val url: String,
    val author: String,
    val provider_name: String,
    val html: String
) : Entity

sealed class ContentRowEntity : Entity

data class TextRowEntity(val text: String) : ContentRowEntity()

data class ImageRowEntity(val src: String) : ContentRowEntity()


data class DiscussionMetadata(val time: Date) : Entity
data class DiscussionPayout(val rShares: BigInteger) : Entity
data class DiscussionVotes(
    val hasUpVote: Boolean,
    val hasDownVote: Boolean,
    val upCount: Int,
    val downCount: Int
) : Entity
