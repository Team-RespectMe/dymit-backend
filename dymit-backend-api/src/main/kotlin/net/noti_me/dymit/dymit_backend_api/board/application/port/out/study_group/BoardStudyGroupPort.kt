package net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group

import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.dto.BoardGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.dto.BoardRecentPostDto
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.dto.BoardStudyGroupDto
import org.bson.types.ObjectId

/**
 * 게시판이 필요로 하는 스터디 그룹 정보를 제공하는 아웃바운드 포트입니다.
 */
interface BoardStudyGroupPort {

    /**
     * 그룹 정보를 조회합니다.
     *
     * @param groupId 그룹 식별자
     * @return 그룹 정보, 없으면 null
     */
    fun loadGroup(groupId: String): BoardStudyGroupDto?

    /**
     * 그룹 멤버 정보를 조회합니다.
     *
     * @param groupId 그룹 식별자
     * @param memberId 멤버 식별자
     * @return 그룹 멤버 정보, 없으면 null
     */
    fun loadMember(groupId: ObjectId, memberId: ObjectId): BoardGroupMemberDto?

    /**
     * 그룹의 최근 게시글 정보를 갱신합니다.
     *
     * @param groupId 그룹 식별자
     * @param recentPost 최근 게시글 정보, 게시글이 없으면 null
     */
    fun updateRecentPost(groupId: String, recentPost: BoardRecentPostDto?)
}
