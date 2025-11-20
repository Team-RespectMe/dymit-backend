package net.noti_me.dymit.dymit_backend_api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnProperty(
    value = ["dymit.debug-endpoint.enabled"],
    havingValue = "true",
    matchIfMissing = false)
@Tag(name = "Debug Endpoint", description = "디버그용 엔드포인트")
class TestEndpointController(
    private val loadMemberPort: LoadMemberPort
) {

    @GetMapping("/api/v1/debug/members/{memberId}")
    @Operation(summary = "멤버 전체 정보 조회", description = "멤버의 전체 정보를 조회합니다. 디버그용 엔드포인트입니다.")
    fun getUserFullInfo(@PathVariable memberId: String): Member {
        return loadMemberPort.loadById(memberId)
            ?: throw NotFoundException(message = "Member not found with id: $memberId")
    }
}