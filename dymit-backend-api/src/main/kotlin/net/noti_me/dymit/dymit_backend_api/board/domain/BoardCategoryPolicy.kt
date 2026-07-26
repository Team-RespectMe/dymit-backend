package net.noti_me.dymit.dymit_backend_api.board.domain

/**
 * 게시판 카테고리별 작성 정책입니다.
 *
 * @property category 적용 카테고리
 * @property enabled 카테고리 사용 여부
 * @property writePolicy 카테고리 작성 정책
 */
data class BoardCategoryPolicy(
    val category: PostCategory,
    val enabled: Boolean = true,
    val writePolicy: BoardCategoryWritePolicy
) {

    fun canWriteByRole(role: BoardMemberRole): Boolean {
        return when (writePolicy) {
            BoardCategoryWritePolicy.GROUP_ADMIN_ONLY ->
                role == BoardMemberRole.OWNER || role == BoardMemberRole.ADMIN

            BoardCategoryWritePolicy.SCHEDULE_PARTICIPANT_ONLY ->
                role == BoardMemberRole.OWNER ||
                    role == BoardMemberRole.ADMIN ||
                    role == BoardMemberRole.MEMBER

            BoardCategoryWritePolicy.ALL_MEMBERS ->
                role == BoardMemberRole.OWNER ||
                    role == BoardMemberRole.ADMIN ||
                    role == BoardMemberRole.MEMBER
        }
    }

    companion object {
        fun defaults(): MutableSet<BoardCategoryPolicy> {
            return mutableSetOf(
                BoardCategoryPolicy(
                    category = PostCategory.NOTICE,
                    enabled = true,
                    writePolicy = BoardCategoryWritePolicy.GROUP_ADMIN_ONLY
                ),
                BoardCategoryPolicy(
                    category = PostCategory.RETROSPECTIVE,
                    enabled = true,
                    writePolicy = BoardCategoryWritePolicy.SCHEDULE_PARTICIPANT_ONLY
                ),
                BoardCategoryPolicy(
                    category = PostCategory.QUESTION,
                    enabled = true,
                    writePolicy = BoardCategoryWritePolicy.ALL_MEMBERS
                ),
                BoardCategoryPolicy(
                    category = PostCategory.ASSIGNMENT,
                    enabled = true,
                    writePolicy = BoardCategoryWritePolicy.ALL_MEMBERS
                )
            )
        }
    }
}

enum class BoardCategoryWritePolicy {
    GROUP_ADMIN_ONLY,
    SCHEDULE_PARTICIPANT_ONLY,
    ALL_MEMBERS
}
