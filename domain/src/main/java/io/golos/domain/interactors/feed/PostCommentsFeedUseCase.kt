package io.golos.domain.interactors.feed

import io.golos.domain.repositories.DiscussionsFeedRepository
import io.golos.domain.DispatchersProvider
import io.golos.domain.repositories.Repository
import io.golos.domain.entities.CommentEntity
import io.golos.domain.entities.DiscussionsSort
import io.golos.domain.entities.FeedRelatedEntities
import io.golos.domain.entities.VoteRequestEntity
import io.golos.domain.interactors.model.*
import io.golos.domain.requestmodel.CommentFeedUpdateRequest
import io.golos.domain.requestmodel.CommentsOfApPostUpdateRequest
import io.golos.domain.mappers.EntityToModelMapper

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-03-27.
 */
open class PostCommentsFeedUseCase(
    protected val postId: DiscussionIdModel,
    commentsFeedRepository: DiscussionsFeedRepository<CommentEntity, CommentFeedUpdateRequest>,
    voteRepository: Repository<VoteRequestEntity, VoteRequestEntity>,
    feedMapper: EntityToModelMapper<FeedRelatedEntities<CommentEntity>, DiscussionsFeed<CommentModel>>,
    dispatchersProvider: DispatchersProvider
) : AbstractFeedUseCase<CommentFeedUpdateRequest, CommentEntity, CommentModel>(
    commentsFeedRepository,
    voteRepository,
    feedMapper,
    dispatchersProvider
) {


    override val baseFeedUpdateRequest: CommentsOfApPostUpdateRequest
            by lazy {
                CommentsOfApPostUpdateRequest(
                    postId.userId,
                    postId.permlink,
                    0,
                    DiscussionsSort.FROM_OLD_TO_NEW,
                    null
                )
            }

    override fun requestFeedUpdate(limit: Int, option: UpdateOption,
                                   sort: DiscussionsSort?, timeFrame: FeedTimeFrameOption?) {
        val myFeed = discussionsFeedRepository.getAsLiveData(baseFeedUpdateRequest).value
        val nextPageId = myFeed?.nextPageId
        val resolvedOption = option.resolveUpdateOption()
        val request = CommentsOfApPostUpdateRequest(
            postId.userId,
            postId.permlink,
            limit,
            DiscussionsSort.FROM_OLD_TO_NEW,
            when (resolvedOption) {
                UpdateOption.REFRESH_FROM_BEGINNING -> null
                UpdateOption.FETCH_NEXT_PAGE -> nextPageId
            }
        )
        discussionsFeedRepository.makeAction(request)
    }
}