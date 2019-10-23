package io.golos.cyber_android.ui.shared_fragments.post.view.list.view_holders

import android.view.ViewGroup
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.common.recycler_view.ViewHolderBase
import io.golos.cyber_android.ui.shared_fragments.post.dto.post_list_items.FirstLevelCommentRetryListItem
import io.golos.cyber_android.ui.shared_fragments.post.view_model.PostPageViewModelListEventsProcessor
import kotlinx.android.synthetic.main.item_post_comments_retry.view.*

class FirstLevelCommentRetryViewHolder(
    parentView: ViewGroup
) : ViewHolderBase<PostPageViewModelListEventsProcessor, FirstLevelCommentRetryListItem>(
    parentView,
    R.layout.item_post_comments_retry
) {
    override fun init(listItem: FirstLevelCommentRetryListItem, listItemEventsProcessor: PostPageViewModelListEventsProcessor) {
        itemView.pageLoadingRetryButton.setOnClickListener {
            listItemEventsProcessor.onRetryLoadingFirstLevelCommentButtonClick()
        }
    }

    override fun release() {
        super.release()
        itemView.pageLoadingRetryButton.setOnClickListener(null)
    }
}