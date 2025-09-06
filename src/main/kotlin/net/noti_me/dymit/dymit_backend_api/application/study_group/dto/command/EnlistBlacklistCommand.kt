package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command

class EnlistBlacklistCommand(
    val groupId: String,
    val targetMember: String,
    val reason: String
) {

}