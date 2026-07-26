package net.noti_me.dymit.dymit_backend_api.member.application.usecases

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * 회원 도메인 이벤트 발행기입니다.
 *
 * @param eventPublisher Spring 이벤트 발행기
 */
@Component
class MemberEventHandler(
    private val eventPublisher: ApplicationEventPublisher
) {

    private val logger = LoggerFactory.getLogger(javaClass)
}
