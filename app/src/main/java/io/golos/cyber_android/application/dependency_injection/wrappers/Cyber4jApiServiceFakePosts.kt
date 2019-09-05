package io.golos.cyber_android.application.dependency_injection.wrappers

import io.golos.cyber4j.Cyber4J
import io.golos.cyber4j.http.rpc.model.ApiResponseError
import io.golos.cyber4j.model.CyberDiscussion
import io.golos.cyber4j.model.TextRow
import io.golos.cyber4j.services.model.ContentParsingType
import io.golos.cyber4j.sharedmodel.CyberName
import io.golos.cyber4j.sharedmodel.Either
import io.golos.cyber_android.R
import io.golos.data.api.Cyber4jApiService
import io.golos.domain.AppResourcesProvider
import io.golos.domain.dependency_injection.scopes.ApplicationScope
import javax.inject.Inject

@ApplicationScope
open class Cyber4jApiServiceFakePosts
@Inject
constructor(
    private val cyber4j: Cyber4J,
    private val appResourcesProvider: AppResourcesProvider
) : Cyber4jApiService(cyber4j) {

    override fun getPost(user: CyberName, permlink: String): CyberDiscussion {
        val result = cyber4j.getPost(user, null, permlink, ContentParsingType.MOBILE)
        if(result is Either.Success<CyberDiscussion, ApiResponseError>) {
            val value = result.value

            val post = (value.content.body.mobile!![0] as TextRow)
            val newPost = post.copy(content = String(appResourcesProvider.getRaw(R.raw.fake_post).readBytes()))

            return value.copy(content = value.content.copy(body = value.content.body.copy(mobile = listOf(newPost))))
        }
        return result.getOrThrow()
    }
}