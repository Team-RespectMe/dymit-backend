package net.noti_me.dymit.dymit_backend_api.domain.report

data class ReportedResource(
    val resourceType: ResourceType,
    val resourceId: String
)
