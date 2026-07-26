package net.noti_me.dymit.dymit_backend_api.board.adapter.out.study_group

import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.BoardStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.dto.BoardGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.dto.BoardRecentPostDto
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.dto.BoardStudyGroupDto
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardMemberRole
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardProfileImageType
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupCommandPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupQueryPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupRecentPostDto
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

/**
 * 스터디 그룹 공개 계약을 게시판 아웃바운드 포트로 변환하는 어댑터입니다.
 */
@Component
class StudyGroupBoardAdapter(
    private val queryPort: StudyGroupQueryPort,
    private val commandPort: StudyGroupCommandPort,
    private val memberPort: StudyGroupMemberPort
) : BoardStudyGroupPort {

    override fun loadGroup(groupId: String): BoardStudyGroupDto? {
        return queryPort.loadByGroupId(groupId)?.let { group ->
            BoardStudyGroupDto(
                id = group.id!!,
                ownerId = group.ownerId,
                name = group.name,
                profileImageThumbnail = group.profileImage.thumbnail,
                recentPost = group.recentPost?.let {
                    BoardRecentPostDto(
                        postId = it.postId,
                        title = it.title,
                        createdAt = it.createdAt
                    )
                }
            )
        }
    }

    override fun loadMember(groupId: ObjectId, memberId: ObjectId): BoardGroupMemberDto? {
        return memberPort.findByGroupIdAndMemberId(groupId, memberId)?.let { member ->
            BoardGroupMemberDto(
                groupId = member.groupId,
                memberId = member.memberId,
                nickname = member.nickname,
                profileImageType = BoardProfileImageType.valueOf(member.profileImage.type.name),
                profileImageUrl = member.profileImage.url,
                role = BoardMemberRole.valueOf(member.role.name)
            )
        }
    }

    override fun updateRecentPost(groupId: String, recentPost: BoardRecentPostDto?) {
        val group = queryPort.loadByGroupId(groupId) ?: return
        group.updateRecentPost(
            recentPost?.let {
                StudyGroupRecentPostDto(
                    postId = it.postId,
                    title = it.title,
                    createdAt = it.createdAt
                )
            }
        )
        commandPort.update(group)
    }
}
