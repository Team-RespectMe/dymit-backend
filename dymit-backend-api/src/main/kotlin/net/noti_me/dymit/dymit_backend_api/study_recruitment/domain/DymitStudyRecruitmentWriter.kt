package net.noti_me.dymit.dymit_backend_api.study_recruitment.domain

import org.bson.types.ObjectId

/**
 * Dymit 스터디 모집글 작성자 값 객체입니다.
 *
 * @property id 작성자 ObjectId
 * @property nickname 작성자 표시 닉네임
 */
data class DymitStudyRecruitmentWriter(
    val id: ObjectId,
    val nickname: String
)
