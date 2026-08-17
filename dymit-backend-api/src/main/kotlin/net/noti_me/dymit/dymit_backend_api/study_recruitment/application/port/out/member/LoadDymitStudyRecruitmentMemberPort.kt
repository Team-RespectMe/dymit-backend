package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.member

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.member.dto.DymitStudyRecruitmentMemberDto
import org.bson.types.ObjectId

/**
 * Dymit 스터디 모집글 작성자의 최신 회원 정보를 조회하는 출력 포트입니다.
 */
interface LoadDymitStudyRecruitmentMemberPort {

    /**
     * 회원 식별자로 작성자 정보를 조회합니다.
     *
     * @param memberId 회원 식별자
     * @return 모집글 모듈 전용 회원 DTO, 없으면 null
     */
    fun loadById(memberId: ObjectId): DymitStudyRecruitmentMemberDto?

    /**
     * 여러 회원 식별자로 작성자 정보를 조회합니다.
     *
     * @param memberIds 회원 식별자 목록
     * @return 조회된 모집글 모듈 전용 회원 DTO 목록
     */
    fun loadByIds(memberIds: Collection<ObjectId>): List<DymitStudyRecruitmentMemberDto>
}
