package io.golos.data.repositories.discussion_creation

import io.golos.commun4j.abi.implementation.comn.gallery.CreatemssgComnGalleryStruct
import io.golos.commun4j.abi.implementation.comn.gallery.DeletemssgComnGalleryStruct
import io.golos.commun4j.abi.implementation.comn.gallery.UpdatemssgComnGalleryStruct
import io.golos.data.api.DiscussionsCreationApi
import io.golos.data.api.TransactionsApi
import io.golos.domain.DispatchersProvider
import io.golos.domain.entities.DiscussionCreationResultEntity
import io.golos.domain.mappers.discussion_creation.request.DiscussionCreationRequestMapper
import io.golos.domain.mappers.discussion_creation.result.DiscussionCreateResultToEntityMapper
import io.golos.domain.mappers.discussion_creation.result.DiscussionDeleteResultToEntityMapper
import io.golos.domain.mappers.discussion_creation.result.DiscussionUpdateResultToEntityMapper
import io.golos.domain.requestmodel.*
import kotlinx.coroutines.withContext

abstract class DiscussionCreationRepositoryBase(
    private val dispatchersProvider: DispatchersProvider,
    private val discussionsCreationApi: DiscussionsCreationApi,
    private val transactionsApi: TransactionsApi
) {
    protected suspend fun createOrUpdateDiscussion(params: DiscussionCreationRequestEntity): DiscussionCreationResultEntity =
        withContext(dispatchersProvider.ioDispatcher) {
            val request = DiscussionCreationRequestMapper.map(params)
            val apiAnswer = when (request) {
                is CreateCommentRequest -> discussionsCreationApi.createComment(
                    request.body,
                    request.parentAccount,
                    request.parentPermlink,
                    request.category,
                    request.metadata,
                    request.beneficiaries,
                    request.vestPayment,
                    request.tokenProp
                )
                is CreatePostRequest -> discussionsCreationApi.createPost(
                    request.title, request.body, request.tags,
                    request.metadata, request.beneficiaries, request.vestPayment, request.tokenProp
                )
                is UpdatePostRequest -> discussionsCreationApi.updatePost(
                    request.postPermlink, request.title, request.body,
                    request.tags, request.metadata
                )
                is DeleteDiscussionRequest -> discussionsCreationApi.deletePostOrComment(request.permlink)
            }

            transactionsApi.waitForTransaction(apiAnswer.first.transaction_id)

            when (request) {
                is UpdatePostRequest -> DiscussionUpdateResultToEntityMapper.map(apiAnswer.second as UpdatemssgComnGalleryStruct)
                is DeleteDiscussionRequest -> DiscussionDeleteResultToEntityMapper.map(apiAnswer.second as DeletemssgComnGalleryStruct)
                else -> DiscussionCreateResultToEntityMapper.map(apiAnswer.second as CreatemssgComnGalleryStruct)
            }
        }
}