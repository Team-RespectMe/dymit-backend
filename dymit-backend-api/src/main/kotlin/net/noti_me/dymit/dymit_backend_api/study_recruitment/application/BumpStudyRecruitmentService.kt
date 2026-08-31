package net.noti_me.dymit.dymit_backend_api.study_recruitment.application

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.BumpStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.BumpStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.member.LoadDymitStudyRecruitmentMemberPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.LoadDymitStudyRecruitmentPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.SaveDymitStudyRecruitmentPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.study_group.DymitStudyRecruitmentLoadStudyGroupPort
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * Dymit 스터디 모집글 끌어올리기 서비스입니다.
 *
 * @property loadRecruitmentPort 모집글 조회 출력 포트
 * @property loadStudyGroupPort 그룹 조회 출력 포트
 * @property saveRecruitmentPort 모집글 저장 출력 포트
 * @property loadMemberPort 회원 조회 출력 포트
 */
@Service
class BumpStudyRecruitmentService(
    private val loadRecruitmentPort: LoadDymitStudyRecruitmentPort,
    private val loadStudyGroupPort: DymitStudyRecruitmentLoadStudyGroupPort,
    private val saveRecruitmentPort: SaveDymitStudyRecruitmentPort,
    private val loadMemberPort: LoadDymitStudyRecruitmentMemberPort
) : BumpStudyRecruitmentUseCase {

    /**
     * 그룹 소유자만 Dymit 모집글을 끌어올립니다.
     *
     * @param memberInfo 로그인 회원 정보
     * @param command 모집글 끌어올리기 명령
     * @return 끌어올린 모집글 DTO
     */
    override fun execute(
        memberInfo: MemberInfo,
        command: BumpStudyRecruitmentCommand
    ): DymitStudyRecruitmentDto {
        val recruitmentId = parseObjectId(command.recruitmentId)
        val persistenceDto = loadRecruitmentPort.loadById(recruitmentId)
            ?: throw NotFoundException(message = "존재하지 않는 Dymit 스터디 모집글입니다.")
        val group = loadStudyGroupPort.loadById(persistenceDto.groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")

        if ( group.ownerId.toHexString() != memberInfo.memberId ) {
            throw ForbiddenException(message = "그룹 소유자만 모집글을 끌어올릴 수 있습니다.")
        }

        val recruitment = persistenceDto.toDomain()
        recruitment.bump()
        val savedRecruitment = saveRecruitmentPort.save(recruitment)
        val writer = loadMemberPort.loadById(savedRecruitment.writerId)
            ?: throw NotFoundException(message = "존재하지 않는 작성자입니다.")

        return DymitStudyRecruitmentDto.from(savedRecruitment, writer)
    }

    private fun parseObjectId(value: String): ObjectId {
        if ( !ObjectId.isValid(value) ) {
            throw BadRequestException(message = "올바르지 않은 모집글 식별자입니다.")
        }
        return ObjectId(value)
    }
}
