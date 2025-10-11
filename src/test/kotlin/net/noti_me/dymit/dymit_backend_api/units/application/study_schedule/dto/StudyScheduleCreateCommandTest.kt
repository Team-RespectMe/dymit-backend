package net.noti_me.dymit.dymit_backend_api.units.application.study_schedule.dto

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo.LocationVo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.RoleAssignment
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleLocation
import java.time.LocalDateTime

/**
 * StudyScheduleCreateCommand 테스트 클래스
 * 스터디 일정 생성 명령 DTO의 생성자와 속성을 테스트합니다.
 */
class StudyScheduleCreateCommandTest : AnnotationSpec() {

    /**
     * 유효한 데이터로 StudyScheduleCreateCommand 객체를 생성하는 헬퍼 함수
     */
    private fun createValidStudyScheduleCreateCommand() = StudyScheduleCreateCommand(
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
            )
        )
    )

    @Test
    fun `생성자로 객체를 정상적으로 생성할 수 있다`() {
        // Given
        val title = "새로운 스터디 일정"
        val description = "새로운 스터디 설명"
        val scheduleAt = LocalDateTime.of(2024, 12, 30, 10, 0)
        val location = LocationVo(
            type = ScheduleLocation.LocationType.OFFLINE,
            value = "서울시 서초구 스터디 카페"
        )
        val roles = listOf(
            RoleAssignment(
                memberId = "member123",
                roles = listOf("발표자")
            )
        )

        // When
        val command = StudyScheduleCreateCommand(
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
        val command = StudyScheduleCreateCommand(
            title = "빈 역할 일정",
            description = "역할이 없는 일정",
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
            value = "https://meet.google.com/test"
        )

        // When
        val command = StudyScheduleCreateCommand(
            title = "온라인 스터디",
            description = "화상회의를 통한 스터디",
            scheduleAt = LocalDateTime.now(),
            location = onlineLocation,
            roles = emptyList()
        )

        // Then
        command.location.type shouldBe ScheduleLocation.LocationType.ONLINE
        command.location.value shouldBe "https://meet.google.com/test"
    }

    @Test
    fun `복수의 역할이 할당된 객체를 생성할 수 있다`() {
        // Given
        val multipleRoles = listOf(
            RoleAssignment(memberId = "member1", roles = listOf("발표자", "진행자")),
            RoleAssignment(memberId = "member2", roles = listOf("참여자")),
            RoleAssignment(memberId = "member3", roles = listOf("기록자", "타임키퍼"))
        )

        // When
        val command = StudyScheduleCreateCommand(
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
        command.roles[0].roles.size shouldBe 2
        command.roles[2].roles shouldBe listOf("기록자", "타임키퍼")
    }

    @Test
    fun `미래 시간으로 객체를 생성할 수 있다`() {
        // Given
        val futureTime = LocalDateTime.of(2025, 6, 15, 15, 30)

        // When
        val command = StudyScheduleCreateCommand(
            title = "미래 일정",
            description = "미래 시간 일정",
            scheduleAt = futureTime,
            location = LocationVo(),
            roles = emptyList()
        )

        // Then
        command.scheduleAt shouldBe futureTime
    }
}
