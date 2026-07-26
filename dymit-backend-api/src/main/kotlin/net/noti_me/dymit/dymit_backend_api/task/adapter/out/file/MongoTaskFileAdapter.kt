package net.noti_me.dymit.dymit_backend_api.task.adapter.`out`.file

import net.noti_me.dymit.dymit_backend_api.configs.CDNConfig
import net.noti_me.dymit.dymit_backend_api.file.application.usecase.UpdateFileStatusUseCase
import net.noti_me.dymit.dymit_backend_api.file.application.port.`in`.web.dto.FileStatusDto
import net.noti_me.dymit.dymit_backend_api.file.application.port.`in`.web.dto.UpdateFileStatusCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.TaskFilePort
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto.TaskFileDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto.TaskFileStatusDto
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

/**
 * 과제 모듈의 파일 출력 포트를 MongoDB와 파일 유즈케이스에 연결합니다.
 *
 * @param mongoTemplate 파일 문서 조회용 MongoDB 템플릿
 * @param cdnConfig 파일 접근 URL 설정
 * @param updateFileStatusUseCase 파일 상태 변경 유즈케이스
 */
@Component
class MongoTaskFileAdapter(
    private val mongoTemplate: MongoTemplate,
    private val cdnConfig: CDNConfig,
    private val updateFileStatusUseCase: UpdateFileStatusUseCase
) : TaskFilePort {

    override fun loadByIds(fileIds: List<ObjectId>): List<TaskFileDto> {
        if ( fileIds.isEmpty() ) {
            return emptyList()
        }

        val query = Query(Criteria.where("_id").`in`(fileIds))
        return mongoTemplate.find(query, Document::class.java, COLLECTION_NAME)
            .map { it.toDto() }
    }

    override fun updateStatus(
        fileId: ObjectId,
        status: TaskFileStatusDto
    ): TaskFileStatusDto? {
        val query = Query(Criteria.where("_id").`is`(fileId))
        if ( !mongoTemplate.exists(query, COLLECTION_NAME) ) {
            return null
        }

        val updated = updateFileStatusUseCase.execute(
            UpdateFileStatusCommand(
                fileId = fileId.toHexString(),
                status = FileStatusDto.valueOf(status.name)
            )
        )
        return TaskFileStatusDto.valueOf(updated.status.name)
    }

    private fun Document.toDto(): TaskFileDto {
        val path = getString("path")
        val thumbnailPath = getString("thumbnailPath")
        val domain = cdnConfig.getDomain().trimEnd('/')
        return TaskFileDto(
            id = getObjectId("_id"),
            originalFileName = getString("originalFileName"),
            status = TaskFileStatusDto.valueOf(get("status").toString()),
            url = domain + path,
            thumbnailUrl = thumbnailPath?.let { domain + it }
        )
    }

    private companion object {
        const val COLLECTION_NAME = "user_files"
    }
}
