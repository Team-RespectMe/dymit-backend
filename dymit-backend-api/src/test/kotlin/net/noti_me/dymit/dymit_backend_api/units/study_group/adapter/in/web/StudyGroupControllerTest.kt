package net.noti_me.dymit.dymit_backend_api.study_group.adapter.`in`.web

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_group.application.StudyGroupCommandService
import net.noti_me.dymit.dymit_backend_api.study_group.application.StudyGroupQueryService
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.command.StudyGroupCreateCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.command.StudyGroupDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.web.dto.StudyGroupCreateRequest
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.study_schedule.StudyGroupSchedulePort
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupProfileImageVo
import org.bson.types.ObjectId
import java.time.LocalDateTime

internal class StudyGroupControllerTest : BehaviorSpec() {

    private val commandService = mockk<StudyGroupCommandService>()
    private val queryService = mockk<StudyGroupQueryService>()
    private val schedulePort = mockk<StudyGroupSchedulePort>()
    private val controller = StudyGroupController(commandService, queryService, schedulePort)

    init {
        afterEach { clearAllMocks() }

        given("a study-group creation request") {
            `when`("the REST adapter creates the study group") {
                then("it preserves LoginMember, maps the request to a command, and maps the service DTO to the response") {
                    val member = MemberInfo.of(ObjectId.get().toHexString(), "tester", listOf("ROLE_MEMBER"))
                    val request = StudyGroupCreateRequest(name = "Algorithm", description = "Weekly practice")
                    val createdAt = LocalDateTime.of(2026, 7, 26, 9, 0)
                    val result = StudyGroupDto(
                        groupId = ObjectId.get().toHexString(),
                        profileImage = GroupProfileImageVo(),
                        ownerId = member.memberId,
                        name = request.name,
                        description = request.description,
                        inviteCodeVo = InviteCodeVo("invite", createdAt, createdAt.plusDays(1)),
                        createdAt = createdAt
                    )
                    val command = slot<StudyGroupCreateCommand>()
                    every { commandService.createStudyGroup(member, capture(command)) } returns result

                    val response = controller.createStudyGroup(member, request)

                    verify(exactly = 1) { commandService.createStudyGroup(member, any()) }
                    command.captured.name shouldBe request.name
                    command.captured.description shouldBe request.description
                    response.groupId shouldBe result.groupId
                    response.owner shouldBe member.memberId
                    response.inviteCodeVo.code shouldBe "invite"
                    StudyGroupController::class.java.methods
                        .single { it.name == "createStudyGroup" }
                        .parameters[0]
                        .isAnnotationPresent(LoginMember::class.java) shouldBe true
                }
            }
        }
    }
}
