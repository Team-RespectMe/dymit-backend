package net.noti_me.dymit.dymit_backend_api.units.application.board.v2

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.board.v2.impl.RemovePostUseCaseImplV2
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.PostRepositoryV2
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.supports.createProfileImageVo
import org.bson.types.ObjectId

internal class RemovePostUseCaseImplV2Test : BehaviorSpec({

    val postRepository = mockk<PostRepositoryV2>()
    val loadStudyGroupPort = mockk<LoadStudyGroupPort>()
    val saveStudyGroupPort = mockk<SaveStudyGroupPort>(relaxed = true)
    val studyGroupMemberRepository = mockk<StudyGroupMemberRepository>()

    val useCase = RemovePostUseCaseImplV2(
        postRepository = postRepository,
        loadGroupPort = loadStudyGroupPort,
        saveGroupPort = saveStudyGroupPort,
        groupMemberRepository = studyGroupMemberRepository
    )

    val groupId = ObjectId.get()
    val boardId = ObjectId.get()
    val memberId = ObjectId.get()
    val postId = ObjectId.get()

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

    fun createPost(writerMember: StudyGroupMember): Post {
        return Post(
            id = postId,
            groupId = groupId,
            boardId = boardId,
            writer = Writer.from(writerMember),
            title = "title",
            content = "content",
            category = PostCategory.QUESTION
        )
    }

    fun createGroup(): StudyGroup {
        return StudyGroup(
            id = groupId,
            ownerId = memberId,
            name = "group",
            description = "description"
        )
    }

    beforeEach {
        clearAllMocks()
    }

    given("게시글 V2 삭제 시") {
        `when`("정상 요청이면") {
            then("게시글을 삭제하고 최근 게시글을 갱신한다") {
                val groupMember = createGroupMember()
                val post = createPost(groupMember)
                val group = createGroup()
                every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns group
                every { postRepository.findById(postId.toHexString()) } returns post
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns groupMember
                every { postRepository.deleteById(post.identifier) } returns true
                every { postRepository.findLastPostByGroupIdAndBoardId(groupId, boardId) } returns null
                every { saveStudyGroupPort.update(any()) } returns group

                useCase.remove(
                    memberInfo = memberInfo(),
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    postId = postId.toHexString()
                )

                verify(exactly = 1) { postRepository.deleteById(post.identifier) }
                verify(exactly = 1) { saveStudyGroupPort.update(any()) }
            }
        }

        `when`("그룹이 없으면") {
            then("리소스 예외가 발생한다") {
                every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns null

                shouldThrow<NotFoundException> {
                    useCase.remove(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString(),
                        postId = postId.toHexString()
                    )
                }
            }
        }

        `when`("게시글이 없으면") {
            then("리소스 예외가 발생한다") {
                every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns createGroup()
                every { postRepository.findById(postId.toHexString()) } returns null

                shouldThrow<NotFoundException> {
                    useCase.remove(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString(),
                        postId = postId.toHexString()
                    )
                }
            }
        }

        `when`("요청자가 그룹 멤버가 아니면") {
            then("리소스 예외가 발생한다") {
                val groupMember = createGroupMember()
                val post = createPost(groupMember)
                every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns createGroup()
                every { postRepository.findById(postId.toHexString()) } returns post
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns null

                shouldThrow<NotFoundException> {
                    useCase.remove(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString(),
                        postId = postId.toHexString()
                    )
                }
            }
        }
    }
})
