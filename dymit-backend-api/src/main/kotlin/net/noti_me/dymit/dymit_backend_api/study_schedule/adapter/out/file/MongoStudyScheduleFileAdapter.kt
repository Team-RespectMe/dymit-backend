package net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.out.file

import net.noti_me.dymit.dymit_backend_api.configs.CDNConfig
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.file.StudyScheduleFilePort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.file.dto.StudyScheduleFileDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.file.dto.StudyScheduleFileStatusDto
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component

@Component
class MongoStudyScheduleFileAdapter(
    private val mongoTemplate: MongoTemplate,
    private val cdnConfig: CDNConfig
) : StudyScheduleFilePort {

    override fun loadByIds(fileIds: List<ObjectId>): List<StudyScheduleFileDto> {
        if ( fileIds.isEmpty() ) {
            return emptyList()
        }
        val query = Query(Criteria.where("_id").`in`(fileIds))
        return mongoTemplate.find(query, Document::class.java, COLLECTION_NAME).map { it.toDto() }
    }

    override fun updateStatus(
        fileId: ObjectId,
        status: StudyScheduleFileStatusDto
    ): StudyScheduleFileStatusDto? {
        val query = Query(Criteria.where("_id").`is`(fileId))
        if ( !mongoTemplate.exists(query, COLLECTION_NAME) ) {
            return null
        }
        mongoTemplate.updateFirst(
            query,
            Update().set("status", status.name),
            COLLECTION_NAME
        )
        return status
    }

    private fun Document.toDto(): StudyScheduleFileDto {
        val path = getString("path")
        val thumbnailPath = getString("thumbnailPath")
        val domain = cdnConfig.getDomain().trimEnd('/')
        return StudyScheduleFileDto(
            id = getObjectId("_id"),
            originalFileName = getString("originalFileName"),
            path = path,
            thumbnailPath = thumbnailPath,
            status = StudyScheduleFileStatusDto.valueOf(get("status").toString()),
            contentType = getString("contentType"),
            fileSize = (get("fileSize") as Number).toLong(),
            url = domain + path,
            thumbnailUrl = thumbnailPath?.let { domain + it }
        )
    }

    private companion object {
        const val COLLECTION_NAME = "user_files"
    }
}
