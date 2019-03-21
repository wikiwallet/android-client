package io.golos.domain.model

import io.golos.domain.Model
import io.golos.domain.interactors.model.DiscussionIdModel

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-03-21.
 */
sealed class VoteRequest(val power: Short, val discussionIdEntity: DiscussionIdModel) : Model {

    class VoteForPostRequest(power: Short, discussionIdEntity: DiscussionIdModel) :
        VoteRequest(power, discussionIdEntity)

    class VoteForComentRequest(power: Short, discussionIdEntity: DiscussionIdModel) :
        VoteRequest(power, discussionIdEntity)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VoteRequest

        if (power != other.power) return false
        if (discussionIdEntity != other.discussionIdEntity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = power.toInt()
        result = 31 * result + discussionIdEntity.hashCode()
        return result
    }

    override fun toString(): String {
        return "VoteRequest(power=$power, discussionIdEntity=$discussionIdEntity)"
    }

}