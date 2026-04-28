package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage

interface Pushable {

    fun toPushMessage(): PersonalPushMessage
}