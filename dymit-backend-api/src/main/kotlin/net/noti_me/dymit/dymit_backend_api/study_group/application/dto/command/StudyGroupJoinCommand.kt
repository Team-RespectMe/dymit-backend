package net.noti_me.dymit.dymit_backend_api.study_group.application.dto.command

data class StudyGroupJoinCommand(
    val inviteCode: String,
    val groupId: String
) {
}