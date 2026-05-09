package net.noti_me.dymit.dymit_backend_api.ports.persistence.file

import net.noti_me.dymit.dymit_backend_api.domain.file.UserFile
import org.bson.types.ObjectId

/**
 * 사용자 파일 메타데이터 영속성 포트입니다.
 */
interface UserFileRepository {

    /**
     * 사용자 파일 문서를 저장합니다.
     *
     * @param userFile 저장할 파일 문서
     * @return 저장된 파일 문서
     */
    fun save(userFile: UserFile): UserFile

    /**
     * 파일 ID로 사용자 파일 문서를 조회합니다.
     *
     * @param fileId 조회할 파일 ID
     * @return 조회된 파일 문서, 없으면 null
     */
    fun findById(fileId: String): UserFile?

    /**
     * 파일 ID 목록으로 사용자 파일 문서를 조회합니다.
     *
     * @param fileIds 조회할 파일 ID 목록
     * @return 조회된 파일 문서 목록
     */
    fun findByIds(fileIds: List<ObjectId>): List<UserFile>
}
