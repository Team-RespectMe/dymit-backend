package net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.feed.domain.IconType
import net.noti_me.dymit.dymit_backend_api.feed.domain.ResourceType
import java.time.LocalDateTime

/**
 * 사용자 피드 HTTP 응답입니다.
 *
 * @param id 피드 식별자
 * @param iconType 아이콘 종류
 * @param eventName 이벤트 이름
 * @param messages 피드 메시지 목록
 * @param resources 연관 리소스 목록
 * @param isRead 읽음 여부
 * @param createdAt 생성 시각
 */
@Schema(description = "사용자 피드 응답")
data class UserFeedResponse(
    @Schema(description = "피드 ID", example = "6884dec1beed715fdd4a7639")
    val id: String,
    @Schema(description = "피드 아이콘 타입", example = "HAND_WAVING")
    val iconType: IconType,
    @Schema(description = "이벤트 이름", example = "STUDY_SCHEDULE_CREATED")
    val eventName: String,
    @Schema(description = "피드 메시지")
    val messages: List<FeedMessageResponse> = listOf(
        FeedMessageResponse(text = "새로운 스터디 일정이 등록되었어요! ", highlightColor = "#FF5733"),
        FeedMessageResponse(text = "[ "),
        FeedMessageResponse(text = "2주차 - 포인터 및 배열(더미 데이터입니다.)", highlightColor = "#33C1FF"),
        FeedMessageResponse(text = " ]")
    ),
    @Schema(description = "관련 리소스 목록, 리디렉션 시 사용")
    val resources: List<AssociatedResourceResponse>,
    @Schema(description = "읽은 여부", example = "false")
    val isRead: Boolean,
    @Schema(description = "생성 일자", example = "2025-10-05T14:48:00")
    val createdAt: LocalDateTime
) : BaseResponse() {

    companion object {

        /**
         * 입력 포트 결과를 HTTP 응답으로 변환합니다.
         *
         * @param dto 입력 포트 결과
         * @return HTTP 응답
         */
        fun from(dto: UserFeedDto): UserFeedResponse {
            return UserFeedResponse(
                id = dto.id,
                iconType = dto.iconType,
                eventName = dto.eventName,
                messages = dto.messages.map(FeedMessageResponse::from),
                resources = dto.associates.map(AssociatedResourceResponse::from),
                isRead = dto.isRead,
                createdAt = dto.createdAt
            )
        }
    }
}

/**
 * 피드 메시지 HTTP 응답입니다.
 *
 * @param text 메시지 본문
 * @param textColor 글자 색상
 * @param highlightColor 강조 색상
 */
@Schema(description = "피드 메시지 파트")
data class FeedMessageResponse(
    @Schema(description = "피드 메시지", example = "새로운 댓글이 달렸습니다.")
    val text: String,
    @Schema(description = "피드 메시지 컬러(nullable)", example = "#FF5733")
    val textColor: String? = null,
    @Schema(description = "피드 메시지 하이라이트 컬러(nullable)", example = "#33FF57")
    val highlightColor: String? = null
) {

    companion object {

        /**
         * 입력 포트 메시지를 HTTP 응답으로 변환합니다.
         *
         * @param dto 입력 포트 메시지
         * @return HTTP 응답 메시지
         */
        fun from(dto: FeedMessageDto): FeedMessageResponse {
            return FeedMessageResponse(
                text = dto.text,
                textColor = dto.textColor,
                highlightColor = dto.highlightColor
            )
        }
    }
}

/**
 * 연관 리소스 HTTP 응답입니다.
 *
 * @param type 리소스 종류
 * @param resourceId 리소스 식별자
 */
@Schema(description = "연관 리소스 응답 객체")
data class AssociatedResourceResponse(
    @Schema(description = "리소스 타입")
    val type: ResourceType,
    @Schema(description = "리소스 ID", example = "6884dec1beed715fdd4a7639")
    val resourceId: String
) {

    companion object {

        /**
         * 입력 포트 리소스를 HTTP 응답으로 변환합니다.
         *
         * @param dto 입력 포트 리소스
         * @return HTTP 응답 리소스
         */
        fun from(dto: AssociatedResourceDto): AssociatedResourceResponse {
            return AssociatedResourceResponse(
                type = dto.type,
                resourceId = dto.resourceId
            )
        }
    }
}
