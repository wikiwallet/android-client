package io.golos.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.golos.data.api.EmbedApi
import io.golos.domain.DispatchersProvider
import io.golos.domain.Logger
import io.golos.domain.Repository
import io.golos.domain.dependency_injection.scopes.ApplicationScope
import io.golos.domain.entities.LinkEmbedResult
import io.golos.domain.entities.ProcessedLinksEntity
import io.golos.domain.mappers.*
import io.golos.domain.requestmodel.EmbedRequest
import io.golos.domain.requestmodel.Identifiable
import io.golos.domain.requestmodel.QueryResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-04-01.
 */
@ApplicationScope
class EmbedsRepository
@Inject
constructor(
    private val embedApi: EmbedApi,
    private val dispatchersProvider: DispatchersProvider,
    private val logger: Logger
) : Repository<ProcessedLinksEntity, EmbedRequest> {

    private val repositoryScope = CoroutineScope(dispatchersProvider.uiDispatcher + SupervisorJob())

    private val savedEmbeds = MutableLiveData<ProcessedLinksEntity>()
    private val embedUpdateStates = MutableLiveData<Map<Identifiable.Id, QueryResult<EmbedRequest>>>()

    private val allRequest = EmbedRequest("##all_data##")

    override val allDataRequest: EmbedRequest
        get() = allRequest

    override fun getAsLiveData(params: EmbedRequest): LiveData<ProcessedLinksEntity> {

        return savedEmbeds
    }

    override fun makeAction(params: EmbedRequest) {
        if (params == allRequest) return

        repositoryScope.launch {

            embedUpdateStates.value = embedUpdateStates.value.orEmpty() + (params.id to QueryResult.Loading(params))

            var result: LinkEmbedResult? = null
            try {
                result = withContext(dispatchersProvider.calculationsDispatcher) {
                    val iframelyData = embedApi.getIframelyEmbed(params.url)
                    IfremlyEmbedMapper.map(IFramelyEmbedResultRelatedData(iframelyData, params.url))
                }
            } catch (e: Exception) {
                logger.log(e)

                try {
                    result = withContext(dispatchersProvider.calculationsDispatcher) {
                        val oembedData = embedApi.getOEmbedEmbed(params.url)
                        OembedMapper.map(OembedResultRelatedData(oembedData, params.url))
                    }
                } catch (e: java.lang.Exception) {
                    logger.log(e)
                    embedUpdateStates.value =
                        embedUpdateStates.value.orEmpty() + (params.id to QueryResult.Error(e, params))
                }
            }

            if (result != null) {

                savedEmbeds.value = ProcessedLinksEntity(savedEmbeds.value?.embeds.orEmpty() + result)

                embedUpdateStates.value =
                    embedUpdateStates.value.orEmpty() + (params.id to QueryResult.Success(params))

            }
        }
    }

    override val updateStates: LiveData<Map<Identifiable.Id, QueryResult<EmbedRequest>>>
        get() = embedUpdateStates
}