package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.study_group

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.study_group.dto.DymitStudyRecruitmentStudyGroupDto
import org.bson.types.ObjectId

/**
 * Dymit 스터디 모집글에서 그룹 정보를 조회하는 출력 포트입니다.
 */
interface DymitStudyRecruitmentLoadStudyGroupPort {

    /**
     * 그룹 식별자로 모집글에 필요한 그룹 정보를 조회합니다.
     *
     * @param groupId 그룹 ObjectId
     * @return 그룹이 있으면 그룹 DTO, 없으면 null
     */
    fun loadById(groupId: ObjectId): DymitStudyRecruitmentStudyGroupDto?
}
