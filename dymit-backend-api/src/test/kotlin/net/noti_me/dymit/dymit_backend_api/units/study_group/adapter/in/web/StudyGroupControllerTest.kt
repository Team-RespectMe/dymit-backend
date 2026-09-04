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
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.MemberPreview
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.PostPreview
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.StudyGroupMemberQueryDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.StudyGroupQueryModelDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.web.dto.StudyGroupCreateRequest
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.LoadStudyGroupPostPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.study_schedule.StudyGroupSchedulePort
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.domain.ProfileImageVo
import org.bson.types.ObjectId
import java.time.Instant

internal class StudyGroupControllerTest : BehaviorSpec() {

    private val commandService = mockk<StudyGroupCommandService>()
    private val queryService = mockk<StudyGroupQueryService>()
    private val schedulePort = mockk<StudyGroupSchedulePort>()
    private val loadStudyGroupPostPort = mockk<LoadStudyGroupPostPort>()
    private val controller = StudyGroupController(
        commandService,
        queryService,
        schedulePort,
        loadStudyGroupPostPort
    )

    init {
        afterEach { clearAllMocks() }

        given("a study-group creation request") {
            `when`("the REST adapter creates the study group") {
                then("it preserves LoginMember, maps the request to a command, and maps the service DTO to the response") {
                    val member = MemberInfo.of(ObjectId.get().toHexString(), "tester", listOf("ROLE_MEMBER"))
                    val request = StudyGroupCreateRequest(name = "Algorithm", description = "Weekly practice")
                    val createdAt = Instant.parse("2026-07-26T09:00:00Z")
                    val result = StudyGroupDto(
                        groupId = ObjectId.get().toHexString(),
                        profileImage = GroupProfileImageVo(),
                        ownerId = member.memberId,
                        name = request.name,
                        description = request.description,
                        inviteCodeVo = InviteCodeVo("invite", createdAt, createdAt.plusSeconds(1L * 86400L)),
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

        given("a study-group detail query") {
            val member = MemberInfo.of(ObjectId.get().toHexString(), "tester", listOf("ROLE_MEMBER"))
            val groupId = ObjectId.get().toHexString()
            val createdAt = Instant.parse("2026-08-31T10:00:00Z")
            val owner = MemberPreview(
                memberId = ObjectId.get().toHexString(),
                nickname = "owner",
                role = GroupMemberRole.OWNER,
                profileImage = ProfileImageVo()
            )

            `when`("the notice board has a latest post") {
                then("it loads the recent notice, keeps group/member queries, and returns sorted members") {
                    val noticeBoardId = ObjectId.get().toHexString()
                    val recentPost = PostPreview(
                        postId = ObjectId.get().toHexString(),
                        title = "latest notice",
                        createdAt = createdAt.plusSeconds(1L * 86400L)
                    )
                    val group = createStudyGroupQueryModel(
                        id = groupId,
                        owner = owner,
                        noticeBoardId = noticeBoardId,
                        createdAt = createdAt
                    )
                    val members = listOf(
                        createMemberQueryDto(groupId, "member", GroupMemberRole.MEMBER, createdAt.plusSeconds(2L * 60L)),
                        createMemberQueryDto(groupId, "owner", GroupMemberRole.OWNER, createdAt),
                        createMemberQueryDto(groupId, "admin", GroupMemberRole.ADMIN, createdAt.plusSeconds(1L * 60L))
                    )
                    every { queryService.getStudyGroup(member, groupId) } returns group
                    every { loadStudyGroupPostPort.loadLatestPost(noticeBoardId) } returns recentPost
                    every { queryService.getStudyGroupMembers(member, groupId) } returns members

                    val response = controller.getStudyGroup(member, groupId)

                    verify(exactly = 1) { queryService.getStudyGroup(member, groupId) }
                    verify(exactly = 1) { loadStudyGroupPostPort.loadLatestPost(noticeBoardId) }
                    verify(exactly = 1) { queryService.getStudyGroupMembers(member, groupId) }
                    response.id shouldBe groupId
                    response.noticeBoardId shouldBe noticeBoardId
                    response.recentPost?.postId shouldBe recentPost.postId
                    response.recentPost?.title shouldBe recentPost.title
                    response.recentPost?.createdAt shouldBe recentPost.createdAt
                    response.members.map { it.role } shouldBe listOf(
                        GroupMemberRole.OWNER,
                        GroupMemberRole.ADMIN,
                        GroupMemberRole.MEMBER
                    )
                }
            }

            `when`("the notice board id is blank") {
                then("it skips recent-post loading and returns null recentPost") {
                    val group = createStudyGroupQueryModel(
                        id = groupId,
                        owner = owner,
                        noticeBoardId = "",
                        createdAt = createdAt
                    )
                    every { queryService.getStudyGroup(member, groupId) } returns group
                    every { queryService.getStudyGroupMembers(member, groupId) } returns emptyList()

                    val response = controller.getStudyGroup(member, groupId)

                    verify(exactly = 1) { queryService.getStudyGroup(member, groupId) }
                    verify(exactly = 0) { loadStudyGroupPostPort.loadLatestPost(any()) }
                    verify(exactly = 1) { queryService.getStudyGroupMembers(member, groupId) }
                    response.recentPost shouldBe null
                }
            }

            `when`("the notice board has no latest post") {
                then("it returns null recentPost after querying the board") {
                    val noticeBoardId = ObjectId.get().toHexString()
                    val group = createStudyGroupQueryModel(
                        id = groupId,
                        owner = owner,
                        noticeBoardId = noticeBoardId,
                        createdAt = createdAt
                    )
                    every { queryService.getStudyGroup(member, groupId) } returns group
                    every { loadStudyGroupPostPort.loadLatestPost(noticeBoardId) } returns null
                    every { queryService.getStudyGroupMembers(member, groupId) } returns emptyList()

                    val response = controller.getStudyGroup(member, groupId)

                    verify(exactly = 1) { loadStudyGroupPostPort.loadLatestPost(noticeBoardId) }
                    response.recentPost shouldBe null
                }
            }
        }
    }

    private fun createStudyGroupQueryModel(
        id: String,
        owner: MemberPreview,
        noticeBoardId: String,
        createdAt: Instant
    ): StudyGroupQueryModelDto {
        return StudyGroupQueryModelDto(
            id = id,
            name = "Algorithm",
            profileImage = GroupProfileImageVo(),
            owner = owner,
            description = "Weekly practice",
            noticeBoardId = noticeBoardId,
            inviteCode = InviteCodeVo("invite", createdAt, createdAt.plusSeconds(1L * 86400L)),
            createdAt = createdAt
        )
    }

    private fun createMemberQueryDto(
        groupId: String,
        nickname: String,
        role: GroupMemberRole,
        createdAt: Instant
    ): StudyGroupMemberQueryDto {
        return StudyGroupMemberQueryDto(
            groupId = groupId,
            memberId = ObjectId.get().toHexString(),
            nickname = nickname,
            role = role,
            profileImage = ProfileImageVo(),
            createdAt = createdAt
        )
    }
}
