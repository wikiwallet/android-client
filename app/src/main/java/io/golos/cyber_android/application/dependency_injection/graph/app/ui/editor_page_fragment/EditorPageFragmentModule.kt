package io.golos.cyber_android.application.dependency_injection.graph.app.ui.editor_page_fragment

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.golos.cyber_android.ui.common.mvvm.viewModel.ViewModelKey
import io.golos.cyber_android.ui.shared_fragments.editor.EditorPageViewModel
import io.golos.domain.DiscussionsFeedRepository
import io.golos.domain.DispatchersProvider
import io.golos.domain.Repository
import io.golos.domain.entities.*
import io.golos.domain.interactors.feed.PostWithCommentUseCase
import io.golos.domain.interactors.images.ImageUploadUseCase
import io.golos.domain.interactors.model.*
import io.golos.domain.interactors.publish.DiscussionPosterUseCase
import io.golos.domain.interactors.publish.EmbedsUseCase
import io.golos.domain.requestmodel.CommentFeedUpdateRequest
import io.golos.domain.requestmodel.PostFeedUpdateRequest
import io.golos.domain.rules.EntityToModelMapper

@Module
class EditorPageFragmentModule(private val community: CommunityModel?, private val postToEdit: DiscussionIdModel?) {
    @Provides
    internal fun provideCommunity(): CommunityModel? = community

    @Provides
    internal fun providePostToEdit(): DiscussionIdModel? = postToEdit

    @Provides
    @IntoMap
    @ViewModelKey(EditorPageViewModel::class)
    internal fun provideEditorPageViewModel(
        embedsUseCase: EmbedsUseCase,
        posterUseCase: DiscussionPosterUseCase,
        imageUploadUseCase: ImageUploadUseCase,
        community: CommunityModel?,
        postToEdit: DiscussionIdModel?,
        postFeedRepository: DiscussionsFeedRepository<PostEntity, PostFeedUpdateRequest>,
        postEntityToModelMapper: EntityToModelMapper<DiscussionRelatedEntities<PostEntity>, PostModel>,
        commentsRepository: DiscussionsFeedRepository<CommentEntity, CommentFeedUpdateRequest>,
        voteRepository: Repository<VoteRequestEntity, VoteRequestEntity>,
        commentFeeEntityToModelMapper: EntityToModelMapper<FeedRelatedEntities<CommentEntity>, DiscussionsFeed<CommentModel>>,
        dispatchersProvider: DispatchersProvider
    ): ViewModel =
        EditorPageViewModel(
            embedsUseCase,
            posterUseCase,
            imageUploadUseCase,
            community,
            postToEdit,
            if (postToEdit != null) getPostWithCommentsUseCase(
                postToEdit,
                postFeedRepository,
                postEntityToModelMapper,
                commentsRepository,
                voteRepository,
                commentFeeEntityToModelMapper,
                dispatchersProvider
            )
            else null
        )

    private fun getPostWithCommentsUseCase(
        postId: DiscussionIdModel?,
        postFeedRepository: DiscussionsFeedRepository<PostEntity, PostFeedUpdateRequest>,
        postEntityToModelMapper: EntityToModelMapper<DiscussionRelatedEntities<PostEntity>, PostModel>,
        commentsRepository: DiscussionsFeedRepository<CommentEntity, CommentFeedUpdateRequest>,
        voteRepository: Repository<VoteRequestEntity, VoteRequestEntity>,
        commentFeeEntityToModelMapper: EntityToModelMapper<FeedRelatedEntities<CommentEntity>, DiscussionsFeed<CommentModel>>,
        dispatchersProvider: DispatchersProvider
    ) : PostWithCommentUseCase =
        PostWithCommentUseCase(
            postId!!,
            postFeedRepository,
            postEntityToModelMapper,
            commentsRepository,
            voteRepository,
            commentFeeEntityToModelMapper,
            dispatchersProvider
        )
}