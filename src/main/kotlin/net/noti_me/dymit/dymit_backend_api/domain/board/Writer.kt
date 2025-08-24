package net.noti_me.dymit.dymit_backend_api.domain.board

import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroupMember
import org.bson.types.ObjectId

class Writer(
    val id: ObjectId,
    val nickname: String,
    val image: ProfileImageVo
) {

    companion object {
        fun from(entity: StudyGroupMember): Writer {
            return Writer(
                id = entity.id,
                nickname = entity.nickname,
                image = ProfileImageVo(
                    type = entity.profileImage.type,
                    url = entity.profileImage.url
                )
            )
        }
    }
}