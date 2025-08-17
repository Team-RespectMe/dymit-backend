package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo.study_group

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupCreateCommand
import net.noti_me.dymit.dymit_backend_api.configs.MongoConfig
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.MemberPreview
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroup
import org.bson.types.ObjectId
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate

@DataMongoTest
@Import(MongoConfig::class)
class MongoLoadStudyGroupAdapterTest(
    private val mongoTemplate: MongoTemplate
) : BehaviorSpec({

    val entity = StudyGroup(
        name = "Test Study Group",
        description = "This is a test study group.",
        ownerId = ObjectId.get(),
        memberCount = 1
    )

    beforeEach {
//        mongoTemplate.save(entity)
    }

    given("새로운 스터디 그룹 생성 커맨드가 주어진다.") {
        val command = StudyGroupCreateCommand(
            name = entity.name,
            description = entity.description,
        )

        `when`("스터디 그룹을 생성하면") {
            val result = mongoTemplate.save(entity)

            then("스터디 그룹이 성공적으로 저장된다") {
                result.identifier.isEmpty() shouldNotBe true
                result.name shouldBe command.name
                result.description shouldBe command.description
                result.ownerId shouldNotBe null
                result.memberCount shouldBe 1
            }
        }
    }
})