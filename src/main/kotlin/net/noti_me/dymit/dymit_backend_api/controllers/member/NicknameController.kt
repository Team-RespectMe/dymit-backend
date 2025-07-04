package net.noti_me.dymit.dymit_backend_api.controllers.member

import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.MemberCreateUsecase
import net.noti_me.dymit.dymit_backend_api.common.constraints.nickname.Nickname
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class NicknameController(
    private val memberCreateUsecase: MemberCreateUsecase
) : NicknameApi {

    private val logger = LoggerFactory.getLogger(javaClass)

//    @GetMapping("/api/v1/nicknames/validate")
//    fun checkNickname(@RequestParam @Valid @Nickname nickname: String) {
    override fun checkNickname(nickname: String) {
        logger.debug("checkNickname called with nickname: $nickname")
        return memberCreateUsecase.checkNickname(nickname)
    }
}