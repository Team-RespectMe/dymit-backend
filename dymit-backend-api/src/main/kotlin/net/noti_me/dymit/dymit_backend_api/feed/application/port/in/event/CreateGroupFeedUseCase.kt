package net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event

import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.dto.CreateGroupFeedCommand

/**
 * 이벤트 데이터로 그룹 피드를 생성합니다.
 */
interface CreateGroupFeedUseCase {

    /**
     * 그룹 피드를 생성합니다.
     *
     * @param command 생성 명령
     */
    fun execute(command: CreateGroupFeedCommand)
}
