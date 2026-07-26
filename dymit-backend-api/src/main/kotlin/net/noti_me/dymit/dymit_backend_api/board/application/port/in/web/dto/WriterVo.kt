package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardProfileImage
import net.noti_me.dymit.dymit_backend_api.board.domain.Writer

@Schema(description = "작성자 정보")
class WriterVo(
    @Schema(description = "작성자 ID", example = "507f1f77bcf86cd799439012")
    val memberId: String,
    @Schema(description = "작성자 닉네임", example = "john_doe")
    val nickname: String,
    @Schema(description = "작성자 프로필 이미지")
    val image: BoardProfileImage
) {
    companion object {
        fun from(writer: Writer): WriterVo {
            return WriterVo(
                memberId = writer.id.toHexString(),
                nickname = writer.nickname,
                image = writer.image
            )
        }

    }
}
