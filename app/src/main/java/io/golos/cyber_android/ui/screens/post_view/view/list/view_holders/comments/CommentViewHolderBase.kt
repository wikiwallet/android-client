package io.golos.cyber_android.ui.screens.post_view.view.list.view_holders.comments

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.dto.DonateType
import io.golos.cyber_android.ui.screens.post_view.dto.post_list_items.CommentListItem
import io.golos.cyber_android.ui.screens.post_view.dto.post_list_items.CommentListItemState
import io.golos.cyber_android.ui.screens.post_view.helpers.CommentTextRenderer
import io.golos.cyber_android.ui.screens.post_view.view_model.PostPageViewModelListEventsProcessor
import io.golos.cyber_android.ui.shared.base.adapter.BaseRecyclerItem
import io.golos.cyber_android.ui.shared.base.adapter.RecyclerAdapter
import io.golos.cyber_android.ui.shared.characters.SpecialChars
import io.golos.cyber_android.ui.shared.glide.loadAvatar
import io.golos.cyber_android.ui.shared.recycler_view.ViewHolderBase
import io.golos.cyber_android.ui.shared.spans.ColorTextClickableSpan
import io.golos.cyber_android.ui.shared.utils.getStyledAttribute
import io.golos.cyber_android.ui.shared.widgets.post_comments.ParagraphWidgetListener
import io.golos.cyber_android.ui.shared.widgets.post_comments.items.*
import io.golos.cyber_android.ui.shared.widgets.post_comments.voting.VotingWidget
import io.golos.domain.dto.*
import io.golos.domain.posts_parsing_rendering.post_metadata.TextStyle
import io.golos.domain.posts_parsing_rendering.post_metadata.post_dto.*
import io.golos.utils.format.TimeEstimationFormatter
import io.golos.utils.getColorRes
import io.golos.utils.helpers.SPACE
import io.golos.utils.helpers.appendSpannedText
import io.golos.utils.helpers.appendText
import io.golos.utils.helpers.setSpan
import io.golos.utils.id.IdUtil
import kotlinx.android.synthetic.main.view_post_voting.view.*
import javax.inject.Inject

@Suppress("PropertyName")
abstract class CommentViewHolderBase<T : CommentListItem>(parentView: ViewGroup, private val commentsViewPool: RecyclerView.RecycledViewPool) : ViewHolderBase<PostPageViewModelListEventsProcessor, T>(parentView, R.layout.item_comment) {
    private val maxStringLenToCutNeeded = 285

    @ColorInt
    private val spansColor = parentView.context.resources.getColorRes(R.color.default_clickable_span_color)

    @ColorInt
    private val moreLabelColor = parentView.context.resources.getColorRes(R.color.dark_gray)

    private val commentContentAdapter: RecyclerAdapter = RecyclerAdapter()

    @Inject
    internal lateinit var commentTextRenderer: CommentTextRenderer

    abstract val _userAvatar: ImageView

    abstract val _voting: VotingWidget

    abstract val _content: RecyclerView

    abstract val _replyAndTimeText: TextView

    abstract val _donateText: TextView

    abstract val _commentUserName: TextView

    abstract val _donateCoin: ImageView

    abstract val _processingProgress: ProgressBar

    abstract val _warning: ImageView

    abstract val _rootView: View

    init {
        @Suppress("LeakingThis") inject()
    }

