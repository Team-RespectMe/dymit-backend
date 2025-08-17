package net.noti_me.dymit.dymit_backend_api.common.response

class ListResponse<T>(
    val count: Long,
    val items: List<T> = emptyList(),
    _links: MutableMap<String, HateoasLink> = mutableMapOf()
) : BaseResponse(
    _links = _links
) {

    companion object {
        fun of(
            count: Long,
            items: List<Any>,
            _links: MutableMap<String, HateoasLink> = mutableMapOf()
        ): ListResponse<Any> {
            return ListResponse(
                count = count,
                items = items,
                _links = _links
            )
        }

        fun <T> from(items: List<T>) : ListResponse<T> {
            return ListResponse(
                count = items.size.toLong(),
                items = items
            )
        }
    }
}