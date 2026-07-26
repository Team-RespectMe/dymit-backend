package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupMemberDto
import org.bson.types.ObjectId

interface StudyScheduleGroupPort {

    fun loadByGroupId(groupId: String): StudyScheduleGroupDto?

    fun persist(group: StudyScheduleGroupDto): StudyScheduleGroupDto

    fun findMember(groupId: ObjectId, memberId: ObjectId): StudyScheduleGroupMemberDto?

    fun findMembers(groupId: ObjectId, memberIds: List<ObjectId>): List<StudyScheduleGroupMemberDto>
}
