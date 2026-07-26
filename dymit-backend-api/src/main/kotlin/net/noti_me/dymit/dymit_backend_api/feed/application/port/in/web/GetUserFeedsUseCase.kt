package net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web

import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.GetUserFeedsCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.UserFeedDto

/**
 * 로그인 회원의 개인 피드 목록을 조회합니다.
 */
interface GetUserFeedsUseCase {

    /**
     * 개인 피드를 조회합니다.
     *
     * @param command 조회 명령
     * @return 개인 피드 목록
     */
    fun execute(command: GetUserFeedsCommand): List<UserFeedDto>
}
