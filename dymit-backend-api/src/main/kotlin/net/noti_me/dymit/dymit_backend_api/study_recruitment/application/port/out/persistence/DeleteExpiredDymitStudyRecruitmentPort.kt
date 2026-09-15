package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence

import java.time.Instant

/**
 * 만료된 Dymit 스터디 모집글을 물리 삭제하는 출력 포트입니다.
 */
fun interface DeleteExpiredDymitStudyRecruitmentPort {

    /**
     * 기준 시각 이전에 수정된 모집 종료 글을 물리 삭제합니다.
     *
     * @param cutoff 삭제 대상에 포함되는 마지막 수정 시각
     * @return 삭제된 모집글 수
     */
    fun deleteExpired(cutoff: Instant): Long
}
