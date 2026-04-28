package net.noti_me.dymit.dymit_backend_api.domain.study_group

import org.bson.types.ObjectId

class BlackList(
    val memberId: ObjectId,
    val nickname: String,
    val reason: String
) {

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if( this === other) return true
        if( this::class != other::class) return false
        other as BlackList
        return this.memberId == other.memberId
    }

    override fun hashCode(): Int {
        return memberId.hashCode()
    }
}