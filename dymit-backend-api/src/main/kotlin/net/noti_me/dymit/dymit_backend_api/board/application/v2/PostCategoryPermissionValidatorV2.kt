package net.noti_me.dymit.dymit_backend_api.board.application.v2

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.board.domain.Board
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardMember
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardCategoryWritePolicy
import net.noti_me.dymit.dymit_backend_api.board.domain.PostCategory
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_schedule.BoardScheduleParticipantPort
import org.bson.types.ObjectId

/**
 * 카테고리별 게시글 작성 권한 검증 유틸리티입니다.
 */
object PostCategoryPermissionValidatorV2 {

    fun validate(
        board: Board,
        groupMember: BoardMember,
        category: PostCategory,
        scheduleId: String?,
        scheduleParticipantRepository: BoardScheduleParticipantPort,
        forceRetrospectiveParticipantCheck: Boolean = false
    ): ObjectId? {
        if (!board.canWriteByCategory(groupMember, category)) {
            throw ForbiddenException(message = "해당 카테고리에 게시글 작성 권한이 없습니다.")
        }

        val categoryPolicy = board.getCategoryPolicy(category)
            ?: throw ForbiddenException(message = "해당 카테고리 정책이 설정되지 않았습니다.")

        val normalizedScheduleId = if (category == PostCategory.RETROSPECTIVE) {
            if (scheduleId.isNullOrBlank()) {
                throw BadRequestException(message = "회고 카테고리 작성 시 scheduleId는 필수입니다.")
            }
            ObjectId(scheduleId)
        } else {
            null
        }

        val shouldValidateScheduleParticipant =
            categoryPolicy.writePolicy == BoardCategoryWritePolicy.SCHEDULE_PARTICIPANT_ONLY ||
                (forceRetrospectiveParticipantCheck && category == PostCategory.RETROSPECTIVE)

        if (shouldValidateScheduleParticipant) {
            if (normalizedScheduleId == null) {
                throw BadRequestException(message = "회고 카테고리 작성 시 scheduleId는 필수입니다.")
            }
            val isParticipant = scheduleParticipantRepository.existsParticipant(
                scheduleId = normalizedScheduleId,
                memberId = groupMember.memberId
            )
            if (!isParticipant) {
                throw ForbiddenException(message = "해당 일정 참여자만 회고를 작성할 수 있습니다.")
            }
        }

        return normalizedScheduleId
    }
}
