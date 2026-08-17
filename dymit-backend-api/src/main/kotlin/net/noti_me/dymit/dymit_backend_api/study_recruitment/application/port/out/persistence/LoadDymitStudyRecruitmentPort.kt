package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.dto.DymitStudyRecruitmentPersistenceDto
import org.bson.types.ObjectId

/**
 * Dymit 스터디 모집글 조회 출력 포트입니다.
 */
interface LoadDymitStudyRecruitmentPort {

    /**
     * Dymit 스터디 모집글을 식별자로 조회합니다.
     *
     * @param recruitmentId 모집글 ObjectId
     * @return 모집글이 있으면 영속성 DTO, 없으면 null
     */
    fun loadById(recruitmentId: ObjectId): DymitStudyRecruitmentPersistenceDto?

    /**
     * 커서보다 작은 Dymit 스터디 모집글을 최신순으로 조회합니다.
     *
     * @param cursorId 다음 페이지 커서 ObjectId
     * @param size 조회 개수
     * @return Dymit 스터디 모집글 영속성 DTO 목록
     */
    fun loadByCursorOrderByIdDesc(
        cursorId: ObjectId?,
        size: Int
    ): List<DymitStudyRecruitmentPersistenceDto>
}
