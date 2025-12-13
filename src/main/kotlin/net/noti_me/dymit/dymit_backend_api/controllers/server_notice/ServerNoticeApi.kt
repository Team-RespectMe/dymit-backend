package net.noti_me.dymit.dymit_backend_api.controllers.server_notice

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.server_notice.dto.CreateServerNoticeRequest
import net.noti_me.dymit.dymit_backend_api.controllers.server_notice.dto.ServerNoticeResponse
import net.noti_me.dymit.dymit_backend_api.controllers.server_notice.dto.ServerNoticeSummaryResponse
import net.noti_me.dymit.dymit_backend_api.controllers.server_notice.dto.UpdateServerNoticeRequest

@Tag(name = "서버 공지 API", description = "서버 공지와 관련된 API")
interface ServerNoticeApi {

    @Operation(method = "POST",
        summary = "서버 공지 등록",
        description = "서버 공지를 등록합니다. 관리자 권한이 필요합니다.",
    )
    @SecurityRequirement(name = "bearer-jwt")
    fun postNotice(
        loginMember: MemberInfo,
        @Valid request: CreateServerNoticeRequest
    )

    @Operation(method = "PUT",
        summary = "서버 공지 수정",
        description = "서버 공지를 수정합니다. 관리자 권한이 필요합니다.",
    )
    @SecurityRequirement(name = "bearer-jwt")
    fun putNotice(
        loginMember: MemberInfo,
        noticeId: String,
        @Valid request: UpdateServerNoticeRequest
    )

    @Operation(
        method = "DELETE",
        summary = "서버 공지 삭제",
        description = "서버 공지를 삭제합니다. 관리자 권한이 필요합니다."
    )
    @SecurityRequirement(name = "bearer-jwt")
    fun deleteNotice(loginMember: MemberInfo, noticeId: String)

    @Operation(
        method = "GET",
        summary = "서버 공지 목록 조회",
        description = "서버 공지 목록을 조회합니다."
    )
    fun getNotices(cursor: String?, size: Int?): ListResponse<ServerNoticeSummaryResponse>

    @Operation(
        method = "GET",
        summary = "서버 공지 상세 조회",
        description = "서버 공지 상세를 조회합니다."
    )
    fun getNotice(noticeId: String): ServerNoticeResponse
}