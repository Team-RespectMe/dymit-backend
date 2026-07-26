package net.noti_me.dymit.dymit_backend_api.study_group.domain

import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupProfileImageType

class ProfileImageVo(
    val type: StudyGroupProfileImageType = StudyGroupProfileImageType.PRESET,
    val url: String = GroupPresetImage.STUDY.thumbnail
) {

    companion object {
        fun of(
            type: StudyGroupProfileImageType,
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
