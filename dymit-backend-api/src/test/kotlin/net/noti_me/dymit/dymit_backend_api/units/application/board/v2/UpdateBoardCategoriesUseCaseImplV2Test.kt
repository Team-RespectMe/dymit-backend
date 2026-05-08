package net.noti_me.dymit.dymit_backend_api.units.application.board.v2

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.UpdateBoardCategoryPoliciesCommandV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.impl.UpdateBoardCategoriesUseCaseImplV2
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardCategoryPolicy
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardCategoryWritePolicy
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardPermission
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.BoardRepositoryV2
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.supports.createProfileImageVo
import org.bson.types.ObjectId

internal class UpdateBoardCategoriesUseCaseImplV2Test : BehaviorSpec({

    val boardRepository = mockk<BoardRepositoryV2>()
    val studyGroupMemberRepository = mockk<StudyGroupMemberRepository>()

    val useCase = UpdateBoardCategoriesUseCaseImplV2(
        boardRepository = boardRepository,
        groupMemberRepository = studyGroupMemberRepository
    )

    val groupId = ObjectId.get()
    val boardId = ObjectId.get()
    val memberId = ObjectId.get()

    fun createGroupMember(role: GroupMemberRole): StudyGroupMember {
        return StudyGroupMember(
            id = ObjectId.get(),
            groupId = groupId,
            memberId = memberId,
            nickname = "tester",
            profileImage = createProfileImageVo(),
            role = role
        )
    }

    fun createBoard(): Board {
        return Board(
            id = boardId,
            groupId = groupId,
            name = "게시판",
            permissions = mutableSetOf(
                BoardPermission(
                    role = GroupMemberRole.OWNER,
                    actions = mutableListOf(BoardAction.MANAGE_BOARD, BoardAction.READ_POST, BoardAction.WRITE_POST)
                ),
                BoardPermission(
                    role = GroupMemberRole.ADMIN,
                    actions = mutableListOf(BoardAction.MANAGE_BOARD, BoardAction.READ_POST, BoardAction.WRITE_POST)
                ),
                BoardPermission(
                    role = GroupMemberRole.MEMBER,
                    actions = mutableListOf(BoardAction.READ_POST, BoardAction.WRITE_POST)
                )
            )
        )
    }

    val updateCommand = UpdateBoardCategoryPoliciesCommandV2(
        policies = listOf(
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
    )

    fun memberInfo(): MemberInfo {
        return MemberInfo(
            memberId = memberId.toHexString(),
            nickname = "tester",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
    }

    beforeEach {
        clearAllMocks()
    }

    given("게시판 카테고리 정책 V2 수정 시") {
        `when`("요청자가 게시판 관리 권한이 있으면") {
            then("정상적으로 카테고리 정책을 변경한다") {
                val ownerMember = createGroupMember(GroupMemberRole.OWNER)
                val board = createBoard()
                every { boardRepository.findById(boardId) } returns board
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns ownerMember
                every { boardRepository.save(any()) } answers { firstArg() }

                val result = useCase.updateCategories(
                    memberInfo = memberInfo(),
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    command = updateCommand
                )

                result.size shouldBe 4
                result.first { it.category == PostCategory.NOTICE }.writePolicy shouldBe
                    BoardCategoryWritePolicy.GROUP_ADMIN_ONLY
            }
        }

        `when`("요청자가 게시판 관리 권한이 없으면") {
            then("권한 예외가 발생한다") {
                val regularMember = createGroupMember(GroupMemberRole.MEMBER)
                val board = createBoard()
                every { boardRepository.findById(boardId) } returns board
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns regularMember

                shouldThrow<ForbiddenException> {
                    useCase.updateCategories(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString(),
                        command = updateCommand
                    )
                }
            }
        }

        `when`("게시판이 존재하지 않으면") {
            then("리소스 예외가 발생한다") {
                every { boardRepository.findById(boardId) } returns null

                shouldThrow<NotFoundException> {
                    useCase.updateCategories(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString(),
                        command = updateCommand
                    )
                }
            }
        }

        `when`("요청 사용자가 그룹 멤버가 아니면") {
            then("리소스 예외가 발생한다") {
                every { boardRepository.findById(boardId) } returns createBoard()
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns null

                shouldThrow<NotFoundException> {
                    useCase.updateCategories(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString(),
                        command = updateCommand
                    )
                }
            }
        }

        `when`("요청 그룹과 게시판 소속 그룹이 다르면") {
            then("리소스 예외가 발생한다") {
                val ownerMember = createGroupMember(GroupMemberRole.OWNER)
                val boardInOtherGroup = Board(
                    id = boardId,
                    groupId = ObjectId.get(),
                    name = "다른 그룹 게시판"
                )
                every { boardRepository.findById(boardId) } returns boardInOtherGroup
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns ownerMember

                shouldThrow<NotFoundException> {
                    useCase.updateCategories(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString(),
                        command = updateCommand
                    )
                }
            }
        }
    }
})
