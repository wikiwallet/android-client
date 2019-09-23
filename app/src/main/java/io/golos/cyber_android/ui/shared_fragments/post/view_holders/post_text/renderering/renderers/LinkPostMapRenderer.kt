package io.golos.cyber_android.ui.shared_fragments.post.view_holders.post_text.renderering.renderers

import io.golos.cyber_android.ui.shared_fragments.post.view_holders.post_text.renderering.LinkType
import io.golos.cyber_android.ui.shared_fragments.post.view_holders.post_text.renderering.html_builder.HtmlBuilder
import io.golos.cyber_android.utils.tryString
import org.json.JSONObject
import java.lang.UnsupportedOperationException

class LinkPostMapRenderer(builder: HtmlBuilder) : RendererBase(builder) {
    override fun render(block: JSONObject) {
        val attributes = getAttributes(block) ?: throw IllegalArgumentException("Link attributes can't be empty")

        val type = attributes.getString("type")

        val imageUrl = when(type) {
            LinkType.IMAGE -> attributes.getString("url")
            LinkType.VIDEO -> attributes.tryString("thumbnail_url") ?: "file:///android_asset/video_stub.webp"
            LinkType.WEBSITE -> attributes.tryString("thumbnail_url") ?: "file:///android_asset/website_stub.webp"
            else -> throw UnsupportedOperationException("This type of link is not supported: $type")
        }

        builder.putPostMapBlock(imageUrl, getId(block))
    }
}