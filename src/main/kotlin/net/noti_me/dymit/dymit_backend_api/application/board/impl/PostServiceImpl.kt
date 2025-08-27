package net.noti_me.dymit.dymit_backend_api.application.board.impl

import net.noti_me.dymit.dymit_backend_api.application.board.PostService
import net.noti_me.dymit.dymit_backend_api.application.board.dto.PostCommand
import net.noti_me.dymit.dymit_backend_api.application.board.dto.PostDto
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.study_group.RecentPostVo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.BoardRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.PostRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val loadGroupPort: LoadStudyGroupPort,
    private val saveGroupPort: SaveStudyGroupPort,
    private val boardRepository: BoardRepository,
    private val groupMemberRepository: StudyGroupMemberRepository,
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
            boardId = board.id,
            writer = Writer.from(groupMember),
            title = command.title,
            content = command.content
        )

        val savedPost = this.postRepository.save(newPost)
            ?: throw RuntimeException("게시글 생성에 실패했습니다.")

        group.updateRecentPost(RecentPostVo.from(savedPost))
        saveGroupPort.persist(group)

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
            ?: throw RuntimeException("게시글 수정에 실패했습니다.")

        if (group.recentPost?.postId == updatedPost.id.toHexString()) {
            group.updateRecentPost(RecentPostVo.from(updatedPost))
            saveGroupPort.update(group)
        }

        return PostDto.from(updatedPost)
    }

    override fun removePost(memberInfo: MemberInfo,
                            groupId: String,
                            boardId: String,
                            postId: String) {
        val post = this.postRepository.findById(postId)
            ?: throw NotFoundException(message="해당 게시글을 찾을 수 없습니다.")

        val groupMember = this.groupMemberRepository.findByGroupIdAndMemberId(
            post.groupId,
            ObjectId(memberInfo.memberId)
        ) ?: throw NotFoundException(message="해당 그룹의 멤버가 아닙니다.")

        this.postRepository.deleteById(post.id.toHexString())
    }

    override fun getBoardPosts(
        memberInfo: MemberInfo,
        groupId: String,
        boardId: String
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

        val posts = this.postRepository.findByBoardId(boardId = board.id.toHexString())
            .sortedByDescending { it.createdAt }
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