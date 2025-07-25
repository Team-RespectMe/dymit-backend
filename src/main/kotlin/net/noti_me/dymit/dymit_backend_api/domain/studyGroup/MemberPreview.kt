package net.noti_me.dymit.dymit_backend_api.domain.studyGroup

data class MemberPreview(
    val memberId: String,
    val nickname: String,
    val profileImageUrl: String? = null,
) {
}