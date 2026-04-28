package net.noti_me.dymit.dymit_backend_api.domain.push

import org.bson.types.ObjectId

/**
 * 그룹 푸시 메시지
 *
 * @param groupId 그룹 ID
 * @param title 푸시 제목
 * @param body 푸시 내용
 * @param image 푸시 이미지 URL (선택 사항)
 * @param data 추가 데이터 (선택 사항)
 */
class GroupPushMessage(
    val groupId: ObjectId,
    val eventName: String,
    val title: String = "Dymit",
    val body: String,
    val image: String? = null,
    val data: Map<String, String> = emptyMap(),
    val excluded: MutableSet<ObjectId> = mutableSetOf()
) {
}
