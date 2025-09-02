package net.noti_me.dymit.dymit_backend_api.units.application.study_schedule.dto

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleUpdateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo.LocationVo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.RoleAssignment
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleLocation
import java.time.LocalDateTime

/**
 * StudyScheduleUpdateCommand 테스트 클래스
 * 스터디 일정 업데이트 명령 DTO의 생성자와 속성을 테스트합니다.
 */
class StudyScheduleUpdateCommandTest : AnnotationSpec() {

    /**
     * 유효한 데이터로 StudyScheduleUpdateCommand 객체를 생성하는 헬퍼 함수
     */
    private fun createValidStudyScheduleUpdateCommand() = StudyScheduleUpdateCommand(
        title = "테스트 스터디 일정",
        description = "테스트 설명",
        scheduleAt = LocalDateTime.of(2024, 12, 25, 14, 0),
        location = LocationVo(
            type = ScheduleLocation.LocationType.OFFLINE,
            value = "서울시 강남구 테스트 카페"
        ),
        roles = listOf(
            RoleAssignment(
                memberId = "member1",
                roles = listOf("발표자", "진행자")
            ),
            RoleAssignment(
                memberId = "member2",
                roles = listOf("참여자")
            )
        )
    )

    @Test
    fun `생성자로 객체를 정상 생성할 수 있다`() {
        // Given
        val title = "테스트 스터디 일정"
        val description = "테스트 설명"
        val scheduleAt = LocalDateTime.of(2024, 12, 25, 14, 0)
        val location = LocationVo(
            type = ScheduleLocation.LocationType.OFFLINE,
            value = "서울시 강남구 테스트 카페"
        )
        val roles = listOf(
            RoleAssignment(
                memberId = "member1",
                roles = listOf("발표자", "진행자")
            )
        )

        // When
        val command = StudyScheduleUpdateCommand(
            title = title,
            description = description,
            scheduleAt = scheduleAt,
            location = location,
            roles = roles
        )

        // Then
        command.title shouldBe title
        command.description shouldBe description
        command.scheduleAt shouldBe scheduleAt
        command.location shouldBe location
        command.roles shouldBe roles
    }

    @Test
    fun `빈 역할 목록으로 객체를 생성할 수 있다`() {
        // Given
        val emptyRoles = emptyList<RoleAssignment>()

        // When
        val command = StudyScheduleUpdateCommand(
            title = "테스트 일정",
            description = "설명",
            scheduleAt = LocalDateTime.now(),
            location = LocationVo(),
            roles = emptyRoles
        )

        // Then
        command.roles shouldBe emptyRoles
        command.roles.size shouldBe 0
    }

    @Test
    fun `온라인 위치 타입으로 객체를 생성할 수 있다`() {
        // Given
        val onlineLocation = LocationVo(
            type = ScheduleLocation.LocationType.ONLINE,
            value = "https://zoom.us/test-meeting"
        )

        // When
        val command = StudyScheduleUpdateCommand(
            title = "온라인 스터디",
            description = "온라인 스터디 설명",
            scheduleAt = LocalDateTime.now(),
            location = onlineLocation,
            roles = emptyList()
        )

        // Then
        command.location.type shouldBe ScheduleLocation.LocationType.ONLINE
        command.location.value shouldBe "https://zoom.us/test-meeting"
    }

    @Test
    fun `여러 개의 역할이 할당된 객체를 생성할 수 있다`() {
        // Given
        val multipleRoles = listOf(
            RoleAssignment(memberId = "member1", roles = listOf("발표자")),
            RoleAssignment(memberId = "member2", roles = listOf("진행자")),
            RoleAssignment(memberId = "member3", roles = listOf("참여자", "기록자"))
        )

        // When
        val command = StudyScheduleUpdateCommand(
            title = "테스트 스터디 일정",
            description = "테스트 설명",
            scheduleAt = LocalDateTime.of(2024, 12, 25, 14, 0),
            location = LocationVo(
                type = ScheduleLocation.LocationType.OFFLINE,
                value = "서울시 강남구 테스트 카페"
            ),
            roles = multipleRoles
        )

        // Then
        command.roles.size shouldBe 3
        command.roles[0].memberId shouldBe "member1"
        command.roles[0].roles shouldBe listOf("발표자")
        command.roles[2].roles shouldBe listOf("참여자", "기록자")
    }
}
