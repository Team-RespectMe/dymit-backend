package net.noti_me.dymit.dymit_backend_api.domain.report

import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "reports")
class Report(
    @Id
    val id: ObjectId = ObjectId(),
    val memberId: ObjectId,
    val resource: ReportedResource,
    title: String,
    content: String,
    status: ProcessStatus,
) : BaseAggregateRoot<Report>() {

    var title: String = title
        private set

    var content: String = content
        private set

    var status: ProcessStatus = status
        private set

    fun updateStatus(newStatus: ProcessStatus) {
        this.status = newStatus
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Report) return false
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Report(id=$id, memberId=$memberId, resource=$resource, title='$title', content='$content', status=$status), createdAt=$createdAt"
    }
}
