package net.noti_me.dymit.dymit_backend_api.ports.persistence.study_recruitment

import net.noti_me.dymit.dymit_backend_api.domain.study_recruitment.StudyRecruitment
import org.bson.types.ObjectId

/**
 * 스터디 모집 영속성 포트 인터페이스입니다.
 */
interface StudyRecruitmentRepository {

    /**
     * 커서 기반으로 스터디 모집 목록을 조회합니다.
     *
     * @param cursorId 다음 페이지 조회를 위한 커서 ObjectId
     * @param size 조회 개수
     * @return 스터디 모집 도메인 엔티티 목록
     */
    fun findByCursorOrderByIdDesc(cursorId: ObjectId?, size: Int): List<StudyRecruitment>
}
