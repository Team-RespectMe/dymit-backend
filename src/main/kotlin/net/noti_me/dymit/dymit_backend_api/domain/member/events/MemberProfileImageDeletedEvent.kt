package net.noti_me.dymit.dymit_backend_api.domain.member.events

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import org.springframework.context.ApplicationEvent


/**
 * 멤버 프로필 이미지가 삭제되었을 때 발행되는 이벤트
 * 이 이벤트는 도메인 이벤트로, Push나 Feed와는 관계가 없다.
 * 파일 핸들러 쪽은 이 이벤트가 발행되면 삭제된 이미지 파일을 스토리지에서 삭제한다.
 * @param filePath 삭제된 이미지의 파일 경로
 * @param source 프로필 이미지가 삭제된 멤버
 */
class MemberProfileImageDeletedEvent(
    val filePath: String,
    source: Member
): ApplicationEvent(source) {

}