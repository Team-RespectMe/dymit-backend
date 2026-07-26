package net.noti_me.dymit.dymit_backend_api.study_group.application.dto.command

class StudyGroupMemberPreview(
    val memberId: String,
    val nickname: String,
    val profileImageType : String = "preset",
    val profileImageUrl: String = "0",
) {

}