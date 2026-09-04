package net.noti_me.dymit.dymit_backend_api.board.application.v1

import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.PostService
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.dto.PostCommand
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.dto.PostDto
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardAction
import net.noti_me.dymit.dymit_backend_api.board.domain.Post
import net.noti_me.dymit.dymit_backend_api.board.domain.Writer
import net.noti_me.dymit.dymit_backend_api.board.application.event.PostCreatedEvent
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.BoardRepository
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.PostRepository
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.BoardStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.study_group.dto.BoardRecentPostDto
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val studyGroupPort: BoardStudyGroupPort,
    private val boardRepository: BoardRepository,
    private val eventPublisher: ApplicationEventPublisher
): PostService {

    override fun createPost(
        memberInfo: MemberInfo,
        command: PostCommand
    ): PostDto {
        val board = this.boardRepository.findById(ObjectId(command.boardId))
            ?: throw NotFoundException(message="해당 게시판을 찾을 수 없습니다.")

        val group = studyGroupPort.loadGroup(command.groupId)
            ?: throw NotFoundException(message="해당 그룹을 찾을 수 없습니다.")

        val groupMember = studyGroupPort.loadMember(
            ObjectId(command.groupId),
            ObjectId(memberInfo.memberId)
        )?.toDomain() ?: throw NotFoundException(message="해당 그룹의 멤버가 아닙니다.")

        if (!board.hasPermission(groupMember, BoardAction.WRITE_POST)) {
            throw NotFoundException(message = "해당 게시판에 글 작성 권한이 없습니다.")
        }

        val newPost = Post(
            groupId = ObjectId(command.groupId),
            boardId = board.id!!,
            writer = Writer.of(
                id = groupMember.memberId,
                nickname = groupMember.nickname,
                imageType = groupMember.profileImage.type,
                imageUrl = groupMember.profileImage.url
            ),
            title = command.title,
            content = command.content
        )

        val savedPost = this.postRepository.save(newPost)
        studyGroupPort.updateRecentPost(
            command.groupId,
            BoardRecentPostDto(
                postId = savedPost.identifier,
                title = savedPost.title,
                createdAt = savedPost.createdAt ?: java.time.Instant.now()
            )
        )
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
        this.boardRepository.findById(ObjectId(command.boardId))
            ?: throw NotFoundException(message="해당 게시판을 찾을 수 없습니다.")

        val post = this.postRepository.findById(postId)
            ?: throw NotFoundException(message="해당 게시글을 찾을 수 없습니다.")

        val group = studyGroupPort.loadGroup(command.groupId)
            ?: throw NotFoundException(message="해당 그룹을 찾을 수 없습니다.")

        studyGroupPort.loadMember(
            ObjectId(command.groupId),
            ObjectId(memberInfo.memberId)
        ) ?: throw NotFoundException(message="해당 그룹의 멤버가 아닙니다.")

        post.updateTitle(memberInfo.memberId, command.title)
        post.updateContent(memberInfo.memberId, command.content)
        val updatedPost = this.postRepository.save(post)

        if (group.recentPost?.postId == updatedPost.identifier) {
            studyGroupPort.updateRecentPost(
                command.groupId,
                BoardRecentPostDto(
                    postId = updatedPost.identifier,
                    title = updatedPost.title,
                    createdAt = updatedPost.createdAt ?: java.time.Instant.now()
                )
            )
        }

        return PostDto.from(updatedPost)
    }

    override fun removePost(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String,
        postId: String
    ) {
        studyGroupPort.loadGroup(groupId)
            ?: throw NotFoundException(message="해당 그룹을 찾을 수 없습니다.")

        val post = this.postRepository.findById(postId)
            ?: throw NotFoundException(message="해당 게시글을 찾을 수 없습니다.")

        studyGroupPort.loadMember(
            post.groupId,
            ObjectId(memberInfo.memberId)
        ) ?: throw NotFoundException(message="해당 그룹의 멤버가 아닙니다.")

        this.postRepository.deleteById(post.identifier)

        val recentPost = postRepository.findLastPostByGroupIdAndBoardId(
            groupId = ObjectId(groupId),
            boardId = ObjectId(boardId)
        )

        studyGroupPort.updateRecentPost(
            groupId,
            recentPost?.let {
                BoardRecentPostDto(
                    postId = it.identifier,
                    title = it.title,
                    createdAt = it.createdAt ?: java.time.Instant.now()
                )
            }
        )
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

        val groupMember = studyGroupPort.loadMember(
            ObjectId(groupId),
            ObjectId(memberInfo.memberId)
        )?.toDomain() ?: throw NotFoundException(message="해당 그룹의 멤버가 아닙니다.")

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

        val groupMember = studyGroupPort.loadMember(
            post.groupId,
            ObjectId(memberInfo.memberId)
        )?.toDomain() ?: throw NotFoundException(message="해당 그룹의 멤버가 아닙니다.")

        if (!board.hasPermission(groupMember, BoardAction.READ_POST)) {
            throw NotFoundException(message = "해당 게시판에 글 조회 권한이 없습니다.")
        }

        return PostDto.from(post)
    }
}
