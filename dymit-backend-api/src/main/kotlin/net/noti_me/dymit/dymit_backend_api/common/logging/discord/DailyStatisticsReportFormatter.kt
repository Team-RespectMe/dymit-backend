package net.noti_me.dymit.dymit_backend_api.common.logging.discord

import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsReportDocument
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Formats completed daily statistics into a Korean Discord report.
 */
@Component
class DailyStatisticsReportFormatter {

    /**
     * Builds a Discord embed containing every business field from the daily-statistics document.
     */
    fun format(document: DailyStatisticsReportDocument): DiscordMessageDto {
        return DiscordMessageDto(
            content = "",
            embeds = listOf(
                Embed(
                    title = "일일 통계 보고서 (${document.statisticDate})",
                    description = """
### 집계 기간
- 시작: ${formatDateTime(document.windowStart)}
- 종료: ${formatDateTime(document.windowEnd)}
### 회원
- 가입: ${document.member.joinedCount}
- 탈퇴: ${document.member.withdrawnCount}
- 방문: ${document.member.visitorCount}
### 스터디 그룹
- 생성: ${document.studyGroup.createdCount}
### 스터디 일정
- 생성: ${document.studySchedule.createdCount}
- 참여 회원(중복 제외): ${document.studySchedule.participantMemberCount}
### 과제
- 생성: ${document.task.createdCount}
- 제출: ${document.task.submittedCount}
### 문서 시각
- 생성: ${formatDateTime(document.createdAt)}
- 수정: ${formatDateTime(document.updatedAt)}
                    """.trimIndent()
                )
            )
        )
    }

    private fun formatDateTime(value: LocalDateTime): String {
        return DATE_TIME_FORMATTER.format(value)
    }

    private companion object {
        val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}
