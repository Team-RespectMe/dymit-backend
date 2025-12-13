package net.noti_me.dymit.dymit_backend_api.controllers.study_group

import net.noti_me.dymit.dymit_backend_api.application.study_group.StudyGroupQueryService
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.IntegerValueResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class MyStudyGroupController(
    private val studyGroupQueryService: StudyGroupQueryService
): MyStudyGroupApi {

    @GetMapping("/members/me/study-groups/owned/count")
    @ResponseStatus(HttpStatus.OK)
    override fun countMyOwnedStudyGroups(
        @LoginMember memberInfo: MemberInfo
    ): IntegerValueResponse {
        return IntegerValueResponse(
            value = studyGroupQueryService.getOwnedGroupCount(memberInfo).toInt()
        )
    }
}