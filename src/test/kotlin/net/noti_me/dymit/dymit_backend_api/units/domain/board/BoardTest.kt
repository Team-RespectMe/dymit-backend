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
import org.bson.types.ObjectId

internal class BoardTest : BehaviorSpec({

    val boardId = ObjectId()

    val groupId = ObjectId()

    // 테스트용 프로필 이미지
    val testProfileImage = MemberProfileImageVo(
        type = "presets",
        filePath = "/images/profile/test.jpg",
        url = "https://example.com/test.jpg",
        fileSize = 1024L,
        width = 200,
        height = 200
    )

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
            permissions = initialPermissions.toMutableSet()
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

            then("권한이 있는 사용자가 50자를 초과하는 이름으로 변경하려고 하면 BadRequestException이 발생한다") {
                val longName = "a".repeat(51)
                shouldThrow<BadRequestException> {
                    board.updateName(ownerMember, longName)
                }.message shouldBe "게시판 이름은 최대 50자까지 입력할 수 있습니다."
            }

            then("권한이 있는 사용자가 유효한 이름으로 변경하면 성공적으로 변경된다") {
                val newName = "수정된 게시판 이름"
                board.updateName(ownerMember, newName)

                board.name shouldBe newName
            }

            then("권한이 있는 사용자가 정확히 50자인 이름으로 변경하면 성공적으로 변경된다") {
                val maxLengthName = "a".repeat(50)
                board.updateName(ownerMember, maxLengthName)

                board.name shouldBe maxLengthName
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
            }
        }

        `when`("removePermission을 호출할 때") {
            then("존재하는 권한을 제거하면 성공적으로 제거된다") {
                val actionsToRemove = listOf(BoardAction.WRITE_POST)
                board.removePermission(
                    ownerMember,
                    GroupMemberRole.OWNER,
                    actionsToRemove
                )

                val ownerPermission = board.permissions.find { it.role == GroupMemberRole.OWNER }
                ownerPermission shouldNotBe null
                ownerPermission!!.actions shouldNotContain BoardAction.WRITE_POST
                ownerPermission.actions shouldContain BoardAction.MANAGE_BOARD
            }

            then("존재하지 않는 역할에서 권한을 제거해도 오류가 발생하지 않는다") {
                val actionsToRemove = listOf(BoardAction.READ_POST)
                val originalSize = board.permissions.size

                // 예외가 발생하지 않아야 함
                board.removePermission(
                    ownerMember,
                    GroupMemberRole.MEMBER,
                    actionsToRemove)

                board.permissions.size shouldBe originalSize
            }

            then("존재하지 않는 권한을 제거해도 오류가 발생하지 않는다") {
                val actionsToRemove = listOf(BoardAction.READ_COMMENT)
                val ownerPermission = board.permissions.find { it.role == GroupMemberRole.OWNER }
                val originalSize = ownerPermission?.actions?.size ?: 0

                // 예외가 발생하지 않아야 함
                board.removePermission(ownerMember,
                    GroupMemberRole.OWNER,
                    actionsToRemove)

                val updatedPermission = board.permissions.find { it.role == GroupMemberRole.OWNER }
                updatedPermission!!.actions.size shouldBe originalSize
            }
        }
    }

    afterEach {

    }
})
