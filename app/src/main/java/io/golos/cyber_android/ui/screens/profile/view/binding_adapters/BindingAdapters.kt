package io.golos.cyber_android.ui.screens.profile.view.binding_adapters

import android.annotation.SuppressLint
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.shared.glide.loadAvatar
import io.golos.cyber_android.ui.shared.glide.loadCover
import io.golos.cyber_android.ui.shared.extensions.parentActivity
import io.golos.utils.format.KiloCounterFormatter
import io.golos.cyber_android.ui.dto.ProfileCommunities
import io.golos.cyber_android.ui.shared.utils.toMMMM_DD_YYYY_Format
import java.util.*

@BindingAdapter("avatar")
fun setAvatarBinding(view: ImageView, valueToBind: LiveData<String?>?) =
    bind(view, valueToBind) { viewToLoad, url -> viewToLoad.loadAvatar(url)}

@BindingAdapter("cover")
fun setCoverBinding(view: ImageView, valueToBind: LiveData<String?>?) =
    bind(view, valueToBind) { viewToLoad, url -> viewToLoad.loadCover(url) }

fun bind(view: ImageView, valueToBind: LiveData<String?>?, bingAction: (ImageView, url: String?) -> Unit) {
    valueToBind?.let { value ->
        view.parentActivity?.let { activity ->
            value.observe(activity, Observer { observedUrl ->
                bingAction(view, observedUrl)
            })
        }
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("joined")
fun setJoinedDateBinding(view: TextView, valueToBind: LiveData<Date>?) {
    valueToBind?.let { value ->
        view.parentActivity?.let { activity ->
            value.observe(activity, Observer { date ->
                view.text = "${view.context.resources.getString(R.string.joined)} ${date.toMMMM_DD_YYYY_Format()}"
            })
        }
    }
}

@BindingAdapter("kilo")
fun setKiloBinding(view: TextView, valueToBind: LiveData<Int>?) {
    valueToBind?.let { value ->
        view.parentActivity?.let { activity ->
            value.observe(activity, Observer {
                view.text = KiloCounterFormatter.format(it)
            })
        }
    }
}

@BindingAdapter("communitiesVisibility")
fun setCommunitiesVisibility(view: FrameLayout, valueToBind: LiveData<ProfileCommunities?>?) {
    valueToBind?.let { value ->
        view.parentActivity?.let { activity ->
            value.observe(activity, Observer { communities ->
                view.visibility = if(communities != null && communities.highlightCommunities.isNotEmpty()) View.VISIBLE else View.GONE
            })
        }
    }
}