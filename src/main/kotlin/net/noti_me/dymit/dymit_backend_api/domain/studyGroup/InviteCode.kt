package net.noti_me.dymit.dymit_backend_api.domain.studyGroup
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import java.time.Instant

/**
 * 스터디터디 그룹 초대 코드 도메인 모델
 * 요구사항:
 * 1. 초대 코드의 수명 관리
 * 2. 코드 생성 및 유효성 검사
 * 3. 코드 생성 시 그룹 ID와 연결
 * 4. 수동 비활성화 지원
 * 5. 코드가 만료되었는지 확인하는 메서드
 * 6. 코드로 그룹을 찾기 위한 고유 인덱스 설정
 * 이 모델은 스터디 그룹에 참여하기 위한 초대 코드를 관리합니다.
 * 초대 코드는 고유하며, 생성 시 그룹 ID와 연결됩니다. 또한,
 * 초대 코드는 만료 시간을 가지며, 만료되거나 수동으로 비활성화될 수 있습니다.
 * 초대 코드는 생성 시 고유 인덱스가 설정되어, 코드로 그룹을 찾을 수 있도록 합니다.
 * 이 모델은 MongoDB의 문서로 매핑되어 데이터베이스에 저장됩니다.
 * @param id 초대 코드의 고유 식별자 (선택적)
 * @param code 초대 코드 문자열
 */
//@Document(collection = "invite_codes")
//class InviteCode(
//    // 요구사항 6: 코드로 그룹을 찾기 위해 고유 인덱스 설정
//    id: String? = null,
//    @Indexed(unique = true)
//    val code: String,
//    // 이 코드가 속한 스터디 그룹의 ID
//    val studyGroupId: String,
//    // 요구사항 1: 수명 관리
//    val expiresAt: Instant,
//): BaseAggregateRoot<InviteCode>() {
//
//    fun isExpired(): Boolean {
//        return Instant.now().isAfter(expiresAt)
//    }
//}