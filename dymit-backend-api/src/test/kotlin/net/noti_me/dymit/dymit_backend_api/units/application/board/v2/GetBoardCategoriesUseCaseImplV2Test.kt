package net.noti_me.dymit.dymit_backend_api.units.application.board.v2

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.board.v2.impl.GetBoardCategoriesUseCaseImplV2
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.BoardRepositoryV2
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.supports.createProfileImageVo
import org.bson.types.ObjectId

internal class GetBoardCategoriesUseCaseImplV2Test : BehaviorSpec({

    val boardRepository = mockk<BoardRepositoryV2>()
    val studyGroupMemberRepository = mockk<StudyGroupMemberRepository>()

    val useCase = GetBoardCategoriesUseCaseImplV2(
        boardRepository = boardRepository,
        groupMemberRepository = studyGroupMemberRepository
    )

    val groupId = ObjectId.get()
    val boardId = ObjectId.get()
    val memberId = ObjectId.get()

    fun memberInfo(): MemberInfo {
        return MemberInfo(
            memberId = memberId.toHexString(),
            nickname = "member",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
    }

    fun createGroupMember(): StudyGroupMember {
        return StudyGroupMember(
            id = ObjectId.get(),
            groupId = groupId,
            memberId = memberId,
            nickname = "nickname",
            profileImage = createProfileImageVo(),
            role = GroupMemberRole.MEMBER
        )
    }

    fun createBoard(targetGroupId: ObjectId = groupId): Board {
        return Board(
            id = boardId,
            groupId = targetGroupId,
            name = "게시판"
        )
    }

    beforeEach {
        clearAllMocks()
    }

    given("게시판 카테고리 정책 V2 조회 시") {
        `when`("정상 요청이면") {
            then("카테고리 정책을 카테고리 이름 순으로 반환한다") {
                every { boardRepository.findById(boardId) } returns createBoard()
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns createGroupMember()

                val result = useCase.getCategories(
                    memberInfo = memberInfo(),
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString()
                )

                result.map { it.category } shouldContainExactly listOf(
                    PostCategory.ASSIGNMENT,
                    PostCategory.NOTICE,
                    PostCategory.QUESTION,
                    PostCategory.RETROSPECTIVE
                )
            }
        }

        `when`("게시판이 없으면") {
            then("리소스 예외가 발생한다") {
                every { boardRepository.findById(boardId) } returns null

                shouldThrow<NotFoundException> {
                    useCase.getCategories(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString()
                    )
                }
            }
        }

        `when`("요청자가 그룹 멤버가 아니면") {
            then("리소스 예외가 발생한다") {
                every { boardRepository.findById(boardId) } returns createBoard()
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns null

                shouldThrow<NotFoundException> {
                    useCase.getCategories(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString()
                    )
                }
            }
        }

        `when`("요청 그룹과 게시판 소속 그룹이 다르면") {
            then("리소스 예외가 발생한다") {
                every { boardRepository.findById(boardId) } returns createBoard(ObjectId.get())
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns createGroupMember()

                shouldThrow<NotFoundException> {
                    useCase.getCategories(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString()
                    )
                }
            }
        }
    }
})
