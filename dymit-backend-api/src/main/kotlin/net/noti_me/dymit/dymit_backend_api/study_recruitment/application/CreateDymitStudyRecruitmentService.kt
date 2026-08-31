package net.noti_me.dymit.dymit_backend_api.study_recruitment.application

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.CreateDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.CreateDymitStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.member.LoadDymitStudyRecruitmentMemberPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.CheckDymitStudyRecruitmentExistencePort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.SaveDymitStudyRecruitmentPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.study_group.DymitStudyRecruitmentLoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitment
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentWriter
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * Dymit 스터디 모집글 생성 서비스입니다.
 *
 * @property loadStudyGroupPort 그룹 조회 출력 포트
 * @property saveRecruitmentPort 모집글 저장 출력 포트
 * @property loadMemberPort 회원 조회 출력 포트
 * @property checkRecruitmentExistencePort 모집글 존재 여부 조회 출력 포트
 */
@Service
class CreateDymitStudyRecruitmentService(
    private val loadStudyGroupPort: DymitStudyRecruitmentLoadStudyGroupPort,
    private val saveRecruitmentPort: SaveDymitStudyRecruitmentPort,
    private val loadMemberPort: LoadDymitStudyRecruitmentMemberPort,
    private val checkRecruitmentExistencePort: CheckDymitStudyRecruitmentExistencePort? = null
) : CreateDymitStudyRecruitmentUseCase {

    /**
     * 그룹 소유자만 Dymit 스터디 모집글을 생성합니다.
     *
     * @param memberInfo 로그인 회원 정보
     * @param command 모집글 생성 명령
     * @return 생성된 모집글 DTO
     */
    override fun execute(
        memberInfo: MemberInfo,
        command: CreateDymitStudyRecruitmentCommand
    ): DymitStudyRecruitmentDto {
        val groupId = parseObjectId(command.groupId, "그룹 식별자")
        if ( checkRecruitmentExistencePort?.existsActiveByGroupId(groupId) == true ) {
            throw ConflictException(message = "해당 스터디 그룹의 모집 공고가 이미 존재합니다.")
        }

        val group = loadStudyGroupPort.loadById(groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")

        verifyOwner(group.ownerId, memberInfo.memberId)

        val recruitment = DymitStudyRecruitment(
            writer = DymitStudyRecruitmentWriter(
                id = parseObjectId(memberInfo.memberId, "회원 식별자"),
                nickname = memberInfo.nickname
            ),
            groupId = group.id,
            type = StudyRecruitmentType.DYMIT,
            title = command.title,
            description = command.description,
            purpose = command.purpose,
            recruitmentStart = command.recruitmentStart,
            recruitmentEnd = command.recruitmentEnd,
            targetMember = command.targetMember,
            studyFormat = command.studyFormat,
            contact = command.contact,
            tags = command.tags
        )

        val savedRecruitment = saveRecruitmentPort.save(recruitment)
        val writer = loadMemberPort.loadById(savedRecruitment.writerId)
            ?: throw NotFoundException(message = "존재하지 않는 작성자입니다.")

        return DymitStudyRecruitmentDto.from(savedRecruitment, writer)
    }

    private fun verifyOwner(ownerId: ObjectId, memberId: String) {
        if ( ownerId.toHexString() != memberId ) {
            throw ForbiddenException(message = "그룹 소유자만 모집글을 생성할 수 있습니다.")
        }
    }

    private fun parseObjectId(value: String, fieldName: String): ObjectId {
        if ( !ObjectId.isValid(value) ) {
            throw BadRequestException(message = "올바르지 않은 ${fieldName}입니다.")
        }
        return ObjectId(value)
    }
}
