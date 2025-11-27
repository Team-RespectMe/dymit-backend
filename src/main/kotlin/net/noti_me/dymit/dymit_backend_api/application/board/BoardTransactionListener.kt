package net.noti_me.dymit.dymit_backend_api.application.board

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberNicknameChangedEvent
import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberProfileImageChangedEvent
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.CommentRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.PostRepository
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
        val member = event.member
        postRepository.updateWriterInfo(member)
        postCommentRepository.updateWriterInfo(member)
    }

    @Async
    @EventListener(classes = [MemberProfileImageChangedEvent::class])
    fun handleMemberProfileImageChangedEvent(event: MemberProfileImageChangedEvent) {
        val member = event.member
        postRepository.updateWriterInfo(member)
        postCommentRepository.updateWriterInfo(member)
    }
}
