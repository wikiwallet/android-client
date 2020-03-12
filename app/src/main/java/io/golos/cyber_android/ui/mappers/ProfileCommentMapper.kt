package io.golos.cyber_android.ui.mappers

import io.golos.cyber_android.ui.dto.Comment
import io.golos.cyber_android.ui.screens.comment_page_menu.model.CommentMenu
import io.golos.domain.dto.*

fun Comment.mapToCommentMenu(): CommentMenu = CommentMenu(
    contentId = contentId,
    communityId = community.communityId,
    communityName = community.name,
    communityAvatarUrl = community.avatarUrl,
    creationTime = meta.creationTime,
    authorUserId = author.userId,
    authorUsername = author.username,
    permlink = contentId.permlink
)

fun Comment.mapToCommentDomain(): CommentDomain = CommentDomain(
    contentId = this.contentId.mapToContentIdDomain(),
    author = AuthorDomain(this.author.avatarUrl,
        this.author.userId,
        this.author.username),
    votes = VotesDomain(this.votes.downCount,
        this.votes.upCount,
        this.votes.hasUpVote,
        this.votes.hasDownVote),
    body = this.body,
    childCommentsCount = this.childCommentsCount,
    community = CommunityDomain(
        this.community.communityId,
        this.community.alias,
        this.community.name ?: "",
        this.community.avatarUrl,
        null,
        0,
        0,
        this.community.isSubscribed),
    meta = MetaDomain(this.meta.creationTime),
    parent = ParentCommentDomain(this.parent.comment,
        this.parent.post),
    type = this.type,
    isDeleted = this.isDeleted,
    isMyComment = this.isMyComment
)