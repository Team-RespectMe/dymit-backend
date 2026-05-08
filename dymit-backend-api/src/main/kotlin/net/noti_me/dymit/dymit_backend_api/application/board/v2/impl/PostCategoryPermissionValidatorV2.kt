package net.noti_me.dymit.dymit_backend_api.application.board.v2.impl

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardCategoryWritePolicy
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import org.bson.types.ObjectId

/**
 * 카테고리별 게시글 작성 권한 검증 유틸리티입니다.
 */
object PostCategoryPermissionValidatorV2 {

    fun validate(
        board: Board,
        groupMember: StudyGroupMember,
        category: PostCategory,
        scheduleId: String?,
        scheduleParticipantRepository: ScheduleParticipantRepository,
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
            val isParticipant = scheduleParticipantRepository.existsByScheduleIdAndMemberId(
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
