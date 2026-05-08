package net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.PostCommandV2
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory
import org.bson.types.ObjectId

/**
 * 게시글 V2 생성/수정 요청 DTO입니다.
 */
@Schema(description = "게시글 V2 생성/수정 요청 DTO")
@Sanitize
class PostCommandRequestV2(
    @field:NotEmpty(message = "제목은 비어 있을 수 없습니다.")
    @Schema(description = "게시글 제목")
    val title: String,
    @field:NotEmpty(message = "내용은 비어 있을 수 없습니다.")
    @Schema(description = "게시글 내용")
    val content: String,
    @Schema(description = "게시글 카테고리")
    val category: PostCategory,
    @Schema(description = "회고 카테고리에서 사용할 일정 ID")
    val scheduleId: String? = null
) {

    @AssertTrue(message = "회고 카테고리 작성 시 scheduleId는 유효한 ObjectId여야 합니다.")
    fun hasValidScheduleIdForRetrospective(): Boolean {
        if (category != PostCategory.RETROSPECTIVE) {
            return true
        }
        return !scheduleId.isNullOrBlank() && ObjectId.isValid(scheduleId)
    }

    fun toCommand(groupId: String, boardId: String): PostCommandV2 {
        return PostCommandV2(
            groupId = groupId,
            boardId = boardId,
            title = title,
            content = content,
            category = category,
            scheduleId = scheduleId
        )
    }
}
