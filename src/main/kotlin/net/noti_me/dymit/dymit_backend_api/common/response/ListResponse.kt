package net.noti_me.dymit.dymit_backend_api.common.response

import net.noti_me.dymit.dymit_backend_api.common.pagination.CursorNextUrlBuilder

class ListResponse<T>(
    val count: Long,
    val items: List<T> = emptyList(),
    _links: MutableMap<String, HateoasLink> = mutableMapOf()
) : BaseResponse(
    _links = _links
) {

    companion object {
//        fun of(
//            count: Long,
//            items: List<Any>,
//            _links: MutableMap<String, HateoasLink> = mutableMapOf()
//        ): ListResponse<Any> {
//            return ListResponse(
//                count = count,
//                items = items,
//                _links = _links
//            )
//        }

        fun <T> from(items: List<T>) : ListResponse<T> {
            return ListResponse(
                count = items.size.toLong(),
                items = items
            )
        }

        /**
         * CursorPagination을 적용한 ListResponse를 생성합니다
         *
         * @param size 실제 응답에 포함할 아이템 수
         * @param list size + 1개를 조회한 아이템 리스트
         * @param extractor 커서 값을 추출하는 람다 함수
         * @return CursorPagination이 적용된 ListResponse
         */
        fun <T> of(
            size: Int,
            items: List<T>,
            extractor: (T) -> Any
        ): ListResponse<T> {
            // 실제 응답에는 size개만 포함
            val responseItems = if (items.size > size) {
                items.take(size)
            } else {
                items
            }

            val response = ListResponse(
                count = responseItems.size.toLong(),
                items = responseItems
            )

            // next URL 추가 (원본 list로 판단)
            val nextUrl = CursorNextUrlBuilder.buildNextUrlWithExtractor(items, extractor, size)
            if (nextUrl != null) {
                response._links["next"] = HateoasLink(nextUrl)
            }

            return response
        }
    }
}