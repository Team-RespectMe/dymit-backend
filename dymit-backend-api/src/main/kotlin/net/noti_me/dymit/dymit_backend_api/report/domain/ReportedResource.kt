package net.noti_me.dymit.dymit_backend_api.report.domain

data class ReportedResource(
    val resourceType: ResourceType,
    val resourceId: String
)
