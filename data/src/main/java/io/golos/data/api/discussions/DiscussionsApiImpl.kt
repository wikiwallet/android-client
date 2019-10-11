package io.golos.data.api.discussions

import android.util.Log
import io.golos.commun4j.Commun4j
import io.golos.commun4j.abi.implementation.comn.gallery.CreatemssgComnGalleryStruct
import io.golos.commun4j.abi.implementation.comn.gallery.DeletemssgComnGalleryStruct
import io.golos.commun4j.http.rpc.model.transaction.response.TransactionCommitted
import io.golos.commun4j.model.*
import io.golos.commun4j.services.model.CyberCommunity
import io.golos.commun4j.services.model.FeedSort
import io.golos.commun4j.services.model.FeedTimeFrame
import io.golos.commun4j.sharedmodel.CyberName
import io.golos.data.api.Commun4jApiBase
import io.golos.data.api.communities.CommunitiesApi
import io.golos.data.repositories.current_user_repository.CurrentUserRepositoryRead
import io.golos.domain.DispatchersProvider
import io.golos.domain.commun_entities.CommunityId
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import io.golos.commun4j.abi.implementation.comn.gallery.UpdatemssgComnGalleryStruct as UpdatemssgComnGalleryStruct1
import io.golos.commun4j.utils.Pair as CommunPair

