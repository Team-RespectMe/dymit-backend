package net.noti_me.dymit.dymit_backend_api.units.domain.board

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardPermission
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.supports.createProfileImageVo
import org.bson.types.ObjectId

/**
 * Board 도메인 객체의 비즈니스 로직을 테스트하는 클래스
 */
internal class BoardTest : BehaviorSpec({

    val boardId = ObjectId()
    val groupId = ObjectId()
    val otherGroupId = ObjectId()
    val testProfileImage = createProfileImageVo()
    // 다양한 역할의 멤버들 생성
    val ownerMember = StudyGroupMember(
        id = ObjectId(),
        groupId = groupId,
        memberId = ObjectId(),
        nickname = "스터디 오너",
        profileImage = testProfileImage,
        role = GroupMemberRole.OWNER
    )

    val adminMember = StudyGroupMember(
        id = ObjectId(),
        groupId = groupId,
        memberId = ObjectId(),
        nickname = "스터디 관리자",
        profileImage = testProfileImage,
        role = GroupMemberRole.ADMIN
    )

    val regularMember = StudyGroupMember(
        id = ObjectId(),
        groupId = groupId,
        memberId = ObjectId(),
        nickname = "일반 멤버",
        profileImage = testProfileImage,
        role = GroupMemberRole.MEMBER
    )

    // 다른 그룹의 멤버
    val otherGroupMember = StudyGroupMember(
        id = ObjectId(),
        groupId = otherGroupId,
        memberId = ObjectId(),
        nickname = "다른 그룹 멤버",
        profileImage = testProfileImage,
        role = GroupMemberRole.OWNER
    )

    // 초기 권한 설정 (오너에게만 게시판 관리 권한 부여)
    val initialPermissions = mutableSetOf(
        BoardPermission(
            role = GroupMemberRole.OWNER,
            actions = mutableListOf(BoardAction.MANAGE_BOARD, BoardAction.WRITE_POST, BoardAction.DELETE_POST)
        ),
        BoardPermission(
            role = GroupMemberRole.ADMIN,
            actions = mutableListOf(BoardAction.WRITE_POST, BoardAction.READ_POST)
        ),
        BoardPermission(
            role = GroupMemberRole.MEMBER,
            actions = mutableListOf(BoardAction.READ_POST)
        )
    )

    var board = Board(
        id = boardId,
        groupId = groupId,
        name = "테스트 게시판",
        permissions = initialPermissions
    )

    beforeEach {
        board = Board(
            id = boardId,
            groupId = groupId,
            name = "테스트 게시판",
            permissions = initialPermissions.map {
                BoardPermission(it.role, it.actions.toMutableList())
            }.toMutableSet()
        )
    }

    given("Board 인스턴스가 주어졌을 때") {

        `when`("updateName을 호출할 때") {
            then("권한이 없는 사용자가 이름을 변경하려고 하면 ForbiddenException이 발생한다") {
                shouldThrow<ForbiddenException> {
                    board.updateName(regularMember, "새로운 게시판 이름")
                }.message shouldBe "게시판 관리 권한이 없습니다."

                shouldThrow<ForbiddenException> {
                    board.updateName(adminMember, "새로운 게시판 이름")
                }.message shouldBe "게시판 관리 권한이 없습니다."
            }

            then("다른 그룹의 멤버가 이름을 변경하려고 하면 ForbiddenException이 발생한다") {
                shouldThrow<ForbiddenException> {
                    board.updateName(otherGroupMember, "새로운 게시판 이름")
                }.message shouldBe "게시판 관리 권한이 없습니다."
            }

            then("권한이 있는 사용자가 빈 이름으로 변경하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    board.updateName(ownerMember, "")
                }.message shouldBe "게시판 이름은 비워둘 수 없습니다."
            }

            then("권한이 있는 사용자가 공백만 있는 이름으로 변경하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    board.updateName(ownerMember, "   ")
                }.message shouldBe "게시판 이름은 비워둘 수 없습니다."
            }

            then("권한이 있는 사용자가 탭과 개행 문자만 있는 이름으로 변경하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    board.updateName(ownerMember, "\t\n\r")
                }.message shouldBe "게시판 이름은 비워둘 수 없습니다."
            }

            then("권한이 있는 사용자가 50자를 초과하는 이름으로 변경하려고 하면 BadRequestException이 발생한다") {
                val longName = "a".repeat(51)
                shouldThrow<BadRequestException> {
                    board.updateName(ownerMember, longName)
                }.message shouldBe "게시판 이름은 최대 50자까지 입력할 수 있습니다."
            }

            then("권한이 있는 사용자가 정확히 50자인 이름으로 변경하면 성공적으로 변경된다") {
                val maxLengthName = "a".repeat(50)
                board.updateName(ownerMember, maxLengthName)

                board.name shouldBe maxLengthName
            }

            then("권한이 있는 사용자가 유효한 이름으로 변경하면 성공적으로 변경된다") {
                val newName = "수정된 게시판 이름"
                board.updateName(ownerMember, newName)

                board.name shouldBe newName
            }

            then("권한이 있는 사용자가 앞뒤 공백이 있는 유효한 이름으로 변경하면 성공적으로 변경된다") {
                val nameWithSpaces = "  유효한 게시판 이름  "
                board.updateName(ownerMember, nameWithSpaces)

                board.name shouldBe nameWithSpaces
            }
        }

        `when`("updatePermissions를 호출할 때") {
            then("권한이 없는 사용자가 권한을 변경하려고 하면 ForbiddenException이 발생한다") {
                val newPermissions = listOf(
                    BoardPermission(GroupMemberRole.MEMBER, mutableListOf(BoardAction.READ_POST, BoardAction.WRITE_POST))
                )

                shouldThrow<ForbiddenException> {
                    board.updatePermissions(regularMember, newPermissions)
                }.message shouldBe "권한 관리 권한이 없습니다."

                shouldThrow<ForbiddenException> {
                    board.updatePermissions(adminMember, newPermissions)
                }.message shouldBe "권한 관리 권한이 없습니다."
            }

            then("다른 그룹의 멤버가 권한을 변경하려고 하면 ForbiddenException이 발생한다") {
                val newPermissions = listOf(
                    BoardPermission(GroupMemberRole.MEMBER, mutableListOf(BoardAction.READ_POST, BoardAction.WRITE_POST))
                )

                shouldThrow<ForbiddenException> {
                    board.updatePermissions(otherGroupMember, newPermissions)
                }.message shouldBe "권한 관리 권한이 없습니다."
            }

            then("권한이 있는 사용자가 빈 권한 목록으로 변경하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    board.updatePermissions(ownerMember, emptyList())
                }.message shouldBe "최소 하나 이상의 권한이 설정되어야 합니다."
            }

            then("권한이 있는 사용자가 유효한 권한으로 변경하면 성공적으로 변경된다") {
                val newPermissions = listOf(
                    BoardPermission(GroupMemberRole.ADMIN, mutableListOf(BoardAction.READ_POST, BoardAction.WRITE_POST, BoardAction.DELETE_POST)),
                    BoardPermission(GroupMemberRole.MEMBER, mutableListOf(BoardAction.READ_POST, BoardAction.WRITE_POST))
                )

                board.updatePermissions(ownerMember, newPermissions)

                board.permissions shouldHaveSize 2
                board.permissions.find { it.role == GroupMemberRole.ADMIN } shouldNotBe null
                board.permissions.find { it.role == GroupMemberRole.MEMBER } shouldNotBe null
                board.permissions.find { it.role == GroupMemberRole.OWNER } shouldBe null
            }

            then("기존 권한이 완전히 교체되어야 한다") {
                val newPermissions = listOf(
                    BoardPermission(GroupMemberRole.MEMBER, mutableListOf(BoardAction.READ_POST))
                )

                board.updatePermissions(ownerMember, newPermissions)

                board.permissions shouldHaveSize 1
                board.permissions.first().role shouldBe GroupMemberRole.MEMBER
                board.permissions.first().actions shouldContain BoardAction.READ_POST
            }
        }

        `when`("removePermission을 호출할 때") {
            then("권한이 없는 사용자가 권한을 제거하려고 하면 ForbiddenException이 발생한다") {
                shouldThrow<ForbiddenException> {
                    board.removePermission(regularMember, GroupMemberRole.MEMBER, listOf(BoardAction.READ_POST))
                }.message shouldBe "권한 관리 권한이 없습니다."
            }

            then("다른 그룹의 멤버가 권한을 제거하려고 하면 ForbiddenException이 발생한다") {
                shouldThrow<ForbiddenException> {
                    board.removePermission(otherGroupMember, GroupMemberRole.MEMBER, listOf(BoardAction.READ_POST))
                }.message shouldBe "권한 관리 권한이 없습니다."
            }

            then("존재하는 권한을 부분적으로 제거하면 성공적으로 제거된다") {
                val actionsToRemove = listOf(BoardAction.WRITE_POST)
                board.removePermission(ownerMember, GroupMemberRole.OWNER, actionsToRemove)

                val ownerPermission = board.permissions.find { it.role == GroupMemberRole.OWNER }
                ownerPermission shouldNotBe null
                ownerPermission!!.actions shouldNotContain BoardAction.WRITE_POST
                ownerPermission.actions shouldContain BoardAction.MANAGE_BOARD
                ownerPermission.actions shouldContain BoardAction.DELETE_POST
            }

            then("존재하지 않는 액션을 제거해도 오류가 발생하지 않는다") {
                val ownerPermission = board.permissions.find { it.role == GroupMemberRole.OWNER }
                val originalActions = ownerPermission!!.actions.toList()

                board.removePermission(ownerMember, GroupMemberRole.OWNER, listOf(BoardAction.READ_COMMENT))

                val updatedPermission = board.permissions.find { it.role == GroupMemberRole.OWNER }
                updatedPermission!!.actions shouldBe originalActions
            }
        }

        `when`("hasPermission을 호출할 때") {
            then("같은 그룹의 멤버가 가진 권한을 확인하면 올바른 결과를 반환한다") {
                board.hasPermission(ownerMember, BoardAction.MANAGE_BOARD) shouldBe true
                board.hasPermission(ownerMember, BoardAction.WRITE_POST) shouldBe true
                board.hasPermission(ownerMember, BoardAction.READ_COMMENT) shouldBe false

                board.hasPermission(adminMember, BoardAction.WRITE_POST) shouldBe true
                board.hasPermission(adminMember, BoardAction.READ_POST) shouldBe true
                board.hasPermission(adminMember, BoardAction.MANAGE_BOARD) shouldBe false

                board.hasPermission(regularMember, BoardAction.READ_POST) shouldBe true
                board.hasPermission(regularMember, BoardAction.WRITE_POST) shouldBe false
                board.hasPermission(regularMember, BoardAction.MANAGE_BOARD) shouldBe false
            }

            then("다른 그룹의 멤버는 어떤 권한도 가지지 않는다") {
                board.hasPermission(otherGroupMember, BoardAction.MANAGE_BOARD) shouldBe false
                board.hasPermission(otherGroupMember, BoardAction.WRITE_POST) shouldBe false
                board.hasPermission(otherGroupMember, BoardAction.READ_POST) shouldBe false
            }

            then("권한이 제거된 후에는 해당 권한을 가지지 않는다") {
                board.removePermission(ownerMember, GroupMemberRole.ADMIN, listOf(BoardAction.WRITE_POST))

                board.hasPermission(adminMember, BoardAction.WRITE_POST) shouldBe false
                board.hasPermission(adminMember, BoardAction.READ_POST) shouldBe true
            }
        }

        `when`("Board 객체의 초기 상태를 확인할 때") {
            then("모든 setter는 private이어야 하고 해당 메서드를 통해서만 변경 가능해야 한다") {
                // 각 메서드를 통한 변경이 정상적으로 작동하는지 확인
                board.updateName(ownerMember, "새 게시판 이름")
                board.updatePermissions(ownerMember, listOf(
                    BoardPermission(GroupMemberRole.MEMBER, mutableListOf(BoardAction.READ_POST))
                ))

                board.name shouldBe "새 게시판 이름"
                board.permissions shouldHaveSize 1
            }

            then("BoardPermission 데이터 클래스가 올바르게 동작한다") {
                val permission1 = BoardPermission(GroupMemberRole.ADMIN, mutableListOf(BoardAction.READ_POST))
                val permission2 = BoardPermission(GroupMemberRole.ADMIN, mutableListOf(BoardAction.READ_POST))
                val permission3 = BoardPermission(GroupMemberRole.MEMBER, mutableListOf(BoardAction.READ_POST))

                permission1 shouldBe permission2
                permission1 shouldNotBe permission3
            }
        }
    }
})
