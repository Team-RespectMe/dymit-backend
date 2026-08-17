package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.dto.DymitStudyRecruitmentPersistenceDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitment

/**
 * Dymit 스터디 모집글 저장 출력 포트입니다.
 */
interface SaveDymitStudyRecruitmentPort {

    /**
     * Dymit 스터디 모집글을 저장합니다.
     *
     * @param recruitment 저장할 도메인 엔티티
     * @return 저장된 모집글 영속성 DTO
     */
    fun save(recruitment: DymitStudyRecruitment): DymitStudyRecruitmentPersistenceDto
}
