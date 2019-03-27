package io.golos.domain

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import io.golos.domain.entities.DiscussionEntity
import io.golos.domain.entities.DiscussionIdEntity
import io.golos.domain.entities.FeedEntity
import io.golos.domain.model.FeedUpdateRequest
import io.golos.domain.model.Identifiable
import io.golos.domain.model.QueryResult

/**
 * Created by yuri yurivladdurain@gmail.com on 11/03/2019.
 * basic class for all repositories
 * D is class of entities to store and update
 * Q is a query parameter for updating entity/entities
 */
interface Repository<D : Entity, Q : Identifiable> {

    val allDataRequest: Q

    @MainThread
            /** get some particular entity for [params] request
             *  repository obliged to give all it's data in response on [allDataRequest]
             * */
    fun getAsLiveData(params: Q): LiveData<D>

    /** make primary action (update, query, vote), depending on type of
     * a repository. Actions are qed, canceled and exposed
     * by [params.id] property.
     * */
    @AnyThread
    fun makeAction(params: Q)

    /**progress of [makeAction] queries. Identifiable.Id here is Q.id class
     * */
    val updateStates: LiveData<Map<Identifiable.Id, QueryResult<Q>>>


}

interface DiscussionsFeedRepository<T : DiscussionEntity, Q : FeedUpdateRequest>
    : Repository<FeedEntity<T>, Q> {

    @AnyThread
    fun requestDiscussionUpdate(updatingDiscussionId: DiscussionIdEntity)

    @MainThread
    fun getDiscussionAsLiveData(discussionIdEntity: DiscussionIdEntity): LiveData<T>

}


