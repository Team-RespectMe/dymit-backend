package net.noti_me.dymit.dymit_backend_api.domain.study_group

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo

class ProfileImageVo(
    val type: ProfileImageType = ProfileImageType.PRESET,
    val url: String = GroupPresetImage.STUDY.thumbnail
) {

    companion object {
        fun from(dto: MemberProfileImageVo): ProfileImageVo {
            return ProfileImageVo(
                type = dto.type,
                url = dto.thumbnail,
            )
        }

        fun from(dto: GroupProfileImageVo): ProfileImageVo {
            return ProfileImageVo(
                type = dto.type,
                url = dto.thumbnail,
            )
        }
    }
}