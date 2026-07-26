package net.noti_me.dymit.dymit_backend_api.study_group.adapter.`in`.web

import net.noti_me.dymit.dymit_backend_api.study_group.application.StudyGroupQueryService
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.IntegerValueResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.web.MyStudyGroupApi
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.PermitAll

@RestController
class MyStudyGroupController(
    private val studyGroupQueryService: StudyGroupQueryService
): MyStudyGroupApi {

    @GetMapping("/members/me/study-groups/owned/count")
    @ResponseStatus(HttpStatus.OK)
    @PermitAll
    override fun countMyOwnedStudyGroups(
        @LoginMember memberInfo: MemberInfo
    ): IntegerValueResponse {
        return IntegerValueResponse(
            value = studyGroupQueryService.getOwnedGroupCount(memberInfo).toInt()
        )
    }
}
