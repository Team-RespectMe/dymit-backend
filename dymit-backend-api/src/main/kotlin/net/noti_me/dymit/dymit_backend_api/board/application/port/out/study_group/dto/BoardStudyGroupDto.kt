package net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.dto

import net.noti_me.dymit.dymit_backend_api.board.domain.BoardMember
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardMemberRole
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardProfileImage
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardProfileImageType
import org.bson.types.ObjectId
import java.time.LocalDateTime

/**
 * 게시판 이벤트 처리에 필요한 그룹 정보입니다.
 */
data class BoardStudyGroupDto(
    val id: ObjectId,
    val ownerId: ObjectId,
    val name: String,
    val profileImageThumbnail: String,
    val recentPost: BoardRecentPostDto?
) {

    val identifier: String
        get() = id.toHexString()
}

/**
 * 게시판 권한 및 작성자 생성에 필요한 그룹 멤버 정보입니다.
 */
data class BoardGroupMemberDto(
    val groupId: ObjectId,
    val memberId: ObjectId,
    val nickname: String,
    val profileImageType: BoardProfileImageType,
    val profileImageUrl: String,
    val role: BoardMemberRole
) {

    /**
     * 게시판 도메인 멤버로 변환합니다.
     *
     * @return 게시판 도메인 멤버
     */
    fun toDomain(): BoardMember {
        return BoardMember(
            groupId = groupId,
            memberId = memberId,
            nickname = nickname,
            profileImage = BoardProfileImage(
                type = profileImageType,
                url = profileImageUrl
            ),
            role = role
        )
    }
}

/**
 * 그룹에 반영할 최근 게시글 정보입니다.
 */
data class BoardRecentPostDto(
    val postId: String,
    val title: String,
    val createdAt: LocalDateTime
)
