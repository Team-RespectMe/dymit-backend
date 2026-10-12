package net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web

import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.MarkAllUserFeedsAsReadCommand

/**
 * 로그인 회원의 모든 개인 피드를 읽음 처리합니다.
 */
interface MarkAllUserFeedsAsReadUseCase {

    /**
     * 읽지 않은 모든 피드를 읽음 처리합니다.
     *
     * @param command 읽음 처리 명령
     */
    fun execute(command: MarkAllUserFeedsAsReadCommand)
}
