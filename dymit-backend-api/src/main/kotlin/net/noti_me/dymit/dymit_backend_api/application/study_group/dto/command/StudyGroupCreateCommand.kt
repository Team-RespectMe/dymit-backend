package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command

data class StudyGroupCreateCommand(
    val name: String,
    val description: String
) {
}