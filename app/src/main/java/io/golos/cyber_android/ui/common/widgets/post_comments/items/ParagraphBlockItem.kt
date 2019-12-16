package io.golos.cyber_android.ui.common.widgets.post_comments.items

import android.content.Context
import io.golos.cyber_android.ui.common.base.adapter.RecyclerItem
import io.golos.cyber_android.ui.common.widgets.post_comments.ParagraphWidget
import io.golos.cyber_android.ui.common.widgets.post_comments.ParagraphWidgetListener
import io.golos.cyber_android.ui.dto.ContentId
import io.golos.domain.use_cases.post.post_dto.ParagraphBlock

class ParagraphBlockItem(
    val paragraphBlock: ParagraphBlock,
    widgetListener: ParagraphWidgetListener?,
    val contentId: ContentId
) : BaseBlockItem<ParagraphBlock, ParagraphWidgetListener, ParagraphWidget>(
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
        if (item is ParagraphBlockItem) {
            return paragraphBlock == item.paragraphBlock
        }
        return false
    }
}