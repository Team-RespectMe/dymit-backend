package net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.member

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupProfileImageType as ProfileImageType
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import java.time.Instant
import java.time.ZoneId
import java.util.Date

internal class MemberPersistenceAdapterTest : BehaviorSpec() {

    private val mongoTemplate = mockk<MongoTemplate>()
    private val adapter = MemberPersistenceAdapter(mongoTemplate)

    init {
        given("a member stored in the members collection") {
            `when`("the study-group adapter loads it") {
                then("it maps only study-group-owned port DTO fields") {
                    val id = ObjectId.get()
                    val createdAt = Instant.parse("2026-07-26T09:00:00Z")
                    val member = Document()
                        .append("_id", id)
                        .append("nickname", "member")
                        .append(
                            "profileImage",
                            Document()
                                .append("type", ProfileImageType.EXTERNAL.name)
                                .append("thumbnail", "thumbnail")
                                .append("original", "original")
                        )
                        .append("roles", listOf("ROLE_MEMBER"))
                        .append(
                            "createdAt",
                            Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant())
                        )
                    every {
                        mongoTemplate.findById(id, Document::class.java, "members")
                    } returns member

                    val result = adapter.loadById(id.toHexString())

                    verify(exactly = 1) {
                        mongoTemplate.findById(id, Document::class.java, "members")
                    }
                    result!!.id shouldBe id.toHexString()
                    result.nickname shouldBe "member"
                    result.profileImageType shouldBe ProfileImageType.EXTERNAL
                    result.profileImageThumbnail shouldBe "thumbnail"
                    result.profileImageOriginal shouldBe "original"
                    result.roles shouldBe listOf("ROLE_MEMBER")
                    result.createdAt shouldBe createdAt
                }
            }
        }
    }
}
