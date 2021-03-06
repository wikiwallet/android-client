package io.golos.cyber_android.ui.screens.post_view.view.list.view_holders

import android.view.ViewGroup
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.screens.post_view.dto.post_list_items.PostControlsListItem
import io.golos.cyber_android.ui.screens.post_view.view_model.PostPageViewModelListEventsProcessor
import io.golos.cyber_android.ui.shared.recycler_view.ViewHolderBase
import io.golos.utils.format.KiloCounterFormatter
import kotlinx.android.synthetic.main.item_post_controls.view.*
import kotlinx.android.synthetic.main.item_post_controls.view.votesArea
import kotlinx.android.synthetic.main.view_post_voting.view.*

class PostControlsViewHolder(parentView: ViewGroup) : ViewHolderBase<PostPageViewModelListEventsProcessor, PostControlsListItem>(parentView, R.layout.item_post_controls) {
    override fun init(listItem: PostControlsListItem, listItemEventsProcessor: PostPageViewModelListEventsProcessor) {
        with(itemView) {
            votesArea.setVoteBalance(listItem.voteBalance)

            viewCountsText.text = KiloCounterFormatter.format(listItem.totalViews)
            commentsCountText.text = KiloCounterFormatter.format(listItem.totalComments)

            votesArea.setUpVoteButtonSelected(listItem.isUpVoteActive)
            votesArea.setDownVoteButtonSelected(listItem.isDownVoteActive)

            if (!listItem.post.isMyPost) {
                votesArea.upvoteButton.isEnabled = true
                votesArea.downvoteButton.isEnabled = true

                votesArea.setOnUpVoteButtonClickListener {
                    if (listItem.post.votes.hasUpVote) {
                        listItemEventsProcessor.onUnVoteClick()
                    } else {
                        listItemEventsProcessor.onUpVoteClick()
                    }
                }

                votesArea.setOnDownVoteButtonClickListener {
                    if (listItem.post.votes.hasDownVote) {
                        listItemEventsProcessor.onUnVoteClick()
                    } else {
                        listItemEventsProcessor.onDownVoteClick()
                    }
                }
                votesArea.setOnDonateClickListener {
                    listItemEventsProcessor.onDonateClick(it, listItem.post.contentId, listItem.post.community.communityId, listItem.post.author)
                }
            } else {
                votesArea.upvoteButton.isActivated = true
                votesArea.downvoteButton.isEnabled = false
            }

            viewCountText.text = KiloCounterFormatter.format(listItem.post.viewCount)

            donationPanel.setAmount(listItem.post)
            donationPanel.setOnClickListener {
                listItemEventsProcessor.onDonatePopupClick(listItem.post)
            }
            donationPanel.setDonateUserListListener { donateType, contentId, communityId, author ->
                listItemEventsProcessor.onDonateClick(donateType, contentId, communityId, author)
            }

            ivShare.setOnClickListener {
                listItem.shareUrl?.let {
                    listItemEventsProcessor.onShareClicked(it)
                }
            }
        }
    }

    override fun release() {
        itemView.votesArea.setOnUpVoteButtonClickListener(null)
        itemView.votesArea.setOnDownVoteButtonClickListener(null)
        itemView.votesArea.setOnDonateClickListener(null)
        itemView.donationPanel.setOnClickListener(null)
    }
}