    @CallSuper
    override fun init(listItem: T, listItemEventsProcessor: PostPageViewModelListEventsProcessor) {
        loadAvatarIcon(listItem.author.avatarUrl)
        val longClickListener = View.OnLongClickListener {
            if (!listItem.isDeleted && listItem.state != CommentListItemState.PROCESSING) {
                listItemEventsProcessor.onCommentLongClick(listItem.externalId)
            }
            true
        }
        _commentUserName.text= listItem.author.username
        listItem.donations?.run {
            _donateCoin.visibility=View.VISIBLE
        }?: run {
            _donateCoin.visibility=View.GONE
        }
        if(!listItem.isMyComment){
            _donateText.text =
                getDonate(itemView.context.applicationContext.resources.getString(R.string.donate), itemView.context.applicationContext)
            _donateText.setOnClickListener {
                listItemEventsProcessor.onDonateClick(DonateType.DONATE_OTHER, listItem.externalId, listItem.externalId.communityId, listItem.author)
            }
        }

        setupCommentContent(listItem, listItemEventsProcessor, longClickListener)

        itemView.setOnLongClickListener(longClickListener)
        _replyAndTimeText.text =
            getTimeAndReplay(listItem.metadata,itemView.context.applicationContext)
        _replyAndTimeText.setOnClickListener {
            listItemEventsProcessor.startReplyToComment(listItem.externalId)
        }

        _userAvatar.setOnClickListener { listItemEventsProcessor.onUserClicked(listItem.author.userId.userId) }

        setupVoting(listItem, listItemEventsProcessor)
        setupDonation(listItem, listItemEventsProcessor)

        setProcessingState(listItem.state)
    }

    @CallSuper
    override fun release() {
        _rootView.setOnLongClickListener(null)
        _voting.release()
    }

    abstract fun inject()

    abstract fun getParentAuthor(listItem: T): UserBriefDomain?

    private fun loadAvatarIcon(avatarUrl: String?) = _userAvatar.loadAvatar(avatarUrl)

