package io.golos.data.mappers

import io.golos.commun4j.services.model.UserSubscriptionItem
import io.golos.domain.dto.FollowingUserDomain
import io.golos.domain.dto.UserDomain
import io.golos.domain.dto.UserIdDomain

fun UserSubscriptionItem.mapToFollowingUserDomain(): FollowingUserDomain =
    FollowingUserDomain(
        user = UserDomain(
            userId = UserIdDomain(userId.name),
            userName = username!!,
            userAvatar = avatarUrl,
            postsCount = postsCount,
            followersCount = subscribersCount,
            isSubscribed = isSubscribed ?: false
        ),
        isBlocked = true,
        isInBlacklist = true
    )
