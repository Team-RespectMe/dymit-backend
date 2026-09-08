package net.noti_me.dymit.dymit_backend_api.study_recruitment.application

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.CheckDymitStudyRecruitmentExistenceUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.CheckDymitStudyRecruitmentExistenceCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentExistenceDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.CheckDymitStudyRecruitmentExistencePort
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * 그룹별 Dymit 스터디 모집글 존재 여부 확인 서비스입니다.
 *
 * @property checkRecruitmentExistencePort 모집글 존재 여부 조회 출력 포트
 */
@Service
class CheckDymitStudyRecruitmentExistenceService(
    private val checkRecruitmentExistencePort: CheckDymitStudyRecruitmentExistencePort
) : CheckDymitStudyRecruitmentExistenceUseCase {

    /**
     * 요청한 그룹 순서를 유지하면서 모집글 존재 여부를 확인합니다.
     *
     * @param command 그룹 식별자 목록 확인 명령
     * @return 그룹별 모집글 존재 여부 목록
     */
    override fun execute(
        command: CheckDymitStudyRecruitmentExistenceCommand
    ): List<DymitStudyRecruitmentExistenceDto> {
        return command.groupIds.map { groupId ->
            DymitStudyRecruitmentExistenceDto(
                groupId = groupId,
                exists = checkRecruitmentExistencePort.existsActiveByGroupId(
                    parseObjectId(groupId)
                )
            )
        }
    }

    private fun parseObjectId(groupId: String): ObjectId {
        if ( !ObjectId.isValid(groupId) ) {
            throw BadRequestException(message = "올바르지 않은 그룹 식별자입니다.")
        }
        return ObjectId(groupId)
    }
}
