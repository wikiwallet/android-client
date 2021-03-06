package io.golos.data.repositories.images_uploading

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.golos.data.api.image_upload.ImageUploadApi
import io.golos.data.utils.ImageCompressor
import io.golos.domain.DispatchersProvider
import io.golos.domain.dto.UploadedImageEntity
import io.golos.domain.dto.UploadedImagesEntity
import io.golos.domain.repositories.Repository
import io.golos.domain.requestmodel.CompressionParams
import io.golos.domain.requestmodel.Identifiable
import io.golos.domain.requestmodel.ImageUploadRequest
import io.golos.domain.requestmodel.QueryResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-04-29.
 */
class ImageUploadRepositoryLiveData
@Inject
constructor(
    api: ImageUploadApi,
    dispatchersProvider: DispatchersProvider,
    compressor: ImageCompressor
) : ImageUploadRepositoryBase(dispatchersProvider, api, compressor),
    Repository<UploadedImagesEntity, ImageUploadRequest> {
    private val repositoryScope = CoroutineScope(dispatchersProvider.uiDispatcher + SupervisorJob())

    private val uploadedImages = MutableLiveData<UploadedImagesEntity>()
    private val uploadedUpdateStates = MutableLiveData<Map<Identifiable.Id, QueryResult<ImageUploadRequest>>>()

    private val allRequest = ImageUploadRequest(File("\\"), Uri.EMPTY, CompressionParams.DirectCompressionParams)
    private val jobsMap: HashMap<ImageUploadRequest, Job> = hashMapOf()

    override val allDataRequest: ImageUploadRequest = allRequest
    override fun getAsLiveData(params: ImageUploadRequest): LiveData<UploadedImagesEntity> {
        return uploadedImages
    }

    override fun makeAction(params: ImageUploadRequest) {
        if (params == allRequest) return
        repositoryScope.launch {
            try {
                uploadedUpdateStates.value =
                    uploadedUpdateStates.value.orEmpty() + (params.id to QueryResult.Loading(params))

                val result = uploadImage(params)

                uploadedImages.value =
                    UploadedImagesEntity(
                        uploadedImages.value?.imageUrls.orEmpty() + (params.imageFile.absolutePath to UploadedImageEntity(
                            result
                        ))
                    )

                uploadedUpdateStates.value =
                    uploadedUpdateStates.value.orEmpty() + (params.id to QueryResult.Success(params))

            } catch (e: Exception) {
                Timber.e(e)
                uploadedUpdateStates.value =
                    uploadedUpdateStates.value.orEmpty() + (params.id to QueryResult.Error(e, params))
            }
        }.let {
            jobsMap[params]?.cancel()
            jobsMap[params] = it
        }
    }

    override val updateStates: LiveData<Map<Identifiable.Id, QueryResult<ImageUploadRequest>>>
        get() = uploadedUpdateStates
}