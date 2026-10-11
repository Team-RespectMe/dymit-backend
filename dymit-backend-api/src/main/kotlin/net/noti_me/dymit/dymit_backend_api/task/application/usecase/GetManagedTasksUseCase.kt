package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetManagedTasksQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.ManagedTaskDto

/** 소유 그룹의 오늘 마감 과제 목록 조회 유즈케이스입니다. */
interface GetManagedTasksUseCase {

    /** 소유 그룹에 등록된 기준일 마감 과제를 커서 순서로 조회합니다. */
    fun execute(query: GetManagedTasksQuery): List<ManagedTaskDto>
}
