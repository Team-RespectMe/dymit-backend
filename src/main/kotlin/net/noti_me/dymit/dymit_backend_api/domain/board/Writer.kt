package net.noti_me.dymit.dymit_backend_api.domain.board

import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.ProfileImageVo
import org.bson.types.ObjectId

class Writer(
    val id: ObjectId,
    val nickname: String,
    val image: ProfileImageVo
) {
}