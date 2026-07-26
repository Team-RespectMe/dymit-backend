package net.noti_me.dymit.dymit_backend_api.study_group.application.dto.command

class ChangeGroupOwnerCommand(
    val groupId: String,
    val newOwnerId: String
) {
}