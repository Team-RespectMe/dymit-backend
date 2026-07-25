package net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.member.domain.Member

interface SaveMemberPort {

    fun persist(member: Member): Member

    fun update(member: Member): Member

    fun delete(member: Member): Boolean
}
