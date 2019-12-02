package io.golos.cyber_android.ui.screens.my_feed.model

import io.golos.cyber_android.ui.common.mvvm.model.ModelBase
import io.golos.cyber_android.ui.screens.post_filters.PostFiltersHolder
import io.golos.cyber_android.ui.screens.post_report.PostReportHolder
import io.golos.domain.use_cases.community.SubscribeToCommunityUseCase
import io.golos.domain.use_cases.community.UnsubscribeToCommunityUseCase
import io.golos.domain.use_cases.posts.GetPostsUseCase
import io.golos.domain.use_cases.user.GetLocalUserUseCase
import kotlinx.coroutines.flow.Flow

interface MyFeedModel : ModelBase,
    GetPostsUseCase,
    GetLocalUserUseCase,
    SubscribeToCommunityUseCase,
    UnsubscribeToCommunityUseCase {

    suspend fun addToFavorite(permlink: String)

    suspend fun removeFromFavorite(permlink: String)

    suspend fun deletePost(permlink: String)

    suspend fun upVote(
        communityId: String,
        userId: String,
        permlink: String
    )

    suspend fun downVote(
        communityId: String,
        userId: String,
        permlink: String
    )

    suspend fun reportPost(
        communityId: String,
        userId: String,
        permlink: String,
        reason: String
    )

    val feedFiltersFlow: Flow<PostFiltersHolder.FeedFilters>

    val reportsFlow: Flow<PostReportHolder.Report>
}