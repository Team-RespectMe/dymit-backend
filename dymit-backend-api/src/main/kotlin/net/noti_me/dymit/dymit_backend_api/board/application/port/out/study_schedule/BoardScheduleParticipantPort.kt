package net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_schedule

import org.bson.types.ObjectId

/**
 * 게시판 카테고리 권한 검증에 필요한 일정 참여 여부 포트입니다.
 */
interface BoardScheduleParticipantPort {

    /**
     * 멤버가 일정 참여자인지 확인합니다.
     *
     * @param scheduleId 일정 식별자
     * @param memberId 멤버 식별자
     * @return 참여 여부
     */
    fun existsParticipant(scheduleId: ObjectId, memberId: ObjectId): Boolean
}
