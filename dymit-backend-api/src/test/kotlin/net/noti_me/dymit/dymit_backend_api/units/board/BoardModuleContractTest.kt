package net.noti_me.dymit.dymit_backend_api.units.board

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto.v2.PostCommandRequestV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.v2.BoardRepositoryV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.v2.PostRepositoryV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.BoardStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.dto.BoardGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.board.application.v2.GetBoardPostsUseCaseImplV2
import net.noti_me.dymit.dymit_backend_api.board.domain.Board
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardAction
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardMemberRole
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardPermission
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardProfileImageType
import net.noti_me.dymit.dymit_backend_api.board.domain.PostCategory
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.bson.types.ObjectId

internal class BoardModuleContractTest : BehaviorSpec({
    given("게시판 권한과 같은 그룹 멤버가 주어지면") {
        val groupId = ObjectId.get()
        val memberId = ObjectId.get()
        val board = Board(
            id = ObjectId.get(),
            groupId = groupId,
            name = "질문",
            permissions = mutableSetOf(
                BoardPermission(BoardMemberRole.MEMBER, mutableListOf(BoardAction.READ_POST))
            )
        )
        val member = BoardGroupMemberDto(
            groupId, memberId, "멤버", BoardProfileImageType.PRESET, "", BoardMemberRole.MEMBER
        )

        `when`("게시글 목록을 커서와 카테고리로 조회하면") {
            val boardRepository = mockk<BoardRepositoryV2>()
            val postRepository = mockk<PostRepositoryV2>()
            val groupPort = mockk<BoardStudyGroupPort>()
            every { boardRepository.findById(board.id!!) } returns board
            every { groupPort.loadMember(groupId, memberId) } returns member
            every { postRepository.findByBoardIdLteId(board.id!!.toHexString(), "cursor", 10, PostCategory.QUESTION) } returns emptyList()

            then("새 보드 포트로 커서 조회 조건을 전달한다") {
                val result = GetBoardPostsUseCaseImplV2(postRepository, boardRepository, groupPort).execute(
                    MemberInfo(memberId.toHexString(), "멤버", emptyList()),
                    groupId.toHexString(), board.id!!.toHexString(), "cursor", 10, PostCategory.QUESTION
                )

                result.size shouldBe 0
                verify(exactly = 1) {
                    postRepository.findByBoardIdLteId(board.id!!.toHexString(), "cursor", 10, PostCategory.QUESTION)
                }
            }
        }
    }

    given("회고 게시글 요청이 주어지면") {
        `when`("일정 식별자가 없으면") {
            then("REST 요청 검증 조건이 거짓이다") {
                PostCommandRequestV2("제목", "내용", PostCategory.RETROSPECTIVE).hasValidScheduleIdForRetrospective() shouldBe false
            }
        }

        `when`("유효한 일정 식별자가 있으면") {
            then("REST 요청이 커맨드로 같은 필드를 전달한다") {
                val scheduleId = ObjectId.get().toHexString()
                val command = PostCommandRequestV2("제목", "내용", PostCategory.RETROSPECTIVE, scheduleId)
                    .toCommand("group", "board")

                command.category shouldBe PostCategory.RETROSPECTIVE
                command.scheduleId shouldBe scheduleId
            }
        }
    }
})
