package net.noti_me.dymit.dymit_backend_api.controllers

import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberProfileResponse
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberNicknameUpdateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.member.MemberApi

@RestController
@RequestMapping("/api/v1/members")
class MemberController : MemberApi {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{memberId}")
    override fun getMemberProfile(
        @PathVariable memberId: String
    ) : MemberProfileResponse{

        return MemberProfileResponse.default()
    }

    override fun patchNickname(memberId: String, 
        request: MemberNicknameUpdateRequest)
    : MemberProfileResponse { 
        TODO("Implement patchNickname logic")
    }

    // override fun updateMemberProfileImage() { 
        // TODO("Implement updateMemberProfileImage logic")
    // }
}
