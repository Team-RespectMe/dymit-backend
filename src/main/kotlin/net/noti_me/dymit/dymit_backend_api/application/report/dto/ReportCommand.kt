package net.noti_me.dymit.dymit_backend_api.application.report.dto

import net.noti_me.dymit.dymit_backend_api.domain.report.ReportedResource

/**
 * 신고 생성을 위한 커맨드 객체
 * 유저가 새로운 신고를 생성할 때 필요한 정보를 담습니다.
 *
 * @param title 신고 제목
 * @param content 신고 내용
 * @param resource 신고된 리소스 정보 (ResourceType과 resourceId 포함)
 */
data class ReportCommand(
    val title: String,
    val content: String,
    val resource: ReportedResource
) {

}
