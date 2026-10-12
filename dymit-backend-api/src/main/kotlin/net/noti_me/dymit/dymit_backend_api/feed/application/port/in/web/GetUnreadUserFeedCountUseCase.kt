package net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web

import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.GetUnreadUserFeedCountCommand

/**
 * 로그인 회원의 읽지 않은 개인 피드 수를 조회합니다.
 */
interface GetUnreadUserFeedCountUseCase {

    /**
     * 읽지 않은 피드 수를 조회합니다.
     *
     * @param command 조회 명령
     * @return 읽지 않은 피드 수
     */
    fun execute(command: GetUnreadUserFeedCountCommand): Long
}
