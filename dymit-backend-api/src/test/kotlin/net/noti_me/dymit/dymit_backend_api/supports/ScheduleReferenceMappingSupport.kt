package net.noti_me.dymit.dymit_backend_api.supports

import org.bson.Document
import org.bson.RawBsonDocument
import org.bson.codecs.DocumentCodec
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

/** DB 연결 없이 실제 BSON 직렬화와 역직렬화를 수행합니다. */
fun scheduleBsonRoundTrip(document: Document): Document =
    RawBsonDocument(document, DocumentCodec()).decode(DocumentCodec())

/** 실제 Spring Data 문서 매핑 동작을 검증할 변환기를 생성합니다. */
fun createStatisticsMongoConverter(): MappingMongoConverter {
    val conversions = MongoCustomConversions(emptyList<Any>())
    val context = MongoMappingContext().apply {
        setSimpleTypeHolder(conversions.simpleTypeHolder)
        afterPropertiesSet()
    }
    return MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context).apply {
        setCustomConversions(conversions)
        afterPropertiesSet()
    }
}

/** 일정 참조 조회와 같은 세 필드 projection을 실제 BSON 문서에 적용합니다. */
fun scheduleReferenceProjection(document: Document): Document {
    val projected = Document()
    listOf("_id", "deletedAt", "updatedAt").forEach { key ->
        if (document.containsKey(key)) {
            projected[key] = document[key]
        }
    }
    return scheduleBsonRoundTrip(projected)
}
