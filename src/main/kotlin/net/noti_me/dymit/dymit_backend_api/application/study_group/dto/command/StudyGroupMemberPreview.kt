package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command

class StudyGroupMemberPreview(
    val memberId: String,
    val nickname: String,
    val profileImageType : String = "preset",
    val profileImageUrl: String = "0",
) {
}