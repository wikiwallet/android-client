package io.golos.cyber_android.ui.screens.profile_comments.view_model

import android.net.Uri
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.dto.Comment
import io.golos.cyber_android.ui.dto.ContentId
import io.golos.cyber_android.ui.mappers.mapToComment
import io.golos.cyber_android.ui.mappers.mapToCommentDomain
import io.golos.cyber_android.ui.mappers.mapToContentIdDomain
import io.golos.cyber_android.ui.screens.profile_comments.model.ProfileCommentsModel
import io.golos.cyber_android.ui.screens.profile_comments.model.item.ProfileCommentListItem
import io.golos.cyber_android.ui.screens.profile_comments.view.view_commands.NavigateToEditComment
import io.golos.cyber_android.ui.shared.mvvm.viewModel.ViewModelBase
import io.golos.cyber_android.ui.shared.mvvm.view_commands.*
import io.golos.cyber_android.ui.shared.paginator.Paginator
import io.golos.cyber_android.ui.shared.utils.PAGINATION_PAGE_SIZE
import io.golos.cyber_android.ui.shared.utils.localSize
import io.golos.cyber_android.ui.shared.utils.toLiveData
import io.golos.cyber_android.ui.shared.widgets.comment.CommentContent
import io.golos.domain.DispatchersProvider
import io.golos.domain.dto.CommentDomain
import io.golos.domain.dto.CommunityIdDomain
import io.golos.domain.dto.UserIdDomain
import io.golos.domain.posts_parsing_rendering.mappers.editor_output_to_json.EditorOutputToJsonMapper
import io.golos.domain.posts_parsing_rendering.post_metadata.editor_output.EmbedMetadata
import io.golos.domain.posts_parsing_rendering.post_metadata.post_dto.*
import io.golos.domain.repositories.CurrentUserRepository
import io.golos.domain.repositories.CurrentUserRepositoryRead
import io.golos.utils.id.IdUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class ProfileCommentsViewModel @Inject constructor(
    dispatchersProvider: DispatchersProvider,
    model: ProfileCommentsModel,
    private val profileUserId: UserIdDomain,
    private val paginator: Paginator.Store<CommentDomain>
) : ViewModelBase<ProfileCommentsModel>(dispatchersProvider, model),
    ProfileCommentsModelEventProcessor {

    override fun onLinkClicked(linkUri: Uri) {
        _command.value = NavigateToLinkViewCommand(linkUri)
    }

    override fun onImageClicked(imageUri: Uri) {
        _command.value = NavigateToImageViewCommand(imageUri)
    }

    override fun onItemClicked(contentId: ContentId) {}

    override fun onUserClicked(userId: String) {
        launch {
            val userIdResolved = model.getUserId(userId)

            _command.value = if(userIdResolved != profileUserId) {
                NavigateToUserProfileCommand(userIdResolved)
            } else {
                ScrollProfileToTopCommand()
            }
        }
    }

    override fun onCommunityClicked(communityId: CommunityIdDomain) {
        _command.value = NavigateToCommunityPageCommand(communityId)
    }

    override fun onSeeMoreClicked(contentId: ContentId) = false

    private var loadCommentsJob: Job? = null

    private val _commentListState: MutableLiveData<Paginator.State> = MutableLiveData(Paginator.State.Empty)
    val commentListState = _commentListState.toLiveData()

    val noDataStubText = MutableLiveData<Int>(R.string.no_comments_title).toLiveData()
    val noDataStubExplanation = MutableLiveData<Int>(R.string.no_comments_written).toLiveData()

    private val _noDataStubVisibility = MutableLiveData<Int>(View.GONE)
    val noDataStubVisibility: LiveData<Int> get() = _noDataStubVisibility

    init {
        paginator.sideEffectListener = { sideEffect ->
            when (sideEffect) {
                is Paginator.SideEffect.LoadPage -> loadPostComments(sideEffect.pageCount)
                is Paginator.SideEffect.ErrorEvent -> {
                }
            }
        }
        paginator.render = { newState, oldState ->
            _commentListState.value = newState
            _noDataStubVisibility.value = if (newState == Paginator.State.Empty && oldState == Paginator.State.EmptyProgress) {
                View.VISIBLE
            }
            else {
                View.GONE
            }
        }

        loadInitialComments()
    }

    override fun onBodyClicked(postContentId: ContentId?) {
        //Not need use
    }

    fun onSendComment(commentContent: CommentContent) {
        launch {
            try {
                _command.value = SetLoadingVisibilityCommand(true)
                val contentId = commentContent.contentId
                if (contentId != null) {

                    // Upload images
                    val uploadedImages = mutableListOf<String>()
                    commentContent.metadata
                        .firstOrNull { it is EmbedMetadata }
                        ?.let { it as EmbedMetadata }
                        ?.let {
                            if(!it.sourceUri.toString().startsWith("http") && !it.sourceUri.toString().startsWith("https")){
                                uploadedImages.add(model.uploadAttachmentContent(File(it.sourceUri.toString())))
                            }
                        }

                    val contentAsJson = EditorOutputToJsonMapper.mapComment(commentContent.metadata, uploadedImages)



                    val commentFromState = getCommentFromStateByContentId(_commentListState.value, contentId)

                    val commentForUpdate = commentFromState?.mapToCommentDomain()?.copy(
                        jsonBody = contentAsJson
                    )
                    commentForUpdate?.let {
                        model.updateComment(it)
                        _commentListState.value = updateCommentAfterEdit(
                            _commentListState.value,
                            contentId,
                            it.body
                        )
                    }
                }
                _command.value = SetLoadingVisibilityCommand(false)
            } catch (e: Exception) {
                Timber.e(e)
                _command.value = ShowMessageResCommand(R.string.unknown_error)
                _command.value = SetLoadingVisibilityCommand(false)
            }
        }
    }

    fun editComment(contentId: ContentId) {
        val comment = getCommentFromStateByContentId(_commentListState.value, contentId)
        comment?.let {
            _command.value = NavigateToEditComment(it)
        }
    }

    fun deleteComment(permlink: String, communityId: CommunityIdDomain) {
        launch {
            try {
                _command.value = SetLoadingVisibilityCommand(true)
                model.deleteComment(permlink, communityId)
                _commentListState.value = deletePostInState(_commentListState.value, permlink)
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                _command.value = SetLoadingVisibilityCommand(false)
            }
        }
    }

    fun loadMoreComments() {
        paginator.proceed(Paginator.Action.LoadMore)
    }

    override fun onRetryLoadComments() {
        loadInitialComments()
    }

    override fun onCommentLongClick(comment: Comment) {
        _command.value = NavigateToProfileCommentMenuDialogViewCommand(comment)
    }

    override fun onCommentUpVoteClick(commentId: ContentId) {
        launch {
            try {
                _commentListState.value = updateUpVoteCountOfVotes(_commentListState.value, commentId)
                model.commentUpVote(commentId.mapToContentIdDomain())
            } catch (e: Exception) {
                Timber.e(e)
                _command.value = ShowMessageResCommand(R.string.unknown_error)
            }
        }
    }

    override fun onCommentDownVoteClick(commentId: ContentId) {
        launch {
            try {
                _commentListState.value = updateDownVoteCountOfVotes(_commentListState.value, commentId)
                model.commentDownVote(commentId.mapToContentIdDomain())
            } catch (e: Exception) {
                Timber.e(e)
                _command.value = ShowMessageResCommand(R.string.unknown_error)
            }
        }
    }

    private fun updateUpVoteCountOfVotes(
        state: Paginator.State?,
        contentId: ContentId
    ): Paginator.State? {
        when (state) {
            is Paginator.State.Data<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                updateUpVoteInMessagesByContentId(contentId, comments)
            }
            is Paginator.State.Refresh<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                updateUpVoteInMessagesByContentId(contentId, comments)
            }
            is Paginator.State.NewPageProgress<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                updateUpVoteInMessagesByContentId(contentId, comments)
            }
            is Paginator.State.FullData<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                updateUpVoteInMessagesByContentId(contentId, comments)
            }
        }
        return state
    }


    private fun getCommentFromStateByContentId(state: Paginator.State?, contentId: ContentId): Comment? {
        return when (state) {
            is Paginator.State.Data<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                comments.find { it.comment.contentId == contentId }?.comment
            }
            is Paginator.State.Refresh<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                comments.find { it.comment.contentId == contentId }?.comment
            }
            is Paginator.State.NewPageProgress<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                comments.find { it.comment.contentId == contentId }?.comment
            }
            is Paginator.State.FullData<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                comments.find { it.comment.contentId == contentId }?.comment
            }
            else -> null
        }
    }

    private fun updateUpVoteInMessagesByContentId(contentId: ContentId, comments: ArrayList<ProfileCommentListItem>) {
        val foundedComment = comments.find { comment ->
            comment.comment.contentId == contentId
        }
        val updateCommentItem = foundedComment?.copy()
        updateCommentItem?.let {
            val oldVotes = it.comment.votes
            it.comment = it.comment.copy(
                votes = oldVotes.copy(
                    upCount = oldVotes.upCount + 1,
                    downCount = if (oldVotes.hasDownVote) oldVotes.downCount - 1 else oldVotes.downCount,
                    hasUpVote = true,
                    hasDownVote = false
                )
            )
            comments[comments.indexOf(foundedComment)] = updateCommentItem
        }
    }

    private fun updateDownVoteCountOfVotes(
        state: Paginator.State?,
        contentId: ContentId
    ): Paginator.State? {
        when (state) {
            is Paginator.State.Data<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                updateDownVoteInMessagesByContentId(contentId, comments)
            }
            is Paginator.State.Refresh<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                updateDownVoteInMessagesByContentId(contentId, comments)
            }
            is Paginator.State.NewPageProgress<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                updateDownVoteInMessagesByContentId(contentId, comments)
            }
            is Paginator.State.FullData<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                updateDownVoteInMessagesByContentId(contentId, comments)
            }
        }
        return state
    }

    private fun updateDownVoteInMessagesByContentId(contentId: ContentId, comments: ArrayList<ProfileCommentListItem>) {
        val foundedComment = comments.find { comment ->
            comment.comment.contentId == contentId
        }
        val updateCommentItem = foundedComment?.copy()
        updateCommentItem?.let {
            val oldVotes = it.comment.votes
            it.comment = it.comment.copy(
                votes = oldVotes.copy(
                    downCount = oldVotes.downCount + 1,
                    upCount = if (oldVotes.hasUpVote) oldVotes.upCount - 1 else oldVotes.upCount,
                    hasUpVote = false,
                    hasDownVote = true
                )
            )
            comments[comments.indexOf(foundedComment)] = updateCommentItem
        }
    }

    private fun updateCommentAfterEdit(
        state: Paginator.State?,
        contentId: ContentId,
        body: ContentBlock?
    ): Paginator.State? {
        when (state) {
            is Paginator.State.Data<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                updateCommentByContentId(contentId, comments, body)
            }
            is Paginator.State.Refresh<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                updateCommentByContentId(contentId, comments, body)
            }
            is Paginator.State.NewPageProgress<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                updateCommentByContentId(contentId, comments, body)
            }
            is Paginator.State.FullData<*> -> {
                val comments = (state).data as ArrayList<ProfileCommentListItem>
                updateCommentByContentId(contentId, comments, body)
            }
        }
        return state
    }

    private fun updateCommentByContentId(
        contentId: ContentId,
        comments: ArrayList<ProfileCommentListItem>,
        body: ContentBlock?
    ) {
        val foundedComment = comments.find { comment ->
            comment.comment.contentId == contentId
        }
        val updatedCommentItem = foundedComment?.copy()
        updatedCommentItem?.let { item ->
            item.comment = item.comment.copy(body = body)
            comments[comments.indexOf(foundedComment)] = item
        }
    }

    private fun deletePostInState(state: Paginator.State?, permlink: String): Paginator.State? {
        when (state) {
            is Paginator.State.Data<*> -> {
                val comments = (state).data as MutableList<ProfileCommentListItem>
                val foundedComment = comments.find { comment ->
                    comment.comment.contentId.permlink == permlink
                }
                val elementDeletedIndex = comments.indexOf(foundedComment)

                if (elementDeletedIndex != -1 && foundedComment != null) {
                    val updatedComment = foundedComment.comment.copy(
                        body = null,
                        isDeleted = true
                    )

                    comments[elementDeletedIndex] = ProfileCommentListItem(updatedComment)
                }
            }
            is Paginator.State.Refresh<*> -> {
                val comments = (state).data as MutableList<ProfileCommentListItem>
                val foundedComment = comments.find { comment ->
                    comment.comment.contentId.permlink == permlink
                }
                val elementDeletedIndex = comments.indexOf(foundedComment)

                if (elementDeletedIndex != -1 && foundedComment != null) {
                    val updatedComment = foundedComment.comment.copy(
                        body = null,
                        isDeleted = true
                    )

                    comments[elementDeletedIndex] = ProfileCommentListItem(updatedComment)
                }

            }
            is Paginator.State.NewPageProgress<*> -> {
                val comments = (state).data as MutableList<ProfileCommentListItem>
                val foundedComment = comments.find { comment ->
                    comment.comment.contentId.permlink == permlink
                }
                val elementDeletedIndex = comments.indexOf(foundedComment)

                if (elementDeletedIndex != -1 && foundedComment != null) {
                    val updatedComment = foundedComment.comment.copy(
                        body = null,
                        isDeleted = true
                    )

                    comments[elementDeletedIndex] = ProfileCommentListItem(updatedComment)
                }

            }
            is Paginator.State.FullData<*> -> {
                val comments = (state).data as MutableList<ProfileCommentListItem>
                val foundedComment = comments.find { comment ->
                    comment.comment.contentId.permlink == permlink
                }
                val elementDeletedIndex = comments.indexOf(foundedComment)

                if (elementDeletedIndex != -1 && foundedComment != null) {
                    val updatedComment = foundedComment.comment.copy(
                        body = null,
                        isDeleted = true
                    )

                    comments[elementDeletedIndex] = ProfileCommentListItem(updatedComment)
                }
            }
        }
        return state
    }

    private fun loadInitialComments() {
        val postsListState = _commentListState.value
        if (postsListState is Paginator.State.Empty || postsListState is Paginator.State.EmptyError) {
            restartLoadComments()
        }
    }

    private fun restartLoadComments() {
        loadCommentsJob?.cancel()
        paginator.proceed(Paginator.Action.Restart)
    }

    private fun loadPostComments(pageCount: Int) {
        loadCommentsJob = launch {
            try {
                val commentsDomain = model.getComments(
                    offset = pageCount * PAGINATION_PAGE_SIZE,
                    pageSize = PAGINATION_PAGE_SIZE
                ).map { it.mapToComment() }
                    .map { ProfileCommentListItem(it) }
                launch(Dispatchers.Main) {
                    paginator.proceed(Paginator.Action.NewPage(pageCount, commentsDomain))
                }
            } catch (e: Exception) {
                Timber.e(e)
                paginator.proceed(Paginator.Action.PageError(e))
            }
        }
    }

    override fun onCleared() {
        loadCommentsJob?.cancel()
        super.onCleared()
    }

}