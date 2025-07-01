package net.noti_me.dymit.dymit_backend_api.domain.studyGroup

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.domain.AbstractAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.events.StudyGroupProfileImageDeleteEvent
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.events.StudyGroupOwnerChangedEvent
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import java.security.Permission


/**
 * 스터디 그룹 도메인 객체
 * 이 객체는 스터디 그룹의 기본 정보를 포함하며, 그룹 이름, 설명, 소유자 ID, 프로필 이미지 등을 관리합니다.
 * 또한 그룹 이름과 설명을 변경하는 메서드, 소유자를 변경하는 메서드, 프로필 이미지를 삭제하는 메서드를 제공합니다.
 * 도메인 규칙에 따라 그룹 이름과 설명은 특정 길이 제한을 가지며, 소유자 변경은 현재 소유자만 수행할 수 있습니다.
 * 프로필 이미지를 삭제할 때는 이미지가 존재하는 경우에만 삭제할 수 있으며, 삭제 시 이벤트를 발생시킵니다.
 * 이 객체는 스프링 데이터 MongoDB의 문서로 매핑되어 데이터베이스에 저장됩니다.
 * 도메인 이벤트를 사용하여 그룹 소유자 변경 및 프로필 이미지 삭제와 같은 중요한 변경 사항을 다른 서비스에 알릴 수 있습니다.
 * 이 객체는 `BaseAggregateRoot`를 상속받아 도메인 이벤트를 관리합니다.
 * @param id 그룹의 고유 식별자 (선택적)
 * @param ownerId 그룹의 소유자 ID
 * @param boardId 그룹이 속한 게시판 ID
 * @param name 그룹의 이름
 * @param description 그룹의 설명
 * @param profile 그룹의 프로필 이미지 정보 (선택적)
 */
@Document("study_groups")
class StudyGroup(
    id: String? = null,
    ownerId: String = "",
    boardId: String = "",
    name: String = "",
    description: String = "",
    profile: GroupProfileImageVo? = null,
): BaseAggregateRoot<StudyGroup>(id) {

    var description: String = description
        private set

    var name: String = name
        private set

    var ownerId: String = ownerId
        private set

    var boardId: String = boardId

    var profileImage: GroupProfileImageVo? = profile
        private set

    /**
     * 그룹 이름을 변경하는 메서드
     * 이 메서드는 현재 그룹 소유자만 호출할 수 있으며, 새로운 그룹 이름이 비어있거나 길이가 3자 이상 30자 이하가 아닐 경우 예외를 발생시킵니다.
     * @param requesterId 요청자의 ID
     * @param newName 새로운 그룹 이름
     * @throws BadRequestException 그룹 이름이 비어있거나 길이가 3자 이상 30자 이하가 아닐 경우
     * @throws ForbiddenException 현재 소유자가 아닌 사용자가 그룹 이름을 변경하려고 할 경우
     */
    fun changeName(requesterId: String, newName: String) {
        if ( this.ownerId != requesterId ) {
            throw ForbiddenException("그룹 소유자만 그룹 이름을 변경할 수 있습니다.")
        }

        if ( newName.isBlank() ) {
            throw BadRequestException("그룹 이름은 빈 문자열 일 수 없습니다.")
        }

        if ( newName.length < 3 || newName.length > 30 ) {
            throw BadRequestException("그룹 이름은 3자 이상 30자 이하이어야 합니다.")
        }

        this.name = newName
    }

    /**
     * 그룹 설명을 변경하는 메서드
     * 이 메서드는 현재 그룹 소유자만 호출할 수 있으며, 새로운 그룹 설명이 비어있거나 길이가 5자 이상 500자 이하가 아닐 경우
     * 예외를 발생시킵니다.
     * @param requesterId 요청자의 ID
     * @param newDescription 새로운 그룹 설명
     * @throws BadRequestException 그룹 설명이 비어있거나 길이가 5자 이상 500자 이하가 아닐 경우
     * @throws ForbiddenException 현재 소유자가 아닌 사용자가 그룹 설명을 변경하려고 할 경우
     */
    fun changeDescription(requesterId: String, newDescription: String) {
        if ( this.ownerId != requesterId ) {
            throw ForbiddenException("그룹 소유자만 그룹 설명을 변경할 수 있습니다.")
        }

        if ( newDescription.isBlank() ) {
            throw BadRequestException("스터디 그룹의 설명은 빈 문자열 일 수 없습니다.")
        }

        if ( newDescription.length < 5 || newDescription.length > 500 ) {
            throw BadRequestException("스터디 그룹의 설명은 5자 이상 500자 이하이어야 합니다.")
        }

        this.description = newDescription
    }

    /**
     * 그룹 소유자를 변경하는 메서드
     * 이 메서드는 현재 그룹 소유자만 호출할 수 있으며, 새로운 소유자 ID가 비어있거나 현재 소유자와 동일한 경우 예외를 발생시킵니다.
     * 새로운 소유자는 현재 그룹에 속한 멤버여야하며 서비스 레이어에서 검증되어야 합니다.
     * 이 메서드는 `StudyGroupOwnerChangedEvent` 이벤트를 발생시켜 그룹 소유자 변경을 알립니다.
     * @param requesterId 요청자의 ID
     * @param newOwnerId 새로운 소유자 ID
     */
    fun changeOwner(requesterId: String, newOwnerId: String) {
        if ( this.ownerId != requesterId ) {
            throw ForbiddenException("그룹 소유자만 소유자를 변경할 수 있습니다.")
        }
        
        this.ownerId = newOwnerId
        val event = StudyGroupOwnerChangedEvent(this.identifier, requesterId, newOwnerId, this)
        this.registerEvent(event)
    }

    fun updateProfileImage(requesterId: String, profileImage: GroupProfileImageVo) {
        if ( this.ownerId != requesterId ) {
            throw ForbiddenException("그룹 소유자가 설정되어 있지 않습니다.")
        }

        if ( !profileImage.isValid() ) {
            throw BadRequestException("유효하지 않은 프로필 이미지입니다.")
        }
        this.profileImage = profileImage
    }

    /**
     * 프로필 이미지를 삭제하는 메서드
     * 이 메서드는 프로필 이미지가 존재하는 경우에만 호출할 수 있으며,
     * 프로필 이미지가 삭제되면 `StudyGroupProfileImageDeleteEvent` 이벤트를 발생시킵니다.
     * @param requesterId 요청자의 ID
     */
    fun deleteProfileImage(requesterId: String) {
        if ( this.ownerId != requesterId ) {
            throw ForbiddenException("그룹 소유자만 프로필 이미지를 삭제할 수 있습니다.")
        }

        if ( this.profileImage == null ) {
            return;
        }
        this.profileImage = null
        val event = StudyGroupProfileImageDeleteEvent(this.identifier, profileImage, this)
        this.registerEvent(event)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StudyGroup) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }   
}