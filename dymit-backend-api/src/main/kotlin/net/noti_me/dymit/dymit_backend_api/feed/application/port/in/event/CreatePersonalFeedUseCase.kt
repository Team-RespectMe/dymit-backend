package net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event

import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.dto.CreatePersonalFeedCommand

/**
 * 이벤트 데이터로 개인 피드를 생성합니다.
 */
interface CreatePersonalFeedUseCase {

    /**
     * 개인 피드를 생성합니다.
     *
     * @param command 생성 명령
     */
    fun execute(command: CreatePersonalFeedCommand)
}
