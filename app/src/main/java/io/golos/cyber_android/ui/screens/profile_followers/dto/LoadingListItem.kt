package io.golos.cyber_android.ui.screens.profile_followers.dto

import io.golos.cyber_android.ui.dto.FollowersFilter
import io.golos.cyber_android.ui.shared.recycler_view.versioned.VersionedListItem
import io.golos.utils.id.IdUtil

/**
 * List item for loading indicator
 */
data class LoadingListItem(
    val filter: FollowersFilter,
    override val id: Long = IdUtil.generateLongId(),
    override val version: Long = 0,
    override val isFirstItem: Boolean = false,
    override val isLastItem: Boolean = false
): VersionedListItem