package net.noti_me.dymit.dymit_backend_api.board.application.port.out.persistence.dto

import net.noti_me.dymit.dymit_backend_api.board.domain.BoardProfileImageType
import org.bson.types.ObjectId

/**
 * 게시판 저장 문서의 작성자 정보를 갱신하기 위한 DTO입니다.
 */
data class BoardWriterUpdateDto(
    val memberId: ObjectId,
    val nickname: String,
    val profileImageType: BoardProfileImageType,
    val profileImageUrl: String
)