    private fun setupCommentContent(listItem: T, listItemEventsProcessor: PostPageViewModelListEventsProcessor, longClickListener: View.OnLongClickListener) {
        _content.apply {
            adapter = commentContentAdapter
            setRecycledViewPool(commentsViewPool)
            layoutManager = object : LinearLayoutManager(itemView.context) {

                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        }
        val body = listItem.content
        val labelCommentDeleted = itemView.context.getString(R.string.comment_deleted)
        val contentList: List<Block> = body?.content ?: arrayListOf()
        var newContentList = contentList.toMutableList()

        ((body?.attachments) as? Block)?.let {
            newContentList.add(it)
        }


        val author =
            UserBriefDomain(listItem.author.avatarUrl, UserIdDomain(listItem.author.userId.userId), listItem.author.username)
        if (newContentList.isEmpty() && listItem.isDeleted) {
            _voting.visibility = View.GONE
            _replyAndTimeText.visibility = View.GONE
            val deleteBlock =
                ParagraphBlock(IdUtil.generateLongId(), arrayListOf(SpanableBlock(getAuthorAndText(author, labelCommentDeleted, listItemEventsProcessor)))) as Block
            newContentList.add(deleteBlock)
        } else {
            addAuthorNameToContent(newContentList, author, listItemEventsProcessor)
        }
        val discussionId = listItem.externalId
        val contentId = ContentIdDomain(CommunityIdDomain(""), discussionId.permlink, discussionId.userId)

        newContentList = joinParagraphs(newContentList)

        val contentItems =
            newContentList.filter { createPostBodyItem(contentId, it, listItemEventsProcessor, longClickListener) != null }.map {
                createPostBodyItem(contentId, it, listItemEventsProcessor, longClickListener)!!
            }

        commentContentAdapter.updateAdapter(contentItems)
    }

    private fun joinParagraphs(source: List<Block>): MutableList<Block> {
        val result = mutableListOf<Block>()

        var blocksList: MutableList<ParagraphBlock>? = null
        var blocksSet: ParagraphSet? = null

        source.forEach {
            if (it is ParagraphBlock) {
                if (blocksSet == null) {
                    blocksList = mutableListOf()
                    blocksSet = ParagraphSet(blocksList!!)
                    result.add(blocksSet!!)
                }
                blocksList!!.add(it)
            } else {
                result.add(it)
            }
        }

        return result
    }

    private fun addAuthorNameToContent(newContentList: MutableList<Block>, author: UserBriefDomain, paragraphWidgetListener: ParagraphWidgetListener) {
        val findBlock = newContentList.find { it is TextBlock || it is ParagraphBlock }
        val authorBlock =
            ParagraphBlock(null, arrayListOf(SpanableBlock(getAuthorAndText(author, "", paragraphWidgetListener)))) as Block

        if (findBlock == null) {
            newContentList.add(0, authorBlock)
        } else {
            val indexOf = newContentList.indexOf(findBlock)
            if (indexOf == 0) {
                if (findBlock is TextBlock) {
                    newContentList[0] =
                        ParagraphBlock(null, arrayListOf(SpanableBlock(getAuthorAndText(author, findBlock.content, paragraphWidgetListener)))) as Block
                } else {
                    if (findBlock is ParagraphBlock) {
                        if (findBlock.content.isNotEmpty()) {
                            val paragraphContent = mutableListOf<ParagraphItemBlock>()

                            paragraphContent.add(TextBlock(IdUtil.generateLongId(), (author.username
                                ?: author.userId.userId) + " ", TextStyle.BOLD, null))
                            findBlock.content.forEach { block -> paragraphContent.add(block) }

                            val newParagraph = ParagraphBlock(null, paragraphContent)
                            newContentList[0] = newParagraph
                        } else {
                            newContentList[0] = authorBlock
                        }
                    }
                }
            } else {
                newContentList.add(0, authorBlock)
            }
        }
    }

    private fun createPostBodyItem(contentId: ContentIdDomain, block: Block, listItemEventsProcessor: PostPageViewModelListEventsProcessor, longClickListener: View.OnLongClickListener): BaseRecyclerItem? {
        return when (block) {
            is AttachmentsBlock -> {
                if (block.content.size == 1) {
                    createPostBodyItem(contentId, block.content.single(), listItemEventsProcessor, longClickListener) // A single attachment is shown as embed block
                } else {
                    AttachmentBlockItem(block, listItemEventsProcessor)
                }
            }

            is ImageBlock -> CommentImageBlockItem(imageBlock = block, widgetListener = listItemEventsProcessor, onLongClickListener = longClickListener)

            is VideoBlock -> VideoBlockItem(videoBlock = block, widgetListener = listItemEventsProcessor)

            is WebsiteBlock -> WebSiteBlockItem(block, listItemEventsProcessor)

            is ParagraphSet -> CommentParagraphSetItem(block, listItemEventsProcessor, contentId, onLongClickListener = longClickListener)

            is RichBlock -> CommentRichBlockItem(block, contentId, listItemEventsProcessor)

            is EmbedBlock -> CommentEmbedBlockItem(block, contentId, listItemEventsProcessor)

            else -> null
        }
    }

    private fun getAuthorAndText(author: UserBriefDomain, text: String, paragraphWidgetListener: ParagraphWidgetListener): SpannableStringBuilder {
        val result = SpannableStringBuilder()
        author.username?.let {
            val userNameInterval = result.appendText(it)
            result.setSpan(StyleSpan(Typeface.BOLD), userNameInterval)
            val colorUserName = getStyledAttribute(R.attr.comment_user_name)
            result.setSpan(object : ColorTextClickableSpan(author.userId.userId, colorUserName) {

                override fun onClick(spanData: String) {
                    super.onClick(spanData)
                    paragraphWidgetListener.onUserClicked(spanData)
                }
            }, userNameInterval)
        }

        result.append(SPACE)
        result.append(text)
        return result
    }

    private fun setupVoting(listItem: T, eventsProcessor: PostPageViewModelListEventsProcessor) {
        _voting.setVoteBalance(listItem.voteBalance)
        _voting.setUpVoteButtonSelected(listItem.isUpVoteActive)
        _voting.setDownVoteButtonSelected(listItem.isDownVoteActive)

        if (listItem.currentUserId != listItem.author.userId) {
            _voting.upvoteButton.isEnabled = true
            _voting.downvoteButton.isEnabled = true

            _voting.setOnUpVoteButtonClickListener {
                if (listItem.isUpVoteActive) {
                    eventsProcessor.onCommentUnVoteClick(listItem.externalId)
                } else {
                    eventsProcessor.onCommentUpVoteClick(listItem.externalId)
                }

            }
            _voting.setOnDownVoteButtonClickListener {
                if (listItem.isDownVoteActive) {
                    eventsProcessor.onCommentUnVoteClick(listItem.externalId)
                } else {
                    eventsProcessor.onCommentDownVoteClick(listItem.externalId)
                }

            }
            _voting.setOnDonateClickListener {
                eventsProcessor.onDonateClick(it, listItem.externalId, listItem.externalId.communityId, listItem.author)
            }
        } else {
            _voting.upvoteButton.isActivated = true
            _voting.setOnUpVoteButtonClickListener { eventsProcessor.onForbiddenClick() }
            _voting.setOnDownVoteButtonClickListener { eventsProcessor.onForbiddenClick() }
        }
    }

    private fun setupDonation(listItem: T, listItemEventsProcessor: PostPageViewModelListEventsProcessor) {

    }

    private fun getReplyAndTimeText(context: Context, metadata: MetaDomain): SpannableStringBuilder {
        val result = SpannableStringBuilder()

        // Reply label
        val replySpan = object : ColorTextClickableSpan("", spansColor) {
            override fun onClick(spanData: String) {
                // Reply
            }
        }
        result.appendSpannedText(context.resources.getString(R.string.reply), replySpan)

        // Time
        val time = TimeEstimationFormatter.format(metadata.creationTime, context)
        result.append(" ${SpecialChars.BULLET} ")
        result.append(time)

        return result
    }


    private fun getDonate(donate: String, context: Context): SpannableStringBuilder {

        val result = SpannableStringBuilder()
        val timeColor = ContextCompat.getColor(context, R.color.post_header_time_text)

        val bulletSymbol = " ${SpecialChars.BULLET} "
        val bulletInterval = result.appendText(bulletSymbol)

        result.setSpan(object : ColorTextClickableSpan(bulletSymbol, timeColor) {

            override fun onClick(widget: View) {

            }

        }, bulletInterval)


        val donateTextColor = ContextCompat.getColor(context, R.color.post_header_user_name_text)
        val donateInterval = result.appendText(donate)
        result.setSpan(object : ColorTextClickableSpan(donate, donateTextColor) {
            override fun onClick(widget: View) {

            }
        }, donateInterval)

        return result
    }

    private fun getTimeAndReplay(metadata: MetaDomain, context: Context): SpannableStringBuilder {

        val result = SpannableStringBuilder()
        val time = TimeEstimationFormatter.format(metadata.creationTime, context)
        val timeInterval = result.appendText(time)

        val timeColor = ContextCompat.getColor(context, R.color.post_header_time_text)

        result.setSpan(object : ColorTextClickableSpan(time, timeColor) {

            override fun onClick(widget: View) {

            }

        }, timeInterval)

        val bulletSymbol = " ${SpecialChars.BULLET} "
        val bulletInterva = result.appendText(bulletSymbol)

        result.setSpan(object : ColorTextClickableSpan(bulletSymbol, timeColor) {

            override fun onClick(widget: View) {

            }

        }, bulletInterva)

        // Reply label
        val replySpan = object : ColorTextClickableSpan("", spansColor) {
            override fun onClick(spanData: String) {

            }
        }
        result.appendSpannedText(context.resources.getString(R.string.reply), replySpan)


        return result
    }

    private fun setProcessingState(state: CommentListItemState) {
        _processingProgress.visibility = if (state == CommentListItemState.PROCESSING) View.VISIBLE else View.INVISIBLE
        _warning.visibility = if (state == CommentListItemState.ERROR) View.VISIBLE else View.INVISIBLE
    }
}