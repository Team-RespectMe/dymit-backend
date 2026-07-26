package net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file

import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto.TaskFileDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto.TaskFileStatusDto
import org.bson.types.ObjectId

/**
 * 과제 모듈에서 필요한 파일 정보를 제공하는 출력 포트입니다.
 */
interface TaskFilePort {

    /**
     * 식별자 목록에 해당하는 파일을 조회합니다.
     *
     * @param fileIds 조회할 파일 식별자 목록
     * @return 과제 모듈 전용 파일 DTO 목록
     */
    fun loadByIds(fileIds: List<ObjectId>): List<TaskFileDto>

    /**
     * 파일 상태를 변경합니다.
     *
     * @param fileId 변경할 파일 식별자
     * @param status 목표 상태
     * @return 변경된 상태, 파일이 없으면 null
     */
    fun updateStatus(fileId: ObjectId, status: TaskFileStatusDto): TaskFileStatusDto?
}
