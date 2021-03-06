package io.golos.cyber_android.ui.screens.post_view.dto.post_list_items

import io.golos.cyber_android.ui.shared.recycler_view.GroupListItem
import io.golos.cyber_android.ui.shared.recycler_view.versioned.VersionedListItem

data class SecondLevelCommentLoadingListItem(
    override val id: Long,
    override val version: Long,
    override val isFirstItem: Boolean = false,
    override val isLastItem: Boolean = false,

    override val groupId: Int = 5
) : GroupListItem, VersionedListItem