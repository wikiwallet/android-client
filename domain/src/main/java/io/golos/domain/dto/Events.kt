package io.golos.domain.dto

import io.golos.commun4j.services.model.EventsData
import io.golos.commun4j.sharedmodel.CyberName
import io.golos.domain.Entity
import io.golos.domain.requestmodel.EventsFeedUpdateRequest
import java.util.*

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-04-24.
 */
data class EventsListDataWithQuery(
    val data: EventsData,
    val query: EventsFeedUpdateRequest
) : Entity

data class EventsListEntity(
    val total: Int,
    val freshCount: Int,
    val queryLastItemId: String?,
    val data: List<EventEntity>
) : List<EventEntity> by data, Entity

sealed class EventEntity

data class VoteEventEntity(
    val actor: EventActorEntity,
    val post: EventPostEntity?,
    val comment: EventCommentEntity?,
    val community: CommunityEntity,
    val eventId: String,
    val isFresh: Boolean,
    val timestamp: Date
) : EventEntity()

data class FlagEventEntity(
    val actor: EventActorEntity,
    val post: EventPostEntity?,
    val comment: EventCommentEntity?,
    val community: CommunityEntity,
    val eventId: String,
    val isFresh: Boolean,
    val timestamp: Date
) : EventEntity()

data class TransferEventEntity(
    val value: EventValueEntity,
    val actor: EventActorEntity,
    val eventId: String,
    val isFresh: Boolean,
    val timestamp: Date
) : EventEntity()

data class SubscribeEventEntity(
    val community: CommunityEntity,
    val actor: EventActorEntity,
    val eventId: String,
    val isFresh: Boolean,
    val timestamp: Date
) : EventEntity()

data class UnSubscribeEventEntity(
    val community: CommunityEntity,
    val actor: EventActorEntity,
    val eventId: String,
    val isFresh: Boolean,
    val timestamp: Date
) : EventEntity()

data class ReplyEventEntity(
    val comment: EventCommentEntity,
    val parentPost: EventPostEntity?,
    val parentComment: EventCommentEntity?,
    val community: CommunityEntity,
    val actor: EventActorEntity,
    val eventId: String,
    val isFresh: Boolean,
    val timestamp: Date
) : EventEntity()

data class MentionEventEntity(
    val comment: EventCommentEntity?,
    val parentPost: EventPostEntity?,
    val parentComment: EventCommentEntity?,
    val community: CommunityEntity,
    val actor: EventActorEntity,
    val eventId: String,
    val isFresh: Boolean,
    val timestamp: Date
) : EventEntity()

data class RepostEventEntity(
    val post: EventPostEntity,
    val comment: EventCommentEntity?,
    val community: CommunityEntity,
    val actor: EventActorEntity,
    val eventId: String,
    val isFresh: Boolean,
    val timestamp: Date
) : EventEntity()

data class AwardEventEntity(
    val payout: EventValueEntity,
    val eventId: String,
    val isFresh: Boolean,
    val timestamp: Date
) : EventEntity()

data class CuratorAwardEventEntity(
    val post: EventPostEntity?,
    val comment: EventCommentEntity?,
    val payout: EventValueEntity,
    val community: CommunityEntity,
    val actor: EventActorEntity,
    val eventId: String,
    val isFresh: Boolean,
    val timestamp: Date
) : EventEntity()

data class WitnessVoteEventEntity(
    val actor: EventActorEntity,
    val eventId: String,
    val isFresh: Boolean,
    val timestamp: Date
) : EventEntity()

data class WitnessCancelVoteEventEntity(
    val actor: EventActorEntity,
    val eventId: String,
    val isFresh: Boolean,
    val timestamp: Date
) : EventEntity()


data class EventValueEntity(val amount: Double, val currency: String) : Entity

data class EventActorEntity(val id: CyberName, val avatarUrl: String?) : Entity

data class EventPostEntity(val contentId: DiscussionIdEntity, val title: String) : Entity

data class EventCommentEntity(val contentId: DiscussionIdEntity, val body: String) : Entity

enum class EventTypeEntity : Entity {
    VOTE, FLAG, TRANSFER, REPLY, SUBSCRIBE, UN_SUBSCRIBE,
    MENTION, REPOST, REWARD, CURATOR_REWARD, WITNESS_VOTE,
    WITNESS_CANCEL_VOTE;
}

