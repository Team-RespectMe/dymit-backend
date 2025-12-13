package net.noti_me.dymit.dymit_backend_api.controllers.server_notice

import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.server_notice.ServerNoticeServiceFacade
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.server_notice.dto.CreateServerNoticeRequest
import net.noti_me.dymit.dymit_backend_api.controllers.server_notice.dto.ServerNoticeResponse
import net.noti_me.dymit.dymit_backend_api.controllers.server_notice.dto.ServerNoticeSummaryResponse
import net.noti_me.dymit.dymit_backend_api.controllers.server_notice.dto.UpdateServerNoticeRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/server-notices")
class ServerNoticeRestController(
    private val serverNoticeService: ServerNoticeServiceFacade
): ServerNoticeApi {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed("ADMIN")
    override fun postNotice(
        @LoginMember loginMember: MemberInfo,
        @RequestBody @Valid @Sanitize request: CreateServerNoticeRequest
    ) {
        serverNoticeService.createNotice(loginMember, request.toCommand())
    }

    @PutMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("ADMIN")
    override fun putNotice(
        @LoginMember loginMember: MemberInfo,
        @PathVariable noticeId: String,
        @RequestBody @Valid @Sanitize request: UpdateServerNoticeRequest
    ) {
        serverNoticeService.updateNotice(
            loginMember,
            request.toCommand(noticeId)
        )
    }

    @DeleteMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed("ADMIN")
    override fun deleteNotice(
        @LoginMember loginMember: MemberInfo,
        @PathVariable noticeId: String
    ) {
        serverNoticeService.deleteNotice(loginMember, noticeId)
    }

    @GetMapping
//    @Secured("ROLE_MEMBER")
//    @PermitAll
    @RolesAllowed("MEMBER")
    override fun getNotices(
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false, defaultValue = "20") size: Int?
    ): ListResponse<ServerNoticeSummaryResponse> {

        logger.debug(SecurityContextHolder.getContext().authentication.principal.toString())
        val items = serverNoticeService.getNotices(
            cursor = cursor,
            size = size ?: 20
        ).map { ServerNoticeSummaryResponse.from(it) }

        return ListResponse.of(
            size = size ?: 20,
            items = items,
            extractors = buildMap {
                put("cursor") { it.id }
                put("size") { size ?: 20 }
            }
        )
    }

    @GetMapping("/{noticeId}")
    @PermitAll()
    override fun getNotice(@PathVariable noticeId: String): ServerNoticeResponse {
        val noticeDto = serverNoticeService.getNotice(noticeId)
        return ServerNoticeResponse.from(noticeDto)
    }
}