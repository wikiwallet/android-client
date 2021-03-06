package io.golos.cyber_android.ui.shared.widgets.post_comments.items

import android.content.Context
import io.golos.cyber_android.generated.callback.OnClickListener
import io.golos.cyber_android.ui.shared.base.adapter.RecyclerItem
import io.golos.cyber_android.ui.shared.utils.getScreenSize
import io.golos.cyber_android.ui.shared.widgets.post_comments.EmbedWidget
import io.golos.cyber_android.ui.shared.widgets.post_comments.EmbedWidgetListener
import io.golos.domain.dto.ContentIdDomain
import io.golos.domain.posts_parsing_rendering.post_metadata.post_dto.EmbedBlock

class PostEmbedBlockItem(
    val embedBlock: EmbedBlock,
    val contentId: ContentIdDomain,
    widgetListener: EmbedWidgetListener? = null,
    onClickListener: OnClickListener? = null
) : BaseBlockItem<EmbedBlock, EmbedWidgetListener, EmbedWidget>(
    embedBlock,
    widgetListener,
    onClickListener = onClickListener
) {

    override fun createWidget(
        context: Context
    ): EmbedWidget = EmbedWidget(context).apply {
        setContentId(contentId)
        setWidthBlock(context.getScreenSize().x)
    }

    override fun areItemsTheSame(): Int = embedBlock.hashCode()

    override fun areContentsSame(item: RecyclerItem): Boolean {
        if (item is PostEmbedBlockItem) {
            return embedBlock == item.embedBlock
        }
        return false
    }
}