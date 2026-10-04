package net.noti_me.dymit.dymit_backend_api.common

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.AbstractAggregateRoot
import org.springframework.data.domain.DomainEvents
import org.springframework.data.domain.Persistable
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

/**
 * MongoDB에 저장되는 aggregate root의 공통 감사 및 삭제 정보를 제공합니다.
 *
 * @param id 문서 식별자
 * @param createdAt 생성 시각
 * @param updatedAt 마지막 변경 시각
 * @param isDeleted 논리 삭제 여부
 * @param deletedAt 논리 삭제 시각
 */
@Document
abstract class BaseAggregateRoot<T : AbstractAggregateRoot<T>>(
    @Id
    @Indexed(unique = true)
    val id: ObjectId? = null,
    createdAt: Instant? = Instant.now(),
    updatedAt: Instant? = Instant.now(),
    isDeleted: Boolean = false,
    deletedAt: Instant? = null
) : AbstractAggregateRoot<T>() {

    val identifier: String
        get() = id?.toHexString() ?: throw IllegalStateException("Entity ID is null")

    @CreatedDate
    var createdAt: Instant? = createdAt ?: Instant.now()
        protected set

    @LastModifiedDate
    var updatedAt: Instant? = updatedAt ?: Instant.now()
        protected set

    var isDeleted: Boolean = isDeleted
        protected set

    var deletedAt: Instant? = deletedAt
        protected set

    @Transient
    protected var modified: Boolean = false

    public fun isModified(): Boolean {
        return modified
    }

    @DomainEvents
    public fun listDomainEvents(): Collection<Any> {
        return super.domainEvents();
    }

    public override fun clearDomainEvents() {
        super.clearDomainEvents()
    }

    /**
     * aggregate를 현재 시각에 논리 삭제합니다.
     */
    open fun markAsDeleted() {
        if (isDeleted) {
            return
        }
        isDeleted = true
        deletedAt = Instant.now()
        modified = true
    }   
}
