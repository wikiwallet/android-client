package io.golos.cyber_android.ui.screens.my_feed.view.items

import android.content.Context
import android.view.View
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.common.base.adapter.BaseRecyclerItem
import io.golos.cyber_android.ui.dto.document.WebSite

class WebSitePostItem(
    private val webSite: WebSite
) : BaseRecyclerItem() {

    override fun getLayoutId(): Int = R.layout.item_feed_link

    override fun renderView(context: Context, view: View) {
        super.renderView(context, view)
    }
}