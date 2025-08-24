package net.noti_me.dymit.dymit_backend_api.units.application.study_group

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.board.BoardService
import net.noti_me.dymit.dymit_backend_api.application.study_group.StudyGroupCommandServiceImpl
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.MemberPreview
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroup
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher

class StudyGroupCommandServiceImplTest : BehaviorSpec({

    val loadMemberPort: LoadMemberPort = mockk()
    val loadStudyGroupPort = mockk<LoadStudyGroupPort>(relaxed = true)
    val saveStudyGroupPort = mockk<SaveStudyGroupPort>(relaxed = true)
    val studyGroupMemberRepository = mockk<StudyGroupMemberRepository>(relaxed = true)
    val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    val boardService = mockk<BoardService>(relaxed = true)
    val member = createMemberEntity()
    val memberInfo = createMemberInfo(member)

    val studyGroupCommandService = StudyGroupCommandServiceImpl(
        loadMemberPort = loadMemberPort,
        loadStudyGroupPort = loadStudyGroupPort,
        saveStudyGroupPort = saveStudyGroupPort,
        studyGroupMemberRepository = studyGroupMemberRepository,
        boardService = boardService,
        applicationEventPublisher = applicationEventPublisher
    )

    given("스터디 그룹을 생성한다.") {
        val expected = StudyGroup(
            name = "테스트 스터디 그룹",
            description = "테스트 스터디 그룹 설명",
            ownerId = ObjectId.get()
        )
        val command = StudyGroupCreateCommand(
            name = "테스트 스터디 그룹",
            description = "테스트 스터디 그룹 설명"
        )

        `when`("그룹 명이 중복되지 않으면") {
            every { saveStudyGroupPort.persist(any()) } returns expected
            every { loadMemberPort.loadById(member.identifier) } returns member
            then("스터디 그룹이 생성되어야 한다.") {
                val result = studyGroupCommandService.createStudyGroup(memberInfo, command)
                result.groupId shouldBe expected.identifier
                result.name shouldBe  command.name
                result.description shouldBe command.description
            }
        }
    }
})