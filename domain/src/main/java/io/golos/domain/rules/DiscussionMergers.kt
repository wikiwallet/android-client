package io.golos.domain.rules

import io.golos.domain.dto.*
import javax.inject.Inject

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-03-13.
 */
class PostMerger
@Inject
constructor() : EntityMerger<PostEntity> {

    override fun invoke(new: PostEntity, old: PostEntity): PostEntity {
        return PostEntity(
            new.contentId, new.author, new.community,
            PostContent(
                new.content.body,
                new.content.tags
            ),
            new.votes, new.comments, new.payout, new.meta, new.stats,
            new.shareUrl
        )
    }
}

class PostFeedMerger
@Inject
constructor() : EntityMerger<FeedRelatedData<PostEntity>> {
    override fun invoke(
        new: FeedRelatedData<PostEntity>,
        old: FeedRelatedData<PostEntity>
    ): FeedRelatedData<PostEntity> {
        val oldFeed = old.feed
        val newFeed = new.feed

        if (newFeed.discussions.isEmpty()) return new
        if (oldFeed.discussions.isEmpty()) return new
        if (newFeed.pageId == null) return new

        val firstOfNew = newFeed.discussions.first()

        if (oldFeed.discussions.any { it.contentId == firstOfNew.contentId }) {
            val lastPos = oldFeed.discussions.indexOfLast { it.contentId == firstOfNew.contentId }
            return FeedRelatedData(
                FeedEntity(
                    oldFeed.discussions.subList(0, lastPos) + newFeed.discussions,
                    oldFeed.nextPageId,
                    newFeed.nextPageId
                ),
                emptySet()
            )
        } else if (oldFeed.nextPageId == newFeed.pageId) {
            return FeedRelatedData(
                FeedEntity(
                    oldFeed.discussions + newFeed.discussions,
                    oldFeed.nextPageId,
                    newFeed.nextPageId
                ), emptySet()
            )
        }
        return old
    }
}


class CommentMerger
@Inject
constructor() : EntityMerger<CommentEntity> {

    override fun invoke(new: CommentEntity, old: CommentEntity): CommentEntity {
        return CommentEntity(
            new.contentId, new.author,
            CommentContent(
                new.content.body
            ),
            new.votes, new.payout,
            new.parentCommentId, new.meta, new.stats
        )
    }
}

class CommentFeedMerger
@Inject
constructor() : EntityMerger<FeedRelatedData<CommentEntity>> {
    override fun invoke(
        new: FeedRelatedData<CommentEntity>,
        old: FeedRelatedData<CommentEntity>
    ): FeedRelatedData<CommentEntity> {

        val oldFeed = old.feed
        val newFeed = new.feed

        if (newFeed.discussions.isEmpty()) return old
        if (oldFeed.discussions.isEmpty()) return new
        if (newFeed.pageId == null) return FeedRelatedData(new.feed, emptySet())


        val newDiscussions = newFeed.discussions.filter { !new.fixedPositionEntities.contains(it) }

        return FeedRelatedData(
            FeedEntity(oldFeed.discussions + newDiscussions, oldFeed.nextPageId, newFeed.nextPageId),
            new.fixedPositionEntities
        )
    }
}