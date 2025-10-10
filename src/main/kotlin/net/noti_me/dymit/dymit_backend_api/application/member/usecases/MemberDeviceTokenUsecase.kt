package net.noti_me.dymit.dymit_backend_api.application.member.usecases

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface MemberDeviceTokenUsecase {

    fun registerDeviceToken(member: MemberInfo, deviceToken: String)

    fun unregisterDeviceToken(member: MemberInfo , deviceToken: String): Unit
}