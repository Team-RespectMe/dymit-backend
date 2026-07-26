package net.noti_me.dymit.dymit_backend_api.study_group.application.dto.command

data class StudyGroupCreateCommand(
    val name: String,
    val description: String
) {
}