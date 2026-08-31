package net.noti_me.dymit.dymit_backend_api.study_recruitment.application

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.QueryStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.QueryStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.StudyRecruitmentDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.LoadStudyRecruitmentPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.dto.StudyRecruitmentPersistenceDto
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * 스터디 모집 목록 조회 유즈케이스 구현체입니다.
 *
 * @property loadStudyRecruitmentPort 스터디 모집 조회 출력 포트
 */
@Service
class QueryStudyRecruitmentService(
    private val loadStudyRecruitmentPort: LoadStudyRecruitmentPort
) : QueryStudyRecruitmentUseCase {

    /**
     * 다음 페이지 판단을 위해 요청 크기보다 한 건 더 조회합니다.
     *
     * @param command 스터디 모집 목록 조회 명령
     * @return 입력 포트용 스터디 모집 정보 목록
     */
    override fun execute(command: QueryStudyRecruitmentCommand): List<StudyRecruitmentDto> {
        val cursorId = command.cursor?.let(::ObjectId)
        return loadStudyRecruitmentPort.findByCursorOrderByIdDesc(
            cursorId = cursorId,
            size = command.size + 1
        ).map { it.toInputDto() }
    }

    private fun StudyRecruitmentPersistenceDto.toInputDto(): StudyRecruitmentDto {
        return StudyRecruitmentDto(
            id = id,
            externalId = externalId,
            type = type,
            title = title,
            content = content,
            url = "${url}/${title}",
            writer = writer,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
