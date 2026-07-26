package net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.persistence

import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.SaveStudyGroupPort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository

@Repository
class MongoSaveStudyGroupAdapter(
    private val mongoTemplate: MongoTemplate
): SaveStudyGroupPort {

    override fun persist(studyGroup: StudyGroup): StudyGroup {
        return mongoTemplate.save(studyGroup)
    }

    override fun update(studyGroup: StudyGroup): StudyGroup {
        return mongoTemplate.save(studyGroup)
    }

    override fun delete(studyGroup: StudyGroup): Boolean {
        return try {
            mongoTemplate.remove(studyGroup)
            true
        } catch (e: Exception) {
            false
        }
    }
}