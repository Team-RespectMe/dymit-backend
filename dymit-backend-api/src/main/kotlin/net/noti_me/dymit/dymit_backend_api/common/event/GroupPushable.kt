package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.push_notification.domain.GroupPushMessage

interface GroupPushable {

    fun toGroupPush(): GroupPushMessage
}
