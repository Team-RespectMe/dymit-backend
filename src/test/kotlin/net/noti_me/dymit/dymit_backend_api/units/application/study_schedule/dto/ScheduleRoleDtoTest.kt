package net.noti_me.dymit.dymit_backend_api.units.application.study_schedule.dto

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.ScheduleRoleDto
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleRole
import org.bson.types.ObjectId

/**
 * ScheduleRoleDto 테스트 클래스
 * 스케줄 역할 DTO의 생성자, 도메인 변환 메서드, 팩토리 메서드를 테스트합니다.
 */
class ScheduleRoleDtoTest : AnnotationSpec() {

    /**
     * 테스트용 ScheduleRole 도메인 객체를 생성하는 헬퍼 함수
     */
    private fun createTestScheduleRole(): ScheduleRole {
        return ScheduleRole(
            memberId = ObjectId.get(),
            nickname = "테스트유저",
            image = ProfileImageVo(type = "preset", url = "1"),
            roles = listOf("발표자", "진행자"),
            color = "#FF3357"
        )
    }

    @Test
    fun `기본값으로 객체를 생성할 수 있다`() {
        // When
        val dto = ScheduleRoleDto(
            roles = listOf("발표자")
        )

        // Then
        dto.memberId shouldBe ""
        dto.nickname shouldBe ""
        dto.image.type shouldBe "preset"
        dto.image.url shouldBe "0"
        dto.roles shouldBe listOf("발표자")
        dto.color shouldBe "#FF3357"
    }

    @Test
    fun `생성자로 객체를 정상적으로 생성할 수 있다`() {
        // Given
        val memberId = "member123"
        val nickname = "테스트유저"
        val image = ProfileImageVo(type = "custom", url = "profile.jpg")
        val roles = listOf("발표자", "진행자")
        val color = "#00FF00"

        // When
        val dto = ScheduleRoleDto(
            memberId = memberId,
            nickname = nickname,
            image = image,
            roles = roles,
            color = color
        )

        // Then
        dto.memberId shouldBe memberId
        dto.nickname shouldBe nickname
        dto.image shouldBe image
        dto.roles shouldBe roles
        dto.color shouldBe color
    }

    @Test
    fun `toDomain 메서드로 도메인 객체를 생성할 수 있다`() {
        // Given
        val memberId = ObjectId.get().toHexString()
        val dto = ScheduleRoleDto(
            memberId = memberId,
            nickname = "테스트유저",
            image = ProfileImageVo(type = "preset", url = "2"),
            roles = listOf("발표자"),
            color = "#FF0000"
        )

        // When
        val domain = dto.toDomain()

        // Then
        domain.memberId.toHexString() shouldBe memberId
        domain.nickname shouldBe dto.nickname
        domain.image shouldBe dto.image
        domain.roles shouldBe dto.roles
        domain.color shouldBe dto.color
    }

    @Test
    fun `from 메서드로 도메인에서 DTO를 생성할 수 있다`() {
        // Given
        val domain = createTestScheduleRole()

        // When
        val dto = ScheduleRoleDto.from(domain)

        // Then
        dto.memberId shouldBe domain.memberId.toHexString()
        dto.nickname shouldBe domain.nickname
        dto.image shouldBe domain.image
        dto.roles shouldBe domain.roles
        dto.color shouldBe domain.color
    }

    @Test
    fun `빈 역할 목록으로 객체를 생성할 수 있다`() {
        // Given
        val emptyRoles = emptyList<String>()

        // When
        val dto = ScheduleRoleDto(
            memberId = "member1",
            nickname = "유저",
            roles = emptyRoles
        )

        // Then
        dto.roles shouldBe emptyList()
        dto.roles.size shouldBe 0
    }

    @Test
    fun `복수의 역할로 객체를 생성할 수 있다`() {
        // Given
        val multipleRoles = listOf("발표자", "진행자", "기록자", "타임키퍼")

        // When
        val dto = ScheduleRoleDto(
            memberId = "member1",
            nickname = "멀티로울유저",
            roles = multipleRoles
        )

        // Then
        dto.roles shouldBe multipleRoles
        dto.roles.size shouldBe 4
    }

    @Test
    fun `다양한 색상 코드로 객체를 생성할 수 있다`() {
        // Given
        val colorCode = "#0099FF"

        // When
        val dto = ScheduleRoleDto(
            memberId = "member1",
            nickname = "블루유저",
            roles = listOf("참여자"),
            color = colorCode
        )

        // Then
        dto.color shouldBe colorCode
    }

    @Test
    fun `toDomain과 from 메서드가 양방향 변환을 지원한다`() {
        // Given
        val originalDto = ScheduleRoleDto(
            memberId = ObjectId.get().toHexString(),
            nickname = "원본유저",
            image = ProfileImageVo(type = "preset", url = "3"),
            roles = listOf("발표자", "진행자"),
            color = "#FFAA00"
        )

        // When
        val domain = originalDto.toDomain()
        val convertedDto = ScheduleRoleDto.from(domain)

        // Then
        convertedDto.memberId shouldBe originalDto.memberId
        convertedDto.nickname shouldBe originalDto.nickname
        convertedDto.image shouldBe originalDto.image
        convertedDto.roles shouldBe originalDto.roles
        convertedDto.color shouldBe originalDto.color
    }
}
