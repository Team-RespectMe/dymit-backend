package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import org.bson.types.ObjectId

/**
 * 태스크 유즈케이스용 ObjectId 파서입니다.
 */
object TaskUseCaseObjectIdParser {

    /**
     * 문자열을 ObjectId로 변환합니다.
     *
     * @param value 검증할 문자열 값
     * @param fieldName 필드명
     * @return 파싱된 ObjectId
     */
    fun parse(value: String, fieldName: String): ObjectId {
        if ( !ObjectId.isValid(value) ) {
            throw BadRequestException(message = "${fieldName} 형식이 올바르지 않습니다.")
        }
        return ObjectId(value)
    }
}
