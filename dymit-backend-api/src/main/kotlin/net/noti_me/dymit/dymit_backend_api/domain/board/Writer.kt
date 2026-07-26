package net.noti_me.dymit.dymit_backend_api.domain.board

import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupProfileImageDto as ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import org.bson.types.ObjectId

class Writer(
    val id: ObjectId,
    var nickname: String,
    var image: ProfileImageVo
) {

    companion object {
        fun from(entity: StudyGroupMember): Writer {
            return Writer(
                id = entity.memberId,
                nickname = entity.nickname,
                image = ProfileImageVo(
                    type = entity.profileImage.type,
                    url = entity.profileImage.url
                )
            )
        }

        fun from(entity: Member): Writer {
            return Writer(
                id = entity.id!!,
                nickname = entity.nickname,
                image = ProfileImageVo(
                    type = entity.profileImage.type,
                    url = entity.profileImage.thumbnail
                )
            )
        }
    }
}