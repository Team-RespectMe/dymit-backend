package net.noti_me.dymit.dymit_backend_api.study_recruitment.application

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.GetDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.GetDymitStudyRecruitmentQuery
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.member.LoadDymitStudyRecruitmentMemberPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.LoadDymitStudyRecruitmentPort
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * Dymit 스터디 모집글 단건 조회 서비스입니다.
 *
 * @property loadRecruitmentPort 모집글 조회 출력 포트
 * @property loadMemberPort 회원 조회 출력 포트
 */
@Service
class GetDymitStudyRecruitmentService(
    private val loadRecruitmentPort: LoadDymitStudyRecruitmentPort,
    private val loadMemberPort: LoadDymitStudyRecruitmentMemberPort
) : GetDymitStudyRecruitmentUseCase {

    /**
     * Dymit 스터디 모집글을 단건 조회합니다.
     *
     * @param query 단건 조회 쿼리
     * @return 모집글 DTO
     */
    override fun execute(query: GetDymitStudyRecruitmentQuery): DymitStudyRecruitmentDto {
        val recruitmentId = parseObjectId(query.recruitmentId)
        val recruitment = loadRecruitmentPort.loadById(recruitmentId)
            ?: throw NotFoundException(message = "존재하지 않는 Dymit 스터디 모집글입니다.")
        val writer = loadMemberPort.loadById(recruitment.writerId)
            ?: throw NotFoundException(message = "존재하지 않는 작성자입니다.")

        return DymitStudyRecruitmentDto.from(recruitment, writer)
    }

    private fun parseObjectId(value: String): ObjectId {
        if ( !ObjectId.isValid(value) ) {
            throw BadRequestException(message = "올바르지 않은 모집글 식별자입니다.")
        }
        return ObjectId(value)
    }
}
