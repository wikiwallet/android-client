package io.golos.domain.interactors.feed

import io.golos.domain.DiscussionsFeedRepository
import io.golos.domain.DispatchersProvider
import io.golos.domain.entities.DiscussionsSort
import io.golos.domain.entities.FeedEntity
import io.golos.domain.entities.PostEntity
import io.golos.domain.interactors.model.PostFeed
import io.golos.domain.interactors.model.UpdateOption
import io.golos.domain.model.PostFeedUpdateRequest
import io.golos.domain.model.UserSubscriptionsFeedUpdateRequest
import io.golos.domain.rules.EntityToModelMapper

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-03-18.
 */
class UserSubscriptionsFeedUseCase(
    private val userId: String,
    postFeedRepository: DiscussionsFeedRepository<PostEntity, PostFeedUpdateRequest>,
    feedMapper: EntityToModelMapper<FeedEntity<PostEntity>, PostFeed>,
    dispatchersProvider: DispatchersProvider
) : AbstractFeedUseCase<UserSubscriptionsFeedUpdateRequest>(postFeedRepository, feedMapper, dispatchersProvider) {


    override val baseFeedUpdateRequest: UserSubscriptionsFeedUpdateRequest
        get() = UserSubscriptionsFeedUpdateRequest(userId, 0, DiscussionsSort.FROM_NEW_TO_OLD, null)

    override fun requestFeedUpdate(limit: Int, option: UpdateOption) {
        postFeedRepository.makeAction(
            UserSubscriptionsFeedUpdateRequest(
                userId,
                limit,
                DiscussionsSort.FROM_NEW_TO_OLD,
                when (option.resolveUpdateOption()) {
                    UpdateOption.REFRESH_FROM_BEGINNING -> null
                    UpdateOption.FETCH_NEXT_PAGE -> postFeedRepository.getAsLiveData(baseFeedUpdateRequest).value?.nextPageId
                }
            )
        )
    }
}