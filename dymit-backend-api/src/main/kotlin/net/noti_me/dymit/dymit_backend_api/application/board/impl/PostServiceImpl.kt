package net.noti_me.dymit.dymit_backend_api.application.board.impl

import net.noti_me.dymit.dymit_backend_api.application.board.PostService
import net.noti_me.dymit.dymit_backend_api.application.board.dto.PostCommand
import net.noti_me.dymit.dymit_backend_api.application.board.dto.PostDto
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.board.event.PostCreatedEvent
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupRecentPostDto as RecentPostVo
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.BoardRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.PostRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupQueryPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupCommandPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberPort
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val loadGroupPort: StudyGroupQueryPort,
    private val saveGroupPort: StudyGroupCommandPort,
    private val boardRepository: BoardRepository,
    private val groupMemberRepository: StudyGroupMemberPort,
    private val eventPublisher: ApplicationEventPublisher
): PostService {

    override fun createPost(
        memberInfo: MemberInfo,
        command: PostCommand
    ): PostDto {
        val board = this.boardRepository.findById(ObjectId(command.boardId))
            ?: throw NotFoundException(message="해당 게시판을 찾을 수 없습니다.")

        val group = this.loadGroupPort.loadByGroupId(command.groupId)
            ?: throw NotFoundException(message="해당 그룹을 찾을 수 없습니다.")

        val groupMember = this.groupMemberRepository.findByGroupIdAndMemberId(
            ObjectId(command.groupId),
            ObjectId(memberInfo.memberId)
        ) ?: throw NotFoundException(message="해당 그룹의 멤버가 아닙니다.")

        if (!board.hasPermission(groupMember, BoardAction.WRITE_POST)) {
            throw NotFoundException(message = "해당 게시판에 글 작성 권한이 없습니다.")
        }

        val newPost = Post(
            groupId = ObjectId(command.groupId),
            boardId = board.id!!,
            writer = Writer.from(groupMember),
            title = command.title,
            content = command.content
        )

        val savedPost = this.postRepository.save(newPost)
        group.updateRecentPost(
            RecentPostVo(
                postId = savedPost.identifier,
                title = savedPost.title,
                createdAt = savedPost.createdAt ?: java.time.LocalDateTime.now()
            )
        )
        saveGroupPort.update(group)
        val event = PostCreatedEvent(
            group = group,
            board = board,
            post = savedPost
        )
        event.addExcludedMemberId(groupMember.memberId)
        eventPublisher.publishEvent(event)

        return PostDto.from(savedPost)
    }

    override fun updatePost(
        memberInfo: MemberInfo,
        postId: String,
        command: PostCommand
    ): PostDto {
        val board = this.boardRepository.findById(ObjectId(command.boardId))
            ?: throw NotFoundException(message="해당 게시판을 찾을 수 없습니다.")

        val post = this.postRepository.findById(postId)
            ?: throw NotFoundException(message="해당 게시글을 찾을 수 없습니다.")

        val group = this.loadGroupPort.loadByGroupId(command.groupId)
            ?: throw NotFoundException(message="해당 그룹을 찾을 수 없습니다.")

        val groupMember = this.groupMemberRepository.findByGroupIdAndMemberId(
            ObjectId(command.groupId),
            ObjectId(memberInfo.memberId)
        ) ?: throw NotFoundException(message="해당 그룹의 멤버가 아닙니다.")

        post.updateTitle(memberInfo.memberId, command.title)
        post.updateContent(memberInfo.memberId, command.content)
        val updatedPost = this.postRepository.save(post)

        if (group.recentPost?.postId == updatedPost.identifier) {
            group.updateRecentPost(
                RecentPostVo(
                    postId = updatedPost.identifier,
                    title = updatedPost.title,
                    createdAt = updatedPost.createdAt ?: java.time.LocalDateTime.now()
                )
            )
            saveGroupPort.update(group)
        }

        return PostDto.from(updatedPost)
    }

    override fun removePost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String
    ) {
        val group = this.loadGroupPort.loadByGroupId(groupId)
            ?: throw NotFoundException(message="해당 그룹을 찾을 수 없습니다.")

        val post = this.postRepository.findById(postId)
            ?: throw NotFoundException(message="해당 게시글을 찾을 수 없습니다.")

        val groupMember = this.groupMemberRepository.findByGroupIdAndMemberId(
            post.groupId,
            ObjectId(memberInfo.memberId)
        ) ?: throw NotFoundException(message="해당 그룹의 멤버가 아닙니다.")

        this.postRepository.deleteById(post.identifier)

        val recentPost = postRepository.findLastPostByGroupIdAndBoardId(
            groupId = ObjectId(groupId),
            boardId = ObjectId(boardId)
        )

        group.updateRecentPost(
            recentPost?.let {
                RecentPostVo(
                    postId = it.identifier,
                    title = it.title,
                    createdAt = it.createdAt ?: java.time.LocalDateTime.now()
                )
            }
        )
        saveGroupPort.update(group)
    }

    override fun getBoardPostsWithCursor(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        cursor: String?,
        size: Int
    ): List<PostDto> {
        val board = this.boardRepository.findById(ObjectId(boardId))
            ?: throw NotFoundException(message="해당 게시판을 찾을 수 없습니다.")

        val groupMember = this.groupMemberRepository.findByGroupIdAndMemberId(
            ObjectId(groupId),
            ObjectId(memberInfo.memberId)
        ) ?: throw NotFoundException(message="해당 그룹의 멤버가 아닙니다.")

        if (!board.hasPermission(groupMember, BoardAction.READ_POST)) {
            throw NotFoundException(message = "해당 게시판에 글 조회 권한이 없습니다.")
        }

        val posts = this.postRepository.findByBoardIdLteId(
            boardId = boardId,
            lastId = cursor,
            limit = size
        ).sortedByDescending { it.createdAt }
        return posts.map { PostDto.from(it) }
    }

    override fun getPost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String
    ): PostDto {
        val post = this.postRepository.findById(postId)
            ?: throw NotFoundException(message="해당 게시글을 찾을 수 없습니다.")

        val board = this.boardRepository.findById(post.boardId)
            ?: throw NotFoundException(message="해당 게시판을 찾을 수 없습니다.")

        val groupMember = this.groupMemberRepository.findByGroupIdAndMemberId(
            post.groupId,
            ObjectId(memberInfo.memberId)
        ) ?: throw NotFoundException(message="해당 그룹의 멤버가 아닙니다.")

        if (!board.hasPermission(groupMember, BoardAction.READ_POST)) {
            throw NotFoundException(message = "해당 게시판에 글 조회 권한이 없습니다.")
        }

        return PostDto.from(post)
    }
}
