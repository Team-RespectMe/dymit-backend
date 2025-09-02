package net.noti_me.dymit.dymit_backend_api.units.application.study_schedule.dto

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.ScheduleRoleDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleSummaryDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo.LocationVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleLocation
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import org.bson.types.ObjectId
import java.time.LocalDateTime

/**
 * StudyScheduleSummaryDto 테스트 클래스
 * 스터디 일정 요약 DTO의 생성자와 팩토리 메서드를 테스트합니다.
 */
class StudyScheduleSummaryDtoTest : AnnotationSpec() {

    /**
     * 테스트용 StudySchedule 엔티티를 생성하는 헬퍼 함수
     */
    private fun createTestStudySchedule(): StudySchedule {
        val scheduleId = ObjectId.get()
        val groupId = ObjectId.get()

        return StudySchedule(
            id = scheduleId,
            groupId = groupId,
            session = 1L,
            title = "테스트 스터디 일정",
            description = "테스트 설명",
            scheduleAt = LocalDateTime.of(2024, 12, 25, 14, 0),
            location = ScheduleLocation(
                type = ScheduleLocation.LocationType.OFFLINE,
                value = "서울시 강남구 테스트 카페"
            ),
            roles = mutableSetOf(
                ScheduleRole(
                    memberId = ObjectId.get(),
                    nickname = "테스트유저1",
                    image = ProfileImageVo(type = "preset", url = "1"),
                    roles = listOf("발표자"),
                    color = "#FF3357"
                )
            ),
            nrParticipant = 5L
        )
    }

    @Test
    fun `생성자로 객체를 정상적으로 생성할 수 있다`() {
        // Given
        val id = "schedule123"
        val groupId = "group456"
        val title = "테스트 일정"
        val description = "테스트 설명"
        val scheduleAt = LocalDateTime.parse("2024-12-25T14:00:00")
        val session = 1L
        val location = LocationVo(
            type = ScheduleLocation.LocationType.OFFLINE,
            value = "테스트 장소"
        )
        val participantCount = 3L
        val roles = listOf(
            ScheduleRoleDto(
                memberId = "member1",
                nickname = "유저1",
                roles = listOf("발표자")
            )
        )
        val createdAt = LocalDateTime.now()
        val updatedAt = LocalDateTime.now()

        // When
        val dto = StudyScheduleSummaryDto(
            id = id,
            groupId = groupId,
            title = title,
            description = description,
            scheduleAt = scheduleAt,
            session = session,
            location = location,
            participantCount = participantCount,
            roles = roles,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        // Then
        dto.id shouldBe id
        dto.groupId shouldBe groupId
        dto.title shouldBe title
        dto.description shouldBe description
        dto.scheduleAt shouldBe scheduleAt
        dto.session shouldBe session
        dto.location shouldBe location
        dto.participantCount shouldBe participantCount
        dto.roles shouldBe roles
        dto.createdAt shouldBe createdAt
        dto.updatedAt shouldBe updatedAt
    }

    @Test
    fun `기본값으로 객체를 생성할 수 있다`() {
        // When
        val dto = StudyScheduleSummaryDto(
            id = "id",
            groupId = "groupId",
            title = "title",
            description = "description",
            scheduleAt = LocalDateTime.parse("2024-12-25T14:00:00"),
            session = 1L,
            location = LocationVo(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // Then
        dto.participantCount shouldBe 0L
        dto.roles shouldBe emptyList()
    }

    @Test
    fun `from 메서드로 엔티티에서 DTO를 생성할 수 있다`() {
        // Given
        val entity = createTestStudySchedule()

        // When
        val dto = StudyScheduleSummaryDto.from(entity)

        // Then
        dto.id shouldBe entity.id.toHexString()
        dto.groupId shouldBe entity.groupId.toHexString()
        dto.title shouldBe entity.title
        dto.description shouldBe entity.description
        dto.scheduleAt shouldBe entity.scheduleAt
        dto.session shouldBe entity.session
        dto.location.type shouldBe entity.location.type
        dto.location.value shouldBe entity.location.value
        dto.participantCount shouldBe entity.nrParticipant
        dto.roles.size shouldBe entity.roles.size
    }

    @Test
    fun `빈 역할 목록을 가진 엔티티에서 DTO를 생성할 수 있다`() {
        // Given
        val entity = StudySchedule(
            id = ObjectId.get(),
            groupId = ObjectId.get(),
            session = 1L,
            title = "테스트 스터디 일정",
            description = "테스트 설명",
            scheduleAt = LocalDateTime.of(2024, 12, 25, 14, 0),
            location = ScheduleLocation(
                type = ScheduleLocation.LocationType.OFFLINE,
                value = "서울시 강남구 테스트 카페"
            ),
            roles = mutableSetOf(),
            nrParticipant = 0L
        )

        // When
        val dto = StudyScheduleSummaryDto.from(entity)

        // Then
        dto.roles shouldBe emptyList()
        dto.roles.size shouldBe 0
    }

    @Test
    fun `참여자 수가 0인 엔티티에서 DTO를 생성할 수 있다`() {
        // Given
        val entity = StudySchedule(
            id = ObjectId.get(),
            groupId = ObjectId.get(),
            session = 1L,
            title = "테스트 스터디 일정",
            description = "테스트 설명",
            scheduleAt = LocalDateTime.of(2024, 12, 25, 14, 0),
            location = ScheduleLocation(
                type = ScheduleLocation.LocationType.OFFLINE,
                value = "서울시 강남구 테스트 카페"
            ),
            roles = mutableSetOf(),
            nrParticipant = 0L
        )

        // When
        val dto = StudyScheduleSummaryDto.from(entity)

        // Then
        dto.participantCount shouldBe 0L
    }

    @Test
    fun `createdAt이 null인 엔티티에서 DTO를 생성할 수 있다`() {
        // Given
        val entity = createTestStudySchedule()

        // When
        val dto = StudyScheduleSummaryDto.from(entity)

        // Then
        dto.createdAt shouldNotBe null
        dto.updatedAt shouldNotBe null
    }

    @Test
    fun `온라인 위치 타입을 가진 엔티티에서 DTO를 생성할 수 있다`() {
        // Given
        val entity = StudySchedule(
            id = ObjectId.get(),
            groupId = ObjectId.get(),
            session = 1L,
            title = "테스트 스터디 일정",
            description = "테스트 설명",
            scheduleAt = LocalDateTime.of(2024, 12, 25, 14, 0),
            location = ScheduleLocation(
                type = ScheduleLocation.LocationType.ONLINE,
                value = "https://zoom.us/test-meeting"
            ),
            roles = mutableSetOf(),
            nrParticipant = 0L
        )

        // When
        val dto = StudyScheduleSummaryDto.from(entity)

        // Then
        dto.location.type shouldBe ScheduleLocation.LocationType.ONLINE
        dto.location.value shouldBe "https://zoom.us/test-meeting"
    }

    @Test
    fun `참여자 수를 변경할 수 있다`() {
        // Given
        val dto = StudyScheduleSummaryDto(
            id = "id",
            groupId = "groupId",
            title = "title",
            description = "description",
            scheduleAt = LocalDateTime.parse("2024-12-25T14:00:00"),
            session = 1L,
            location = LocationVo(),
            participantCount = 5L,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // When
        dto.participantCount = 10L

        // Then
        dto.participantCount shouldBe 10L
    }
}
