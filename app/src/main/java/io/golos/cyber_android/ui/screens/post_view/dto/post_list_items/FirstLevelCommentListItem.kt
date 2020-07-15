package io.golos.cyber_android.ui.screens.post_view.dto.post_list_items

import io.golos.cyber_android.ui.shared.recycler_view.versioned.VersionedListItem
import io.golos.domain.dto.*
import io.golos.domain.posts_parsing_rendering.post_metadata.post_dto.ContentBlock

data class FirstLevelCommentListItem(
    override val id: Long,
    override val version: Long,
    override val isFirstItem: Boolean,
    override val isLastItem: Boolean,

    override val externalId: ContentIdDomain,          // Id of an entity on the backend

    override val author: UserBriefDomain,
    override val currentUserId: UserIdDomain,

    override val content: ContentBlock?,

    override val voteBalance: Long,
    override val isUpVoteActive: Boolean,
    override val isDownVoteActive: Boolean,

    override val metadata: MetaDomain,

    override val state: CommentListItemState,
    override val groupId: Int = 5,
    override val isDeleted: Boolean,

    override val donations: DonationsDomain?
) : CommentListItem, VersionedListItem