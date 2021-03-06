//package io.golos.cyber_android.interactors
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import io.golos.cyber_android.dispatchersProvider
//import io.golos.cyber_android.feedEntityToModelMapper
//import io.golos.cyber_android.feedRepository
//import io.golos.cyber_android.voteRepo
//import io.golos.domain.entities.CyberUser
//import io.golos.domain.interactors.feed.UserSubscriptionsFeedUseCase
//import io.golos.domain.interactors.model.DiscussionsFeed
//import io.golos.domain.interactors.model.PostModel
//import io.golos.domain.interactors.model.UpdateOption
//import junit.framework.Assert.assertTrue
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//
///**
// * Created by yuri yurivladdurain@gmail.com on 2019-03-18.
// */
//class UserSubscriptionsCaseTest {
//
//    @Rule
//    @JvmField
//    public val rule = InstantTaskExecutorRule()
//
//    lateinit var case: UserSubscriptionsFeedUseCase
//    @Before
//    fun before() {
//        case = UserSubscriptionsFeedUseCase(
//            CyberUser("destroyer2k"),
//            feedRepository,
//            voteRepo,
//            feedEntityToModelMapper,
//            dispatchersProvider
//        )
//
//    }
//
//    @Test
//    fun testOne() {
//        val pageSize = 3
//        var postFeed: DiscussionsFeed<PostModel>? = null
//        case.subscribe()
//        case.getAsLiveData.observeForever {
//            postFeed = it
//        }
//        case.requestFeedUpdate(pageSize, UpdateOption.REFRESH_FROM_BEGINNING)
//
//        assertTrue("fail of initial loading of posts", postFeed?.items?.size == pageSize)
//
//        case.requestFeedUpdate(pageSize, UpdateOption.REFRESH_FROM_BEGINNING)
//
//        assertTrue("fail of reloading first posts", postFeed?.items?.size == pageSize)
//
//        case.requestFeedUpdate(pageSize, UpdateOption.FETCH_NEXT_PAGE)
//
//        assertTrue("error fetching second page of posts", postFeed?.items?.size == pageSize * 2)
//
//        case.requestFeedUpdate(pageSize, UpdateOption.FETCH_NEXT_PAGE)
//
//        assertTrue("error fetching third page of posts", postFeed?.items?.size == pageSize * 3)
//
//        case.requestFeedUpdate(pageSize, UpdateOption.REFRESH_FROM_BEGINNING)
//
//        assertTrue(
//            "error loading fresh posts, after downloading 3 consecutive pages of feed",
//            postFeed?.items?.size == pageSize
//        )
//
//    }
//}