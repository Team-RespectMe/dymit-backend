package net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web

import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.DeleteUserFeedCommand

/**
 * 로그인 회원의 개인 피드를 삭제합니다.
 */
interface DeleteUserFeedUseCase {

    /**
     * 개인 피드를 삭제합니다.
     *
     * @param command 삭제 명령
     */
    fun execute(command: DeleteUserFeedCommand)
}
