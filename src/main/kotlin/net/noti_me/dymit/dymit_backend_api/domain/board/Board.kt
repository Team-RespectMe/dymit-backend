package net.noti_me.dymit.dymit_backend_api.domain.board

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("study_group_boards")
class Board(
    @Id
    val id: ObjectId,
    val groupId: ObjectId,
    name: String,
    permissions: MutableSet<BoardPermission> = mutableSetOf(
        BoardPermission(
            role = GroupMemberRole.OWNER,
            actions = mutableListOf()
        ),
        BoardPermission(
            role = GroupMemberRole.ADMIN,
            actions = mutableListOf()
        ),
        BoardPermission(
            role = GroupMemberRole.MEMBER,
            actions = mutableListOf()
        )
    )
) : BaseAggregateRoot<Board>() {

    var name: String = name
        private set

    var permissions: MutableSet<BoardPermission> = permissions
        private set

    fun updateName(requester: StudyGroupMember, newName: String) {
        if (!hasPermission(requester, BoardAction.MANAGE_BOARD)) {
            throw ForbiddenException(message = "게시판 관리 권한이 없습니다.")
        }

        if (newName.isBlank()) {
            throw BadRequestException(message = "게시판 이름은 비워둘 수 없습니다.")
        }

        if (newName.length > 50) {
            throw BadRequestException(message = "게시판 이름은 최대 50자까지 입력할 수 있습니다.")
        }

        this.name = newName
    }

    fun updatePermissions(requester: StudyGroupMember, newPermissions: List<BoardPermission>) {
        if (!hasPermission(requester, BoardAction.MANAGE_BOARD)) {
            throw ForbiddenException(message = "권한 관리 권한이 없습니다.")
        }

        if (newPermissions.isEmpty()) {
            throw BadRequestException(message = "최소 하나 이상의 권한이 설정되어야 합니다.")
        }

        this.permissions.clear()
        this.permissions.addAll(newPermissions)
    }

    fun hasPermission(member: StudyGroupMember, action: BoardAction): Boolean {
//        for (permission in permissions) {
//            println("Permission Role: ${permission.role}, Actions: ${permission.actions}")
//        }
        if ( groupId != member.groupId ) {
            return false
        }

        return permissions.any { permission ->
            permission.role == member.role && permission.actions.contains(action)
        }
    }

    fun removePermission(requester: StudyGroupMember,
                         groupMemberRole: GroupMemberRole,
                         actions: List<BoardAction>) {
        if (!hasPermission(requester, BoardAction.MANAGE_BOARD)) {
            throw ForbiddenException(message = "권한 관리 권한이 없습니다.")
        }

        val permission = permissions.find { it.role == groupMemberRole }!!
        permission.actions.removeAll(actions)
    }
}

data class BoardPermission(
    val role: GroupMemberRole,
    val actions: MutableList<BoardAction>
)

enum class BoardAction(val description: String) {
    READ_POST("게시글 읽기"),
    WRITE_POST("게시글 작성"),
    DELETE_POST("게시글 삭제"),
    READ_COMMENT("댓글 읽기"),
    WRITE_COMMENT("댓글 작성"),
    DELETE_COMMENT("댓글 삭제"),
    MANAGE_BOARD("게시판 관리"),
}
