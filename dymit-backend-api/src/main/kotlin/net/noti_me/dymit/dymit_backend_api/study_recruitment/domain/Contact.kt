package net.noti_me.dymit.dymit_backend_api.study_recruitment.domain

/**
 * Dymit 스터디 모집글의 연락처 값 객체입니다.
 *
 * @property url 연락 URL
 * @property title 연락처 표시 제목
 */
data class Contact(
    val url: String,
    val title: String
) {

    init {
        require(url.length <= URL_MAX_LENGTH) {
            "연락 URL은 ${URL_MAX_LENGTH}자 이내로 작성해야 합니다."
        }
    }

    private companion object {
        const val URL_MAX_LENGTH = 255
    }
}
