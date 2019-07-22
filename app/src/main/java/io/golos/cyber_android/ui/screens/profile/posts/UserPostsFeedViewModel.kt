package io.golos.cyber_android.ui.screens.profile.posts

import io.golos.cyber_android.ui.screens.feed.FeedPageTabViewModel
import io.golos.domain.interactors.action.VoteUseCase
import io.golos.domain.interactors.feed.UserPostFeedUseCase
import io.golos.domain.interactors.publish.DiscussionPosterUseCase
import io.golos.domain.interactors.sign.SignInUseCase
import io.golos.domain.requestmodel.PostFeedUpdateRequest
import javax.inject.Inject

class UserPostsFeedViewModel
@Inject
constructor(
    feedUseCase: UserPostFeedUseCase,
    voteUseCase: VoteUseCase,
    posterUseCase: DiscussionPosterUseCase,
    signInUseCase: SignInUseCase
) : FeedPageTabViewModel<PostFeedUpdateRequest>(feedUseCase, voteUseCase, posterUseCase, signInUseCase)