package net.noti_me.dymit.dymit_backend_api.board.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.TypeAlias

@TypeAlias("net.noti_me.dymit.dymit_backend_api.domain.board.Writer")
class Writer(
    val id: ObjectId,
    var nickname: String,
    var image: BoardProfileImage
) {

    companion object {
        /**
         * 작성자 정보에 필요한 스칼라 값으로 작성자를 생성합니다.
         *
         * @param id 멤버 식별자
         * @param nickname 작성자 닉네임
         * @param imageType 프로필 이미지 종류
         * @param imageUrl 프로필 이미지 URL
         * @return 게시판 작성자
         */
        fun of(
            id: ObjectId,
            nickname: String,
            imageType: BoardProfileImageType,
            imageUrl: String
        ): Writer {
            return Writer(
                id = id,
                nickname = nickname,
                image = BoardProfileImage(
                    type = imageType,
                    url = imageUrl
                )
            )
        }
    }
}
