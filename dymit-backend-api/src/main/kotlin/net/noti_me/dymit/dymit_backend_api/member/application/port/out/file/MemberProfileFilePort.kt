package net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.file

import net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.file.dto.MemberProfileFileUploadCommand
import net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.file.dto.MemberProfileFileUploadDto

/**
 * 멤버 프로필 파일 업로드 출력 포트입니다.
 */
interface MemberProfileFilePort {

    /**
     * 멤버 프로필 이미지를 업로드합니다.
     *
     * @param command 업로드 명령
     * @return 업로드된 프로필 파일 정보
     */
    fun upload(command: MemberProfileFileUploadCommand): MemberProfileFileUploadDto
}
