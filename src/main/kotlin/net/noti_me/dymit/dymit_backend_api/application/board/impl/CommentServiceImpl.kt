package net.noti_me.dymit.dymit_backend_api.application.board.impl

import net.noti_me.dymit.dymit_backend_api.application.board.CommentService
import net.noti_me.dymit.dymit_backend_api.application.board.dto.CommentCommand
import net.noti_me.dymit.dymit_backend_api.application.board.dto.CommentDto
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.PostComment
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.board.event.PostCommentCreatedEvent
import net.noti_me.dymit.dymit_backend_api.domain.board.event.PostCreatedEvent
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.BoardRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.CommentRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.PostRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class CommentServiceImpl(
    private val loadGroupPort: LoadStudyGroupPort,
    private val groupMemberRepository: StudyGroupMemberRepository,
    private val boardRepository: BoardRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val eventPublisher: ApplicationEventPublisher
): CommentService {

    override fun createComment(
        memberInfo: MemberInfo,
        command: CommentCommand
    ): CommentDto {
        val board = this.boardRepository.findById(ObjectId(command.boardId))
            ?: throw NotFoundException(message="해당 게시판을 찾을 수 없습니다.")

        val groupMember = this.groupMemberRepository.findByGroupIdAndMemberId(
            ObjectId(command.groupId),
            ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message="해당 그룹의 멤버가 아닙니다.")

        val post = this.postRepository.findById(command.postId)
            ?: throw NotFoundException(message="해당 게시글을 찾을 수 없습니다.")

        if ( !board.hasPermission(groupMember, BoardAction.WRITE_COMMENT) ) {
            throw ForbiddenException(message = "해당 게시판에 댓글 작성 권한이 없습니다.")
        }

        val comment = PostComment(
            postId = ObjectId(command.postId),
            writer = Writer(
                id = groupMember.memberId,
                nickname = groupMember.nickname,
                image = ProfileImageVo(
                    url = groupMember.profileImage.url,
                    type = groupMember.profileImage.type
                )
            ),
            content = command.content
        )

        val savedComment = this.commentRepository.save(comment)
        eventPublisher.publishEvent(PostCommentCreatedEvent(
            group = loadGroupPort.loadByGroupId(command.groupId)!!,
            board = board,
            post = post,
            comment = savedComment
        ))
        post.increaseCommentCount()
        postRepository.save(post)

        return CommentDto.from(savedComment)
    }

    override fun updateComment(
        memberInfo: MemberInfo,
        commentId: String,
        command: CommentCommand
    ): CommentDto {
        val comment = this.commentRepository.findById(commentId)
            ?: throw NotFoundException(message="해당 댓글을 찾을 수 없습니다.")

        val member = this.groupMemberRepository.findByGroupIdAndMemberId(
            ObjectId(command.groupId),
            ObjectId(memberInfo.memberId)
        ) ?: throw ForbiddenException(message="해당 그룹의 멤버가 아닙니다.")

        comment.updateContent(member.memberId.toHexString(), command.content)
        val savedComment = this.commentRepository.save(comment)

        return CommentDto.from(savedComment)
    }

    override fun removeComment(
        memberInfo: MemberInfo,
        commentId: String
    ) {
        val comment = this.commentRepository.findById(commentId)
            ?: throw NotFoundException(message="해당 댓글을 찾을 수 없습니다.")

        // 댓글 작성자만 삭제할 수 있도록 권한 체크
        if (comment.writer.id.toHexString() != memberInfo.memberId) {
            throw ForbiddenException(message="본인의 댓글만 삭제할 수 있습니다.")
        }

        
        val deleteResult = this.commentRepository.delete(comment)
        if (!deleteResult) {
            throw RuntimeException("댓글 삭제에 실패했습니다.")
        }
        val post = postRepository.findById(comment.postId.toHexString())

        if ( post != null ) {
            postRepository.save(post)
        }
    }

    override fun getPostComments(
        memberInfo: MemberInfo,
        postId: String,
        lastCommentId: String?,
        size: Int
    ): List<CommentDto> {
//        val comments = this.commentRepository.findByPostId(postId)
        val comments = this.commentRepository.findByPostIdLteId(
            postId = postId,
            lastId = lastCommentId,
            size = size
        )
        return comments.map { CommentDto.from(it) }
    }
}
