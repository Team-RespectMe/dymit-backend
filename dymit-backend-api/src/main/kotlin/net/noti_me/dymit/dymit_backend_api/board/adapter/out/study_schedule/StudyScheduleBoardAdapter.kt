package net.noti_me.dymit.dymit_backend_api.board.adapter.out.study_schedule

import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_schedule.BoardScheduleParticipantPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.StudyScheduleQueryPort
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

/**
 * 일정 공개 계약을 게시판 일정 참여 포트로 변환하는 어댑터입니다.
 */
@Component
class StudyScheduleBoardAdapter(
    private val queryPort: StudyScheduleQueryPort
) : BoardScheduleParticipantPort {

    override fun existsParticipant(scheduleId: ObjectId, memberId: ObjectId): Boolean {
        return queryPort.existsParticipant(scheduleId, memberId)
    }
}
