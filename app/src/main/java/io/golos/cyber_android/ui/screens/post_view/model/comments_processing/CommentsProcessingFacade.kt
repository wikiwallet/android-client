package io.golos.cyber_android.ui.screens.post_view.model.comments_processing

import io.golos.cyber_android.ui.dto.ContentId
import io.golos.domain.use_cases.model.CommentModel
import io.golos.domain.use_cases.model.DiscussionIdModel
import io.golos.domain.use_cases.post.post_dto.AttachmentsBlock
import io.golos.domain.use_cases.post.post_dto.Block
import io.golos.domain.use_cases.post.post_dto.ContentBlock

interface CommentsProcessingFacade {
    val pageSize: Int

    suspend fun loadStartFirstLevelPage()

    suspend fun loadNextFirstLevelPageByScroll()

    suspend fun retryLoadFirstLevelPage()

    suspend fun loadNextSecondLevelPage(parentCommentId: DiscussionIdModel)

    suspend fun retryLoadSecondLevelPage(parentCommentId: DiscussionIdModel)

    suspend fun sendComment(content: List<Block>, attachments: AttachmentsBlock?)

    suspend fun deleteComment(commentId: DiscussionIdModel, isSingleComment: Boolean)

    fun getCommentText(commentId: DiscussionIdModel): List<CharSequence>

    fun getCommentBody(commentId: ContentId): ContentBlock?

    fun getComment(discussionIdModel: DiscussionIdModel): CommentModel?

    suspend fun updateCommentText(commentId: DiscussionIdModel, content: List<Block>, attachments: AttachmentsBlock?)

    suspend fun replyToComment(repliedCommentId: DiscussionIdModel, content: List<Block>, attachments: AttachmentsBlock?)

    suspend fun vote(commentId: DiscussionIdModel, isUpVote: Boolean)
}