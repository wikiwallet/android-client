package io.golos.cyber_android.services.firebase.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.moshi.Moshi
import dagger.Lazy
import io.golos.commun4j.services.model.*
import io.golos.cyber_android.application.App
import io.golos.cyber_android.services.firebase.notifications.di.FirebaseNotificationServiceComponent
import io.golos.cyber_android.services.firebase.notifications.dto.NotificationMetadata
import io.golos.cyber_android.services.firebase.notifications.popup_manager.FirebaseNotificationPopupManager
import io.golos.data.mappers.mapToNotificationDomain
import io.golos.domain.DispatchersProvider
import io.golos.domain.KeyValueStorageFacade
import io.golos.domain.dependency_injection.Clarification
import io.golos.domain.dto.FcmTokenStateDomain
import io.golos.domain.dto.UserIdDomain
import io.golos.domain.repositories.CurrentUserRepositoryRead
import io.golos.utils.id.IdUtil
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class FirebaseNotificationService : FirebaseMessagingService(), CoroutineScope {
    private lateinit var injectionKey: String

    private val scopeJob: Job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = scopeJob + dispatchersProvider.uiDispatcher

    @Inject
    internal lateinit var keyValueStorage: Lazy<KeyValueStorageFacade>

    @Inject
    @field:Named(Clarification.FIREBASE_MESSAGES)
    internal lateinit var moshi: Lazy<Moshi>

    @Inject
    internal lateinit var currentUserRepository: Lazy<CurrentUserRepositoryRead>

    @Inject
    internal lateinit var dispatchersProvider: DispatchersProvider

    @Inject
    internal lateinit var popupManager: Lazy<FirebaseNotificationPopupManager>

    override fun onCreate() {
        super.onCreate()

        injectionKey = IdUtil.generateStringId()
        App.injections.get<FirebaseNotificationServiceComponent>(injectionKey).inject(this)
    }

    override fun onNewToken(token: String) {
        Timber.tag("FCM_MESSAGES").d("Token received: $token")
        launch {
            keyValueStorage.get().saveFcmToken(FcmTokenStateDomain(false, token))
            Timber.tag("FCM_MESSAGES").d("Token: saved")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.tag("FCM_MESSAGES").d("Message received - called")
        remoteMessage.data["notification"]?.let { rawJsonNotification ->
            Timber.tag("FCM_MESSAGES").d("Message received: $rawJsonNotification")

            launch {
                parseNotification(rawJsonNotification)
                    ?.let { parsedNotification ->
                        getCurrentUser()
                            ?.let { currentUser ->
                                popupManager.get().showNotification(
                                    parsedNotification.mapToNotificationDomain(currentUser.second, currentUser.first))
                            }
                    }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scopeJob.cancel()
        App.injections.release<FirebaseNotificationServiceComponent>(injectionKey)
    }

    private fun parseNotification(jsonNotification: String): Notification? =
        try {
            moshi.get().adapter(NotificationMetadata::class.java).fromJson(jsonNotification)
                ?.let { metadata ->
                    when(metadata.eventType) {
                        "reply" -> ReplyNotification::class.java
                        "transfer" -> TransferNotification::class.java
                        "reward" -> RewardNotification::class.java
                        "upvote" -> UpvoteNotification::class.java
                        "mention" -> MentionNotification::class.java
                        "subscribe" -> SubscribeNotification::class.java
                        "referralRegistrationBonus" -> ReferralRegistrationBonusNotification::class.java
                        "referralPurchaseBonus" -> ReferralPurchaseBonusNotification::class.java
                        else -> {
                            Timber.w("This notification type is not supported: ${metadata.eventType}")
                            null
                        }
                    }
                    ?.let { eventDtoType ->
                        moshi.get().adapter(eventDtoType).fromJson(jsonNotification)
                    }
                }

        } catch (ex: Exception) {
            Timber.w("Can't parse notification")
            Timber.e(ex)
            null
        }

    private suspend fun getCurrentUser(): Pair<String, UserIdDomain>? =
        (currentUserRepository.get().authState ?:
        withContext(dispatchersProvider.ioDispatcher) { keyValueStorage.get().getAuthState() })
            ?.let { Pair(it.userName, it.user) }
}