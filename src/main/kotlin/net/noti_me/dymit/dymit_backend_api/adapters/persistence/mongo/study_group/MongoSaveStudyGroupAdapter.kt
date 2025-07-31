package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_group

import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroup
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
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