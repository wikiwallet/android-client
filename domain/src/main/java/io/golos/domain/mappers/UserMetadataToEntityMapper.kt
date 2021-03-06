package io.golos.domain.mappers

import io.golos.commun4j.services.model.GetProfileResult
import io.golos.commun4j.sharedmodel.CyberName
import io.golos.domain.dto.*

// todo[AS] temporary stub to compile!!!
object UserMetadataToEntityMapper {
    fun map(communObject: GetProfileResult): UserMetadataEntity {
        return UserMetadataEntity(
            UserPersonalDataEntity(
                "",
                "",
                "",
                ContactsEntity(
                    "", "", "", "")
            ),
            UserSubscriptionsEntity(0, 0),
            UserStatsEntity(communObject.stats?.postsCount ?: 0, communObject.stats?.commentsCount ?: 0),
            CyberName(""),
            communObject.username!!,
            SubscribersEntity(communObject.subscribers?.usersCount ?: 0, communObject.subscribers?.communitiesCount ?: 0),
            communObject.registration?.time,
            false
        )
    }
}

/**
 * Created by yuri yurivladdurain@gmail.com on 2019-04-30.
 */
//class UserMetadataToEntityMapper
//@Inject
//constructor() : CyberToEntityMapper<UserMetadataResult, UserMetadataEntity> {
//    override suspend fun invoke(cyberObject: UserMetadataResult): UserMetadataEntity {
//        return UserMetadataEntity(
//            UserPersonalDataEntity(
//                cyberObject.personal?.avatarUrl,
//                cyberObject.personal?.coverUrl,
//                cyberObject.personal?.about,
//                ContactsEntity(
//                    cyberObject.personal?.contacts?.facebook, cyberObject.personal?.contacts?.telegram,
//                    cyberObject.personal?.contacts?.whatsApp, cyberObject.personal?.contacts?.weChat
//                )
//            ),
//            UserSubscriptionsEntity(
//                cyberObject.subscriptions?.usersCount ?: 0,
//                cyberObject.subscriptions?.communitiesCount ?: 0
//            ),
//            UserStatsEntity(cyberObject.stats?.postsCount ?: 0, cyberObject.stats?.commentsCount ?: 0),
//            cyberObject.userId,
//            cyberObject.username,
//            SubscribersEntity(
//                cyberObject.subscribers?.usersCount ?: 0,
//                cyberObject.subscribers?.communitiesCount ?: 0
//            ),
//            cyberObject.createdAt,
//            cyberObject.isSubscribed
//        )
//    }
//}
