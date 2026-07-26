package net.noti_me.dymit.dymit_backend_api.domain.server_notice

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import org.bson.types.ObjectId

/**
 * 서버 공지 작성자 값입니다.
 *
 * @property id 작성자 식별자
 * @property nickname 작성자 닉네임
 * @property image 작성자 프로필 이미지
 */
data class ServerNoticeWriter(
    val id: ObjectId,
    val nickname: String,
    val image: ServerNoticeWriterImage
) {

    companion object {
        /**
         * 서버 공지 작성자를 필요한 스칼라 값으로 생성합니다.
         */
        fun of(
            id: ObjectId,
            nickname: String,
            imageType: ProfileImageType,
            imageUrl: String
        ): ServerNoticeWriter {
            return ServerNoticeWriter(
                id = id,
                nickname = nickname,
                image = ServerNoticeWriterImage(
                    type = imageType,
                    url = imageUrl
                )
            )
        }
    }
}

/**
 * 서버 공지 작성자의 프로필 이미지 값입니다.
 */
data class ServerNoticeWriterImage(
    val type: ProfileImageType,
    val url: String
)
