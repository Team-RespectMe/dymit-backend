package net.noti_me.dymit.dymit_backend_api.ports.persistence.member

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import org.bson.types.ObjectId

/**
 * 멤버 조회 포트
 * 메서드 이름 규칙
 * - 엔티티를 불러오는 경우 load... 형식으로 작성
 * - 쿼리 모델을 조회하는 경우 find... 형식으로 작성
 */
interface LoadMemberPort {

    fun loadById(id: String): Member?

    fun loadById(id: ObjectId): Member?

    fun loadByOidcIdentity(
        oidcIdentity: OidcIdentity
    ): Member?

    fun loadByIds(
        ids: List<String>
    ): List<Member>

    fun existsByNickname(
        nickname: String
    ): Boolean

    fun loadByDeviceToken(
        deviceToken: String
    ): Member?

}
