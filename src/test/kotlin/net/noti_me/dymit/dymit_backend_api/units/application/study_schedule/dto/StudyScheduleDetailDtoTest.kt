package net.noti_me.dymit.dymit_backend_api.units.application.study_schedule.dto

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.ScheduleRoleDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleDetailDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleParticipantDto
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo.LocationVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleLocation
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import org.bson.types.ObjectId
import java.time.LocalDateTime

/**
 * StudyScheduleDetailDto 테스트 클래스
 * 스터디 일정 상세 DTO의 생성자와 팩토리 메서드를 테스트합니다.
 */
class StudyScheduleDetailDtoTest : AnnotationSpec() {

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
            nrParticipant = 1L
        )
    }

    /**
     * 테스트용 ScheduleParticipant를 생성하는 헬퍼 함수
     */
    private fun createTestParticipant(): ScheduleParticipant {
        return ScheduleParticipant(
            scheduleId = ObjectId.get(),
            memberId = ObjectId.get()
        )
    }

    @Test
    fun `생성자로 객체를 정상적으로 생성할 수 있다`() {
        // Given
        val id = "schedule123"
        val session = 1L
        val title = "테스트 일정"
        val description = "테스트 설명"
        val scheduleAt = LocalDateTime.of(2024, 12, 25, 14, 0)
        val location = LocationVo(
            type = ScheduleLocation.LocationType.OFFLINE,
            value = "테스트 장소"
        )
        val participants = listOf(
            StudyScheduleParticipantDto(
                scheduleId = "schedule123",
                memberId = "member1",
                nickname = "참여자1",
                image = ProfileImageVo(type = "preset", url = "1")
            )
        )
        val roles = listOf(
            ScheduleRoleDto(
                memberId = "member1",
                nickname = "유저1",
                roles = listOf("발표자")
            )
        )
        val attending = true
        val createdAt = LocalDateTime.now()
        val updatedAt = LocalDateTime.now()

        // When
        val dto = StudyScheduleDetailDto(
            id = id,
            session = session,
            title = title,
            description = description,
            scheduleAt = scheduleAt,
            location = location,
            participants = participants,
            roles = roles,
            attending = attending,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        // Then
        dto.id shouldBe id
        dto.session shouldBe session
        dto.title shouldBe title
        dto.description shouldBe description
        dto.scheduleAt shouldBe scheduleAt
        dto.location shouldBe location
        dto.participants shouldBe participants
        dto.roles shouldBe roles
        dto.attending shouldBe attending
        dto.createdAt shouldBe createdAt
        dto.updatedAt shouldBe updatedAt
    }

    @Test
    fun `기본값으로 객체를 생성할 수 있다`() {
        // When
        val dto = StudyScheduleDetailDto(
            id = "id",
            session = 1L,
            title = "title",
            description = "description",
            scheduleAt = LocalDateTime.now(),
            location = LocationVo()
        )

        // Then
        dto.participants shouldBe emptyList()
        dto.roles shouldBe emptyList()
        dto.attending shouldBe false
        dto.createdAt shouldNotBe null
        dto.updatedAt shouldNotBe null
    }

    @Test
    fun `from 메서드로 참여자 없이 엔티티에서 DTO를 생성할 수 있다`() {
        // Given
        val entity = createTestStudySchedule()

        // When
        val dto = StudyScheduleDetailDto.from(entity)

        // Then
        dto.id shouldBe entity.identifier
        dto.session shouldBe entity.session
        dto.title shouldBe entity.title
        dto.description shouldBe entity.description
        dto.scheduleAt shouldBe entity.scheduleAt
        dto.location.type shouldBe entity.location.type
        dto.location.value shouldBe entity.location.value
        dto.roles.size shouldBe entity.roles.size
        dto.attending shouldBe false
    }

    @Test
    fun `from 메서드로 참여자와 함께 엔티티에서 DTO를 생성할 수 있다`() {
        // Given
        val entity = createTestStudySchedule()
        val participant = createTestParticipant()

        // When
        val dto = StudyScheduleDetailDto.from(entity, participant)

        // Then
        dto.id shouldBe entity.identifier
        dto.session shouldBe entity.session
        dto.title shouldBe entity.title
        dto.description shouldBe entity.description
        dto.scheduleAt shouldBe entity.scheduleAt
        dto.attending shouldBe true
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
        val dto = StudyScheduleDetailDto.from(entity)

        // Then
        dto.roles shouldBe emptyList()
        dto.roles.size shouldBe 0
    }

    @Test
    fun `createdAt이 null인 엔티티에서 DTO를 생성할 수 있다`() {
        // Given
        val entity = createTestStudySchedule()

        // When
        val dto = StudyScheduleDetailDto.from(entity)

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
        val dto = StudyScheduleDetailDto.from(entity)

        // Then
        dto.location.type shouldBe ScheduleLocation.LocationType.ONLINE
        dto.location.value shouldBe "https://zoom.us/test-meeting"
    }

    @Test
    fun `참여자 목록을 변경할 수 있다`() {
        // Given
        val dto = StudyScheduleDetailDto(
            id = "id",
            session = 1L,
            title = "title",
            description = "description",
            scheduleAt = LocalDateTime.now(),
            location = LocationVo()
        )

        val participants = listOf(
            StudyScheduleParticipantDto(
                scheduleId = "schedule1",
                memberId = "member1",
                nickname = "참여자1",
                image = ProfileImageVo(type = "preset", url = "1")
            )
        )

        // When
        dto.participants = participants

        // Then
        dto.participants shouldBe participants
        dto.participants.size shouldBe 1
    }

    @Test
    fun `참여 상태를 변경할 수 있다`() {
        // Given
        val dto = StudyScheduleDetailDto(
            id = "id",
            session = 1L,
            title = "title",
            description = "description",
            scheduleAt = LocalDateTime.now(),
            location = LocationVo(),
            attending = false
        )

        // When
        dto.attending = true

        // Then
        dto.attending shouldBe true
    }
}
