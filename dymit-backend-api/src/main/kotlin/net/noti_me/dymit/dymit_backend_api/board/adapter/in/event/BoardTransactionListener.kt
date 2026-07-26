package net.noti_me.dymit.dymit_backend_api.board.adapter.`in`.event

import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.dto.BoardWriterUpdateDto
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardProfileImageType
import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import net.noti_me.dymit.dymit_backend_api.member.domain.events.MemberNicknameChangedEvent
import net.noti_me.dymit.dymit_backend_api.member.domain.events.MemberProfileImageChangedEvent
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.CommentRepository
import net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.PostRepository
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class BoardTransactionListener(
    private val postRepository: PostRepository,
    private val postCommentRepository: CommentRepository
) {

    @Async
    @EventListener(classes = [MemberNicknameChangedEvent::class]) fun handleMemberNicknameChangedEvent(event: MemberNicknameChangedEvent) {
        updateWriter(event.member)
    }

    @Async
    @EventListener(classes = [MemberProfileImageChangedEvent::class])
    fun handleMemberProfileImageChangedEvent(event: MemberProfileImageChangedEvent) {
        updateWriter(event.member)
    }

    private fun updateWriter(member: Member) {
        val writer = BoardWriterUpdateDto(
            memberId = member.id!!,
            nickname = member.nickname,
            profileImageType = BoardProfileImageType.valueOf(member.profileImage.type.name),
            profileImageUrl = member.profileImage.thumbnail
        )
        postRepository.updateWriterInfo(writer)
        postCommentRepository.updateWriterInfo(writer)
    }
}
