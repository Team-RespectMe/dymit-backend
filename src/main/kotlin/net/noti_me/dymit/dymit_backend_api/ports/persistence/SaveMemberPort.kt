package net.noti_me.dymit.dymit_backend_api.ports.persistence

import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateResult
import net.noti_me.dymit.dymit_backend_api.domain.member.Member

interface SaveMemberPort {

    fun persist(member: Member): Member

    fun update(member: Member): Member

    fun delete(member: Member): Boolean
}
