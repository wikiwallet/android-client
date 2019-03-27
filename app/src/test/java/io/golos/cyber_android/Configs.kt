package io.golos.cyber_android

import io.golos.cyber4j.Cyber4J
import io.golos.cyber_android.locator.RepositoriesHolder
import io.golos.data.api.Cyber4jApiService
import io.golos.data.repositories.*
import io.golos.domain.DispatchersProvider
import io.golos.domain.Logger
import io.golos.domain.Repository
import io.golos.domain.entities.AuthState
import io.golos.domain.entities.PostEntity
import io.golos.domain.entities.VoteRequestEntity
import io.golos.domain.model.AuthRequest
import io.golos.domain.model.PostFeedUpdateRequest
import io.golos.domain.rules.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executor

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-03-13.
 */
val cyber4j by lazy { Cyber4J() }

val apiService: Cyber4jApiService by lazy { Cyber4jApiService(cyber4j) }

val cyberPostToEntityMapper = CyberPostToEntityMapper()
val voteToEntityMapper = VoteRequestModelToEntityMapper()
val cyberFeedToEntityMapper = CyberFeedToEntityMapper(cyberPostToEntityMapper)

val postEntityToModelMapper = PostEntityEntitiesToModelMapper()
val feedEntityToModelMapper = PostFeedEntityToModelMapper(postEntityToModelMapper)
val commentEntityToModelMapper = CommentEntityToModelMapper()
val commentFeeEntityToModelMapper = CommentsFeedEntityToModelMapper(commentEntityToModelMapper)
val voteEntityToPostMapper = VoteRequestEntityToModelMapper()

val approver = FeedUpdateApprover()

val postMerger = PostMerger()
val feedMerger = PostFeedMerger()

val emptyPostFeedProducer = EmptyPostFeedProducer()


val cyberCommentToEntityMapper = CyberCommentToEntityMapper()
val cyberCommentFeedToEntityMapper = CyberCommentsToEntityMapper(cyberCommentToEntityMapper)


val commentApprover = CommentUpdateApprover()

val commentMerger = CommentMerger()
val commentFeedMerger = CommentFeedMerger()

val emptyCommentFeedProducer = EmptyCommentFeedProducer()


val logger = object : Logger {
    override fun invoke(e: Exception) {
        e.printStackTrace()
    }
}

val dispatchersProvider = object : DispatchersProvider {
    override val uiDispatcher: CoroutineDispatcher
        get() = Executor { command -> command?.run() }.asCoroutineDispatcher()
    override val workDispatcher: CoroutineDispatcher
        get() = Executor { command -> command?.run() }.asCoroutineDispatcher()
    override val networkDispatcher: CoroutineDispatcher
        get() = Executor { command -> command?.run() }.asCoroutineDispatcher()
}

val feedRepository = PostsFeedRepository(
    apiService,
    cyberFeedToEntityMapper,
    cyberPostToEntityMapper,
    postMerger,
    feedMerger,
    approver,
    emptyPostFeedProducer,
    dispatchersProvider,
    logger
)

val commentsRepository = CommentsFeedRepository(
    apiService,
    cyberCommentFeedToEntityMapper,
    cyberCommentToEntityMapper,
    commentMerger,
    commentFeedMerger,
    commentApprover,
    emptyCommentFeedProducer,
    dispatchersProvider,
    logger
)

val authStateRepository = AuthStateRepository(apiService, dispatchersProvider, logger)

val voteRepo = VoteRepository(apiService, dispatchersProvider, logger)


val appCore = AppCore(object : RepositoriesHolder {
    override val postFeedRepository: AbstractDiscussionsRepository<PostEntity, PostFeedUpdateRequest>
        get() = feedRepository
    override val authRepository: Repository<AuthState, AuthRequest>
        get() = authStateRepository
    override val voteRepository: Repository<VoteRequestEntity, VoteRequestEntity>
        get() = voteRepo
}, dispatchersProvider)