class DiscussionsApiImpl
@Inject
constructor(
    commun4j: Commun4j,
    currentUserRepository: CurrentUserRepositoryRead,
    private val communitiesApi: CommunitiesApi,
    private val dispatchersProvider: DispatchersProvider
) : Commun4jApiBase(commun4j, currentUserRepository), DiscussionsApi {
    override fun createComment(
        body: String,
        parentAccount: CyberName,
        parentPermlink: String,
        category: List<Tag>,
        metadata: DiscussionCreateMetadata,
        beneficiaries: List<Beneficiary>,
        vestPayment: Boolean,
        tokenProp: Long
    ): CommunPair<TransactionCommitted<CreatemssgComnGalleryStruct>, CreatemssgComnGalleryStruct> {
        // It's the BC method
        // We can wait for Yury or get Max's implementation from here:
        // https://github.com/communcom/communTestKit/blob/master/src/main/java/commun_test/communHelpers.java

        return StubDataFactory.createCommitedTransaction(StubDataFactory.getEmptyCreatemssgComnGalleryStruct("", ""))

//        return commun4j.createComment(
//            body,
//            parentAccount,
//            parentPermlink,
//            category,
//            metadata,
//            null,
//            beneficiaries,
//            vestPayment,
//            tokenProp.toShort(),
//            null,
//            BandWidthRequest(BandWidthSource.GOLOSIO_SERVICES)
//        )
//        .getOrThrow()
//        .run { this to this.extractResult() }
    }

    override suspend fun createPost(
        title: String,
        body: String,
        tags: List<Tag>,
        communityId: CommunityId,
        metadata: DiscussionCreateMetadata,
        beneficiaries: List<Beneficiary>,
        vestPayment: Boolean,
        tokenProp: Long
    ): CommunPair<TransactionCommitted<CreatemssgComnGalleryStruct>, CreatemssgComnGalleryStruct> {
        // It's the BC method
        // We can wait for Yury or get Max's implementation from here:
        // https://github.com/communcom/communTestKit/blob/master/src/main/java/commun_test/communHelpers.java
        return withContext(dispatchersProvider.ioDispatcher) {
            delay(500)

            val community = communitiesApi.getCommunityById(communityId)
            val post = StubDataFactory.createPost(body, community!!, authState.user.name)
            Log.d("CREATE_POST", "createPost() UserId: ${post.contentId.userId}; permlink: ${post.contentId.permlink}")
            DataStorage.posts.add(post)

            StubDataFactory.createCommitedTransaction(
                StubDataFactory.getEmptyCreatemssgComnGalleryStruct(post.contentId.userId, post.contentId.permlink))
        }

//        return commun4j.createPost(
//            title,
//            body,
//            tags,
//            metadata,
//            null,
//            beneficiaries,
//            vestPayment,
//            tokenProp.toShort(),
//            null,
//            BandWidthRequest(BandWidthSource.GOLOSIO_SERVICES)
//        )
//        .getOrThrow()
//        .run { this to this.extractResult() }
    }

    override fun updatePost(
        postPermlink: String,
        newTitle: String,
        newBody: String,
        newTags: List<Tag>,
        newJsonMetadata: DiscussionCreateMetadata
    ): CommunPair<TransactionCommitted<UpdatemssgComnGalleryStruct1>, UpdatemssgComnGalleryStruct1> {
        // It's the BC method
        // We can wait for Yury or get Max's implementation from here:
        // https://github.com/communcom/communTestKit/blob/master/src/main/java/commun_test/communHelpers.java
        return StubDataFactory.createCommitedTransaction(StubDataFactory.getEmptyUpdatemssgComnGalleryStruct())

//        return commun4j.updatePost(postPermlink, newTitle, newBody, newTags, newJsonMetadata, BandWidthRequest(BandWidthSource.GOLOSIO_SERVICES))
//            .getOrThrow().run { this to this.extractResult() }
    }

    override fun deletePostOrComment(postOrCommentPermlink: String):
            CommunPair<TransactionCommitted<DeletemssgComnGalleryStruct>, DeletemssgComnGalleryStruct> {
        // It's the BC method
        // We can wait for Yury or get Max's implementation from here:
        // https://github.com/communcom/communTestKit/blob/master/src/main/java/commun_test/communHelpers.java

        return StubDataFactory.createCommitedTransaction(StubDataFactory.getEmptyDeletemssgComnGalleryStruct())

//        return commun4j.deletePostOrComment(postOrCommentPermlink, BandWidthRequest(BandWidthSource.GOLOSIO_SERVICES))
//            .getOrThrow().run {
//                this to this.extractResult()
//            }
    }

    override fun getCommunityPosts(
        communityId: String,
        limit: Int,
        sort: FeedSort,
        timeFrame: FeedTimeFrame,
        sequenceKey: String?,
        tags: List<String>?
    ): GetDiscussionsResultRaw {
        // note[AS] it'll be "getPosts" method in a future. So far we use a stub
        return GetDiscussionsResultRaw(listOf())
    }

    override suspend fun getPost(user: CyberName, permlink: String): CyberDiscussionRaw {
        return withContext(dispatchersProvider.ioDispatcher) {
            Log.d("CREATE_POST", "getPost() UserId: ${user.name}; permlink: $permlink")
            delay(500)
            DataStorage.posts.first {
                it.contentId.userId == user.name && it.contentId.permlink == permlink
            }
        }

        // return commun4j.getPostRaw(user, "", permlink).getOrThrow()
    }

    override fun getUserSubscriptions(
        userId: String,
        limit: Int,
        sort: FeedSort,
        sequenceKey: String?
    ): GetDiscussionsResultRaw {
        // note[AS] no method so far. So we use a stub
        return GetDiscussionsResultRaw(listOf())
//        return commun4j.getUserSubscriptions(CyberName(userId), null, ContentParsingType.MOBILE, limit, sort, sequenceKey)
//            .getOrThrow()
    }

    override fun getUserPost(
        userId: String,
        limit: Int,
        sort: FeedSort,
        sequenceKey: String?
    ): GetDiscussionsResultRaw {
        // note[AS] it'll be "getPosts" method in a future. So far we use a stub
        return GetDiscussionsResultRaw(listOf())
        //return commun4j.getUserPosts(CyberName(userId), null, ContentParsingType.MOBILE, limit, sort, sequenceKey).getOrThrow()
    }

    override fun getCommentsOfPost(
        user: CyberName,
        permlink: String,
        limit: Int,
        sort: FeedSort,
        sequenceKey: String?
    ): GetDiscussionsResultRaw {
        // note[AS] it'll be "getComments" method in a future. So far we use a stub
        return GetDiscussionsResultRaw(listOf())
//        return commun4j.getCommentsOfPost(
//            user,
//            null,
//            permlink,
//            ContentParsingType.MOBILE,
//            limit,
//            sort,
//            sequenceKey
//        ).getOrThrow()
    }

    override fun getComment(user: CyberName, permlink: String): CyberDiscussionRaw {
        // note[AS] it'll be "getComment" method in a future. So far we use a stub
        return CyberDiscussionRaw(
            "",
            DiscussionVotes(0L, 0L),
            DiscussionMetadata(Date()),
            DiscussionId("", ""),
            DiscussionAuthor(CyberName(""), "", ""),
            CyberCommunity("", "", "")
        )

        // return DiscussionsResult(listOf(), "")
        // return commun4j.getComment(user, null, permlink, ContentParsingType.MOBILE).getOrThrow()
    }
}