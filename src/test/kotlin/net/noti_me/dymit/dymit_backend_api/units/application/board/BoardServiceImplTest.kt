package net.noti_me.dymit.dymit_backend_api.units.application.board

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.board.dto.BoardCommand
import net.noti_me.dymit.dymit_backend_api.application.board.impl.BoardServiceImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardPermission
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.BoardRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId

/**
 * BoardServiceImpl에 대한 테스트 클래스
 * 게시판 생성, 수정, 삭제, 조회 기능을 테스트한다.
 */
class BoardServiceImplTest : BehaviorSpec({

    // Mock 객체 선언
    val boardRepository = mockk<BoardRepository>()
    val studyGroupMemberRepository = mockk<StudyGroupMemberRepository>()
    val boardService = BoardServiceImpl(boardRepository, studyGroupMemberRepository)

    // 테스트용 데이터
    lateinit var memberInfo: MemberInfo
    lateinit var groupObjectId: ObjectId
    lateinit var memberObjectId: ObjectId
    lateinit var boardObjectId: ObjectId
    lateinit var ownerMember: StudyGroupMember
    lateinit var regularMember: StudyGroupMember
    lateinit var board: Board
    lateinit var boardCommand: BoardCommand
    lateinit var profileImage: MemberProfileImageVo

    beforeContainer {
        // 공통 테스트 데이터 초기화
        groupObjectId = ObjectId()
        memberObjectId = ObjectId()
        boardObjectId = ObjectId()

        memberInfo = MemberInfo(
            memberId = memberObjectId.toHexString(),
            nickname = "testUser",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )

        profileImage = MemberProfileImageVo(
            type = "presets",
            filePath = "/images/profile/default.jpg",
            url = "https://example.com/default.jpg",
            fileSize = 1024L,
            width = 200,
            height = 200
        )

        // 다양한 권한을 가진 멤버들 생성
        ownerMember = StudyGroupMember(
            id = memberObjectId,
            groupId = groupObjectId,
            memberId = memberObjectId,
            nickname = "ownerUser",
            profileImage = profileImage,
            role = GroupMemberRole.OWNER
        )

        regularMember = StudyGroupMember(
            id = ObjectId(),
            groupId = groupObjectId,
            memberId = ObjectId(),
            nickname = "regularUser",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )

        val permissions = mutableSetOf(
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

        board = Board(
            id = boardObjectId,
            groupId = groupObjectId,
            name = "testBoard",
            permissions = permissions
        )

        boardCommand = BoardCommand(
            name = "newBoard",
            permissions = permissions.toList()
        )
    }

    Given("게시판을 생성할 때") {
        When("정상적인 요청으로 게시판을 생성하면") {
            Then("게시판이 성공적으로 생성되어야 한다") {
                // Given
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns ownerMember
                every { boardRepository.save(any<Board>()) } returns board

                // When
                val result = boardService.createBoard(memberInfo, groupObjectId.toHexString(), boardCommand)

                // Then
                result shouldNotBe null
                result.name shouldBe "testBoard"
                result.groupId shouldBe groupObjectId.toHexString()
                verify { boardRepository.save(any<Board>()) }
            }
        }

        When("해당 그룹의 멤버가 아닌 사용자가 게시판을 생성하려고 하면") {
            Then("ForbiddenException이 발생해야 한다") {
                // Given
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns null

                // When & Then
                val exception = shouldThrow<ForbiddenException> {
                    boardService.createBoard(memberInfo, groupObjectId.toHexString(), boardCommand)
                }
                exception.message shouldBe "권한이 없어 요청을 처리할 수 없습니다."
            }
        }

        When("게시판 저장에 실패하면") {
            Then("RuntimeException이 발생해야 한다") {
                // Given
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns ownerMember
                every { boardRepository.save(any<Board>()) } returns null

                // When & Then
                val exception = shouldThrow<RuntimeException> {
                    boardService.createBoard(memberInfo, groupObjectId.toHexString(), boardCommand)
                }
                exception.message shouldBe "게시판 생성에 실패했습니다."
            }
        }
    }

    Given("게시판을 수정할 때") {
        When("권한이 있는 사용자가 게시판을 수정하면") {
            Then("게시판이 성공적으로 수정되어야 한다") {
                // Given
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns ownerMember
                every { boardRepository.findById(boardObjectId) } returns board
                every { boardRepository.save(any<Board>()) } returns board

                // When
                val result = boardService.updateBoard(
                    memberInfo,
                    groupObjectId.toHexString(),
                    boardObjectId.toHexString(),
                    boardCommand
                )

                // Then
                result shouldNotBe null
                verify { boardRepository.save(any<Board>()) }
            }
        }

        When("해당 그룹의 멤버가 아닌 사용자가 게시판을 수정하려고 하면") {
            Then("ForbiddenException이 발생해야 한다") {
                // Given
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns null

                // When & Then
                val exception = shouldThrow<ForbiddenException> {
                    boardService.updateBoard(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString(),
                        boardCommand
                    )
                }
                exception.message shouldBe "권한이 없어 요청을 처리할 수 없습니다."
            }
        }

        When("존재하지 않는 게시판을 수정하려고 하면") {
            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns ownerMember
                every { boardRepository.findById(boardObjectId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    boardService.updateBoard(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString(),
                        boardCommand
                    )
                }
                exception.message shouldBe "요청한 리소스를 찾을 수 없습니다."
            }
        }

        When("다른 그룹의 게시판을 수정하려고 하면") {
            Then("ForbiddenException이 발생해야 한다") {
                // Given
                val otherGroupBoard = Board(
                    id = boardObjectId,
                    groupId = ObjectId(),
                    name = "otherBoard",
                    permissions = mutableSetOf()
                )
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns ownerMember
                every { boardRepository.findById(boardObjectId) } returns otherGroupBoard

                // When & Then
                val exception = shouldThrow<ForbiddenException> {
                    boardService.updateBoard(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString(),
                        boardCommand
                    )
                }
                exception.message shouldBe "권한이 없어 요청을 처리할 수 없습니다."
            }
        }

        When("권한이 없는 사용자가 게시판을 수정하려고 하면") {
            Then("ForbiddenException이 발생해야 한다") {
                // Given
                val memberWithoutPermission = StudyGroupMember(
                    id = ObjectId(),
                    groupId = groupObjectId,
                    memberId = ObjectId(),
                    nickname = "regularUser",
                    profileImage = profileImage,
                    role = GroupMemberRole.MEMBER
                )
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns memberWithoutPermission
                every { boardRepository.findById(boardObjectId) } returns board

                // When & Then
                val exception = shouldThrow<ForbiddenException> {
                    boardService.updateBoard(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString(),
                        boardCommand
                    )
                }
                exception.message shouldBe "게시판 관리 권한이 없습니다."
            }
        }

        When("빈 이름으로 게시판을 수정하려고 하면") {
            Then("BadRequestException이 발생해야 한다") {
                // Given
                val emptyNameCommand = BoardCommand(
                    name = "",
                    permissions = boardCommand.permissions
                )
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns ownerMember
                every { boardRepository.findById(boardObjectId) } returns board

                // When & Then
                val exception = shouldThrow<BadRequestException> {
                    boardService.updateBoard(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString(),
                        emptyNameCommand
                    )
                }
                exception.message shouldBe "게시판 이름은 비워둘 수 없습니다."
            }
        }

        When("50자를 초과하는 이름으로 게시판을 수정하려고 하면") {
            Then("BadRequestException이 발생해야 한다") {
                // Given
                val longNameCommand = BoardCommand(
                    name = "a".repeat(51),
                    permissions = boardCommand.permissions
                )
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns ownerMember
                every { boardRepository.findById(boardObjectId) } returns board

                // When & Then
                val exception = shouldThrow<BadRequestException> {
                    boardService.updateBoard(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString(),
                        longNameCommand
                    )
                }
                exception.message shouldBe "게시판 이름은 최대 50자까지 입력할 수 있습니다."
            }
        }

        When("빈 권한 목록으로 게시판을 수정하려고 하면") {
            Then("BadRequestException이 발생해야 한다") {
                // Given
                val emptyPermissionsCommand = BoardCommand(
                    name = "validName",
                    permissions = emptyList()
                )
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns ownerMember
                every { boardRepository.findById(boardObjectId) } returns board

                // When & Then
                val exception = shouldThrow<BadRequestException> {
                    boardService.updateBoard(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString(),
                        emptyPermissionsCommand
                    )
                }
                exception.message shouldBe "최소 하나 이상의 권한이 설정되어야 합니다."
            }
        }

        When("게시판 저장에 실패하면") {
            Then("RuntimeException이 발생해야 한다") {
                // Given
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns ownerMember
                every { boardRepository.findById(boardObjectId) } returns board
                every { boardRepository.save(any<Board>()) } returns null

                // When & Then
                val exception = shouldThrow<RuntimeException> {
                    boardService.updateBoard(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString(),
                        boardCommand
                    )
                }
                exception.message shouldBe "게시판 업데이트에 실패했습니다."
            }
        }
    }

    Given("게시판을 삭제할 때") {
        When("권한이 있는 사용자가 게시판을 삭제하면") {
            Then("게시판이 성공적으로 삭제되어야 한다") {
                // Given
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns ownerMember
                every { boardRepository.findById(boardObjectId) } returns board
                every { boardRepository.delete(board) } returns true

                // When
                boardService.removeBoard(
                    memberInfo,
                    groupObjectId.toHexString(),
                    boardObjectId.toHexString()
                )

                // Then
                verify { boardRepository.delete(board) }
            }
        }

        When("해당 그룹의 멤버가 아닌 사용자가 게시판을 삭제하려고 하면") {
            Then("ForbiddenException이 발생해야 한다") {
                // Given
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns null

                // When & Then
                val exception = shouldThrow<ForbiddenException> {
                    boardService.removeBoard(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString()
                    )
                }
                exception.message shouldBe "권한이 없어 요청을 처리할 수 없습니다."
            }
        }

        When("존재하지 않는 게시판을 삭제하려고 하면") {
            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns ownerMember
                every { boardRepository.findById(boardObjectId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    boardService.removeBoard(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString()
                    )
                }
                exception.message shouldBe "요청한 리소스를 찾을 수 없습니다."
            }
        }

        When("다른 그룹의 게시판을 삭제하려고 하면") {
            Then("ForbiddenException이 발생해야 한다") {
                // Given
                val otherGroupBoard = Board(
                    id = boardObjectId,
                    groupId = ObjectId(),
                    name = "otherBoard",
                    permissions = mutableSetOf()
                )
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns ownerMember
                every { boardRepository.findById(boardObjectId) } returns otherGroupBoard

                // When & Then
                val exception = shouldThrow<ForbiddenException> {
                    boardService.removeBoard(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString()
                    )
                }
                exception.message shouldBe "권한이 없어 요청을 처리할 수 없습니다."
            }
        }

        When("게시판 삭제에 실패하면") {
            Then("RuntimeException이 발생해야 한다") {
                // Given
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns ownerMember
                every { boardRepository.findById(boardObjectId) } returns board
                every { boardRepository.delete(board) } returns false

                // When & Then
                val exception = shouldThrow<RuntimeException> {
                    boardService.removeBoard(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString()
                    )
                }
                exception.message shouldBe "게시판 삭제에 실패했습니다."
            }
        }
    }

    Given("그룹의 게시판 목록을 조회할 때") {
        When("해당 그룹에 게시판이 존재하면") {
            Then("게시판 목록이 반환되어야 한다") {
                // Given
                val boardList = listOf(board)
                every { boardRepository.findByGroupId(groupObjectId) } returns boardList

                // When
                val result = boardService.getGroupBoards(groupObjectId.toHexString())

                // Then
                result shouldNotBe null
                result.size shouldBe 1
                result[0].name shouldBe "testBoard"
                result[0].groupId shouldBe groupObjectId.toHexString()
            }
        }

        When("해당 그룹에 게시판이 존재하지 않으면") {
            Then("빈 목록이 반환되어야 한다") {
                // Given
                every { boardRepository.findByGroupId(groupObjectId) } returns emptyList()

                // When
                val result = boardService.getGroupBoards(groupObjectId.toHexString())

                // Then
                result shouldNotBe null
                result.size shouldBe 0
            }
        }
    }
})
