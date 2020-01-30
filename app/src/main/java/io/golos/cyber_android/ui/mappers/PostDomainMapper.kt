package io.golos.cyber_android.ui.mappers

import io.golos.cyber_android.ui.dto.Post
import io.golos.domain.dto.PostDomain

fun PostDomain.mapToPost(): Post {
    return Post(
        this.author.mapToAuthor(),
        this.community.mapToPostCommunity(),
        this.contentId.mapToContentId(),
        this.body,
        this.meta.mapToMeta(),
        null,
        null,
        this.shareUrl,
        this.votes.mapToVotes(),
        this.isMyPost,
        this.reward
    )
}