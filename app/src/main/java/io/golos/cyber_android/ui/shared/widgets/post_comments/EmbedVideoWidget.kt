package io.golos.cyber_android.ui.shared.widgets.post_comments

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.shared.glide.loadVideoContent
import io.golos.cyber_android.ui.shared.utils.prefetchScreenSize
import io.golos.domain.dto.ContentIdDomain
import io.golos.domain.posts_parsing_rendering.post_metadata.post_dto.VideoBlock
import kotlinx.android.synthetic.main.view_post_embed_video.view.*

class EmbedVideoWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
    BlockWidget<VideoBlock, EmbedVideoWidgetListener> {

    private var isNeedToShowHtmlContent: Boolean = true
    private var postContentId: ContentIdDomain? = null

    init {
        inflate(context, R.layout.view_post_embed_video, this)
    }

    fun disableHtmlContent() {
        isNeedToShowHtmlContent = false
    }

    fun setContentId(contentId: ContentIdDomain?) {
        postContentId = contentId
    }

    override fun setOnClickProcessor(processor: EmbedVideoWidgetListener?) {
        super.setOnClickProcessor(processor)
        if (!isNeedToShowHtmlContent) {
            setOnClickListener {
                postContentId?.let { id ->
                    processor?.onItemClicked(id)
                }
            }
        }
    }

    override fun render(block: VideoBlock) {

        if (!block.title.isNullOrEmpty()) {
            description.visibility = View.VISIBLE
            description.text = block.title
        } else {
            description.visibility = View.GONE
        }

        if (!block.author.isNullOrEmpty()) {
            subDescription.visibility = View.VISIBLE
            subDescription.text = block.author
        } else {
            subDescription.visibility = View.GONE
        }

        var html = block.html
        //var html = "<div><div style=\"left: 0; width: 100%; height: 0; position: relative; padding-bottom: 56.2493%;\"><iframe src=\"https://www.youtube.com/embed/gEZ1YK-peVM\" style=\"border: 0; top: 0; left: 0; width: 100%; height: 100%; position: absolute;\" allowfullscreen scrolling=\"no\"></iframe></div></div>"

        if (html != null && isNeedToShowHtmlContent) {
            html = correctHtml(html)

            video.visibility = View.VISIBLE
            imageContainer.visibility = View.GONE
            image.visibility = View.GONE
            providerName.visibility = View.GONE

            video.loadHtml(html)
        } else {
            video.visibility = View.GONE
            imageContainer.visibility = View.VISIBLE
            image.visibility = View.VISIBLE

            if (!block.providerName.isNullOrEmpty()) {
                providerName.visibility = View.VISIBLE
                providerName.text = block.providerName!!.capitalize()
            } else {
                providerName.visibility = View.GONE
            }
            val thumbnailUri = block.thumbnailUrl?.prefetchScreenSize(context)
            image.loadVideoContent(thumbnailUri?.toString())
        }
    }

    override fun release() {
        if (video.visibility == View.VISIBLE) {
            video.stopLoading()
        }

        if (image.visibility == View.VISIBLE) {
            Glide.with(this).clear(image)
        }

        setOnClickListener(null)
    }

    private fun correctHtml(html: String): String {
        var result = html

        val openTag = "<iframe"
        val closeTag = "</iframe>"

        val openTagIndex = result.indexOf(openTag)
        if (openTagIndex > 0) {
            result = result.removeRange(0 until openTagIndex)       // a head is cut
            result = result.removeRange(result.indexOf(closeTag) + closeTag.length until result.length)     // a tail is cut
        }

        result = result.replace("allowfullscreen", "")

        return result
    }
}