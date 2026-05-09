package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.file

import net.noti_me.dymit.dymit_backend_api.domain.file.UserFile
import net.noti_me.dymit.dymit_backend_api.ports.persistence.file.UserFileRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * 사용자 파일 문서를 MongoDB에 저장하는 Repository 구현체입니다.
 *
 * @param mongoTemplate MongoTemplate 인스턴스
 */
@Repository
class MongoUserFileRepository(
    private val mongoTemplate: MongoTemplate
) : UserFileRepository {

    override fun save(userFile: UserFile): UserFile {
        return mongoTemplate.save(userFile)
    }

    override fun findById(fileId: String): UserFile? {
        return try {
            mongoTemplate.findById(ObjectId(fileId), UserFile::class.java)
        } catch (exception: IllegalArgumentException) {
            null
        }
    }

    override fun findByIds(fileIds: List<ObjectId>): List<UserFile> {
        if ( fileIds.isEmpty() ) {
            return emptyList()
        }

        val query = Query(Criteria.where("_id").`in`(fileIds))
        return mongoTemplate.find(query, UserFile::class.java)
    }
}
