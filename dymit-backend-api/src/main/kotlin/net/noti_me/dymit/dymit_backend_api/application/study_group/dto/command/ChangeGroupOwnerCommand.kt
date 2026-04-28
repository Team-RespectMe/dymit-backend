package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command

class ChangeGroupOwnerCommand(
    val groupId: String,
    val newOwnerId: String
) {
}