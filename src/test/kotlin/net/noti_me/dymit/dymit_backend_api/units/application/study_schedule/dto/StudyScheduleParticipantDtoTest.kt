package net.noti_me.dymit.dymit_backend_api.units.application.study_schedule.dto

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleParticipantDto
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleLocation
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import org.bson.types.ObjectId
import java.time.LocalDateTime

/**
 * StudyScheduleParticipantDto 테스트 클래스
 * 스터디 일정 참여자 DTO의 생성자와 팩토리 메서드를 테스트합니다.
 */
class StudyScheduleParticipantDtoTest : AnnotationSpec() {

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
            roles = mutableSetOf(),
            nrParticipant = 0L
        )
    }

    /**
     * 테스트용 StudyGroupMember를 생성하는 헬퍼 함수
     */
    private fun createTestStudyGroupMember(): StudyGroupMember {
        return StudyGroupMember(
            groupId = ObjectId.get(),
            memberId = ObjectId.get(),
            nickname = "테스트유저",
            profileImage = ProfileImageVo(type = "preset", url = "1"),
            role = GroupMemberRole.MEMBER
        )
    }

    @Test
    fun `생성자로 객체를 정상적으로 생성할 수 있다`() {
        // Given
        val scheduleId = "schedule123"
        val memberId = "member456"
        val nickname = "참여자닉네임"
        val image = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(type = "preset", url = "2")

        // When
        val dto = StudyScheduleParticipantDto(
            scheduleId = scheduleId,
            memberId = memberId,
            nickname = nickname,
            image = image
        )

        // Then
        dto.scheduleId shouldBe scheduleId
        dto.memberId shouldBe memberId
        dto.nickname shouldBe nickname
        dto.image shouldBe image
    }

    @Test
    fun `of 메서드로 스케줄과 멤버에서 DTO를 생성할 수 있다`() {
        // Given
        val schedule = createTestStudySchedule()
        val member = createTestStudyGroupMember()

        // When
        val dto = StudyScheduleParticipantDto.of(schedule, member)

        // Then
        dto.scheduleId shouldBe schedule.identifier
        dto.memberId shouldBe member.memberId.toHexString()
        dto.nickname shouldBe member.nickname
        dto.image.type shouldBe member.profileImage.type
        dto.image.url shouldBe member.profileImage.url
    }

    @Test
    fun `다양한 프로필 이미지 타입으로 객체를 생성할 수 있다`() {
        // Given
        val customImage = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(type = "custom", url = "https://example.com/profile.jpg")

        // When
        val dto = StudyScheduleParticipantDto(
            scheduleId = "schedule1",
            memberId = "member1",
            nickname = "커스텀유저",
            image = customImage
        )

        // Then
        dto.image.type shouldBe "custom"
        dto.image.url shouldBe "https://example.com/profile.jpg"
    }

    @Test
    fun `preset 이미지 타입의 멤버로 DTO를 생성할 수 있다`() {
        // Given
        val schedule = createTestStudySchedule()
        val member = StudyGroupMember(
            groupId = ObjectId.get(),
            memberId = ObjectId.get(),
            nickname = "테스트유저",
            profileImage = ProfileImageVo(type = "preset", url = "5"),
            role = GroupMemberRole.MEMBER
        )

        // When
        val dto = StudyScheduleParticipantDto.of(schedule, member)

        // Then
        dto.image.type shouldBe "preset"
        dto.image.url shouldBe "5"
    }

    @Test
    fun `긴 닉네임을 가진 멤버로 DTO를 생성할 수 있다`() {
        // Given
        val schedule = createTestStudySchedule()
        val longNickname = "매우긴닉네임을가진사용자입니다"
        val member = StudyGroupMember(
            groupId = ObjectId.get(),
            memberId = ObjectId.get(),
            nickname = longNickname,
            profileImage = ProfileImageVo(type = "preset", url = "1"),
            role = GroupMemberRole.MEMBER
        )

        // When
        val dto = StudyScheduleParticipantDto.of(schedule, member)

        // Then
        dto.nickname shouldBe longNickname
    }

    @Test
    fun `다양한 ObjectId를 가진 객체들로 DTO를 생성할 수 있다`() {
        // Given
        val scheduleId = ObjectId.get()
        val groupId = ObjectId.get()
        val memberId = ObjectId.get()

        val schedule = StudySchedule(
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
            roles = mutableSetOf(),
            nrParticipant = 0L
        )
        val member = StudyGroupMember(
            groupId = groupId,
            memberId = memberId,
            nickname = "테스트유저",
            profileImage = ProfileImageVo(type = "preset", url = "1"),
            role = GroupMemberRole.MEMBER
        )

        // When
        val dto = StudyScheduleParticipantDto.of(schedule, member)

        // Then
        dto.scheduleId shouldBe schedule.identifier
        dto.memberId shouldBe memberId.toHexString()
    }

    @Test
    fun `OWNER 역할의 멤버로 DTO를 생성할 수 있다`() {
        // Given
        val schedule = createTestStudySchedule()
        val ownerMember = StudyGroupMember(
            groupId = ObjectId.get(),
            memberId = ObjectId.get(),
            nickname = "그룹장",
            profileImage = ProfileImageVo(type = "preset", url = "1"),
            role = GroupMemberRole.OWNER
        )

        // When
        val dto = StudyScheduleParticipantDto.of(schedule, ownerMember)

        // Then
        dto.nickname shouldBe "그룹장"
        dto.memberId shouldBe ownerMember.memberId.toHexString()
    }

    @Test
    fun `ADMIN 역할의 멤버로 DTO를 생성할 수 있다`() {
        // Given
        val schedule = createTestStudySchedule()
        val adminMember = StudyGroupMember(
            groupId = ObjectId.get(),
            memberId = ObjectId.get(),
            nickname = "관리자",
            profileImage = ProfileImageVo(type = "preset", url = "1"),
            role = GroupMemberRole.ADMIN
        )

        // When
        val dto = StudyScheduleParticipantDto.of(schedule, adminMember)

        // Then
        dto.nickname shouldBe "관리자"
        dto.memberId shouldBe adminMember.memberId.toHexString()
    }
}
