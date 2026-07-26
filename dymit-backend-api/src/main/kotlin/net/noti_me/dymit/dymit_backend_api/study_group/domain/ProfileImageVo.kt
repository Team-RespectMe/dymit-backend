package net.noti_me.dymit.dymit_backend_api.study_group.domain

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType

class ProfileImageVo(
    val type: ProfileImageType = ProfileImageType.PRESET,
    val url: String = GroupPresetImage.STUDY.thumbnail
) {

    companion object {
        fun of(
            type: ProfileImageType,
            url: String
        ): ProfileImageVo {
            return ProfileImageVo(
                type = type,
                url = url,
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
