package net.noti_me.dymit.dymit_backend_api.board.domain

import org.bson.types.ObjectId

/**
 * 게시판 권한 판단에 필요한 그룹 멤버 정보입니다.
 *
 * @property groupId 그룹 식별자
 * @property memberId 멤버 식별자
 * @property nickname 그룹 내 닉네임
 * @property profileImage 프로필 이미지
 * @property role 그룹 역할
 */
data class BoardMember(
    val groupId: ObjectId,
    val memberId: ObjectId,
    val nickname: String,
    val profileImage: BoardProfileImage,
    val role: BoardMemberRole
)

/**
 * 게시판 모듈이 사용하는 그룹 멤버 역할입니다.
 */
enum class BoardMemberRole {
    OWNER,
    ADMIN,
    MEMBER
}

/**
 * 게시판 작성자 프로필 이미지입니다.
 *
 * @property type 이미지 종류
 * @property url 이미지 URL
 */
data class BoardProfileImage(
    val type: BoardProfileImageType = BoardProfileImageType.PRESET,
    val url: String = ""
)
