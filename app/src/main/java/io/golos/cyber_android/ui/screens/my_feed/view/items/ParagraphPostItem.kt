package io.golos.cyber_android.ui.screens.my_feed.view.items

import android.content.Context
import io.golos.cyber_android.ui.common.base.adapter.RecyclerItem
import io.golos.cyber_android.ui.common.widgets.post.ParagraphWidget
import io.golos.cyber_android.ui.common.widgets.post.ParagraphWidgetListener
import io.golos.cyber_android.ui.dto.Post
import io.golos.domain.use_cases.post.post_dto.ParagraphBlock

class ParagraphPostItem(
    val paragraphBlock: ParagraphBlock,
    widgetListener: ParagraphWidgetListener?,
    val contentId: Post.ContentId
) : BasePostBlockItem<ParagraphBlock, ParagraphWidgetListener, ParagraphWidget>(
    paragraphBlock,
    widgetListener
) {

    override fun createWidgetView(
        context: Context
    ): ParagraphWidget = ParagraphWidget(context).apply {
        setSeeMoreEnabled(true)
        setContentId(contentId)
    }

    override fun areItemsTheSame(): Int = paragraphBlock.hashCode()

    override fun areContentsSame(item: RecyclerItem): Boolean {
        if (item is ParagraphPostItem) {
            return paragraphBlock == item.paragraphBlock
        }
        return false
    }
}