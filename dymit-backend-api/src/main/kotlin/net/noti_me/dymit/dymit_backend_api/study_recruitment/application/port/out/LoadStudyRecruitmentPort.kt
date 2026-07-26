package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.dto.StudyRecruitmentPersistenceDto
import org.bson.types.ObjectId

/**
 * 스터디 모집 목록을 불러오는 출력 포트입니다.
 */
interface LoadStudyRecruitmentPort {

    /**
     * 커서보다 작은 식별자의 미삭제 모집글을 최신순으로 불러옵니다.
     *
     * @param cursorId 다음 페이지 조회를 위한 커서 ObjectId
     * @param size 조회 개수
     * @return 출력 포트용 스터디 모집 정보 목록
     */
    fun findByCursorOrderByIdDesc(
        cursorId: ObjectId?,
        size: Int
    ): List<StudyRecruitmentPersistenceDto>
}
