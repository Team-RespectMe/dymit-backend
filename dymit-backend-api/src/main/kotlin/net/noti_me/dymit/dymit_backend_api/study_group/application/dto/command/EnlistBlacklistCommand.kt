package net.noti_me.dymit.dymit_backend_api.study_group.application.dto.command

class EnlistBlacklistCommand(
    val groupId: String,
    val targetMember: String,
    val reason: String
) {

}