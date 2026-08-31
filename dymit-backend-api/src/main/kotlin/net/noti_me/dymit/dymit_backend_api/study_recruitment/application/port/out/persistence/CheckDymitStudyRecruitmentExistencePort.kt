package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence

import org.bson.types.ObjectId

/**
 * Dymit 스터디 모집글 존재 여부 조회 출력 포트입니다.
 */
fun interface CheckDymitStudyRecruitmentExistencePort {

    /**
     * 그룹의 미삭제 Dymit 모집글 존재 여부를 조회합니다.
     *
     * @param groupId 그룹 ObjectId
     * @return 미삭제 Dymit 모집글이 존재하면 true
     */
    fun existsActiveByGroupId(groupId: ObjectId): Boolean
}
