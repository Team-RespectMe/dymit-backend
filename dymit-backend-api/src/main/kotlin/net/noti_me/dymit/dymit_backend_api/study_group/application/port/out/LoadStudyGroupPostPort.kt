package net.noti_me.dymit.dymit_backend_api.study_group.application.port.out

import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.PostPreview

/**
 * 스터디 그룹 공지 게시판의 게시글 정보를 조회하는 출력 포트입니다.
 */
fun interface LoadStudyGroupPostPort {

    /**
     * 공지 게시판에서 삭제되지 않은 최신 게시글을 조회합니다.
     *
     * @param boardId 조회할 공지 게시판 ID
     * @return 최신 게시글 미리보기, 게시글이 없으면 null
     */
    fun loadLatestPost(boardId: String): PostPreview?
}
