package net.noti_me.dymit.dymit_backend_api.supports

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo

fun createProfileImageVo(
    type: ProfileImageType = ProfileImageType.PRESET,
    url: String = "https://example.com/profile.jpg",
): ProfileImageVo {
    return ProfileImageVo(
        type = type,
        url = url,
    )
}