package net.noti_me.dymit.dymit_backend_api.units.domain.study_schedule

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleComment
import org.bson.types.ObjectId

class ScheduleCommentTest : BehaviorSpec({

    Given("ScheduleComment 객체가 생성될 때") {
        val scheduleId = ObjectId.get()
        val writerId = ObjectId.get()
        val writer = Writer(
            id = writerId,
            nickname = "testWriter",
            image = ProfileImageVo(type = "preset", url = "0")
        )
        val scheduleComment = ScheduleComment(
            scheduleId = scheduleId,
            writer = writer,
            content = "테스트 댓글 내용"
        )

        When("기본 생성자로 생성하면") {
            Then("모든 속성이 올바르게 설정된다") {
                scheduleComment.scheduleId shouldBe scheduleId
                scheduleComment.writer shouldBe writer
                scheduleComment.content shouldBe "테스트 댓글 내용"
                scheduleComment.id.toString() shouldHaveLength 24 // ObjectId는 24자리 hex string
            }
        }

        When("identifier 속성에 접근하면") {
            Then("id의 hex string 값을 반환한다") {
                scheduleComment.identifier shouldBe scheduleComment.id.toHexString()
                scheduleComment.identifier shouldHaveLength 24
            }
        }
    }

    Given("댓글 내용 수정 시나리오에서") {
        val scheduleId = ObjectId.get()
        val writerId = ObjectId.get()
        val writer = Writer(
            id = writerId,
            nickname = "testWriter",
            image = ProfileImageVo(type = "preset", url = "0")
        )

        val requester = StudyGroupMember(
            id = writerId, // 작성자와 동일한 ID로 설정
            groupId = ObjectId.get(),
            memberId = writerId,
            nickname = "testWriter",
            profileImage = MemberProfileImageVo(type = "preset", url = "0"),
            role = GroupMemberRole.MEMBER
        )

        val anotherMember = StudyGroupMember(
            id = ObjectId.get(), // 다른 ID
            groupId = ObjectId.get(),
            memberId = ObjectId.get(),
            nickname = "anotherMember",
            profileImage = MemberProfileImageVo(type = "preset", url = "1"),
            role = GroupMemberRole.MEMBER
        )

        When("작성자가 댓글 내용을 수정하면") {
            val scheduleComment = ScheduleComment(
                scheduleId = scheduleId,
                writer = writer,
                content = "테스트 댓글 내용"
            )
            val newContent = "수정된 댓글 내용"

            Then("댓글 내용이 성공적으로 변경된다") {
                scheduleComment.updateContent(requester, newContent)
                scheduleComment.content shouldBe newContent
            }
        }

        When("작성자가 아닌 사용자가 댓글 내용을 수정하려고 하면") {
            val scheduleComment = ScheduleComment(
                scheduleId = scheduleId,
                writer = writer,
                content = "테스트 댓글 내용"
            )
            val newContent = "수정된 댓글 내용"

            Then("ForbiddenException이 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    scheduleComment.updateContent(anotherMember, newContent)
                }
                exception.message shouldBe "댓글 작성자만 댓글을 수정할 수 있습니다."
            }
        }

        When("500자를 초과하는 내용으로 수정하려고 하면") {
            val scheduleComment = ScheduleComment(
                scheduleId = scheduleId,
                writer = writer,
                content = "테스트 댓글 내용"
            )
            val longContent = "a".repeat(501) // 501자 문자열

            Then("IllegalArgumentException이 발생한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    scheduleComment.updateContent(requester, longContent)
                }
                exception.message shouldBe "댓글 내용은 500자 이내로 작성해야 합니다."
            }
        }

        When("정확히 500자인 내용으로 수정하면") {
            val scheduleComment = ScheduleComment(
                scheduleId = scheduleId,
                writer = writer,
                content = "테스트 댓글 내용"
            )
            val maxContent = "a".repeat(500) // 500자 문자열

            Then("정상적으로 수정된다") {
                scheduleComment.updateContent(requester, maxContent)
                scheduleComment.content shouldBe maxContent
                scheduleComment.content shouldHaveLength 500
            }
        }

        When("빈 문자열로 수정하면") {
            val scheduleComment = ScheduleComment(
                scheduleId = scheduleId,
                writer = writer,
                content = "테스트 댓글 내용"
            )
            val emptyContent = ""

            Then("정상적으로 수정된다") {
                scheduleComment.updateContent(requester, emptyContent)
                scheduleComment.content shouldBe emptyContent
            }
        }
    }

    Given("경계값 테스트에서") {
        val scheduleId = ObjectId.get()
        val writerId = ObjectId.get()
        val writer = Writer(
            id = writerId,
            nickname = "testWriter",
            image = ProfileImageVo(type = "preset", url = "0")
        )

        val requester = StudyGroupMember(
            id = writerId, // 작성자와 동일한 ID
            groupId = ObjectId.get(),
            memberId = writerId,
            nickname = "testWriter",
            profileImage = MemberProfileImageVo(type = "preset", url = "0"),
            role = GroupMemberRole.MEMBER
        )

        When("499자 내용으로 수정하면") {
            val scheduleComment = ScheduleComment(
                scheduleId = scheduleId,
                writer = writer,
                content = "테스트 댓글 내용"
            )
            val content499 = "a".repeat(499)

            Then("정상적으로 수정된다") {
                scheduleComment.updateContent(requester, content499)
                scheduleComment.content shouldBe content499
                scheduleComment.content shouldHaveLength 499
            }
        }

        When("501자 내용으로 수정하면") {
            val scheduleComment = ScheduleComment(
                scheduleId = scheduleId,
                writer = writer,
                content = "테스트 댓글 내용"
            )
            val content501 = "a".repeat(501)

            Then("IllegalArgumentException이 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    scheduleComment.updateContent(requester, content501)
                }
            }
        }
    }

    Given("동일성 검증에서") {
        val scheduleId = ObjectId.get()
        val writerId = ObjectId.get()
        val writer = Writer(
            id = writerId,
            nickname = "testWriter",
            image = ProfileImageVo(type = "preset", url = "0")
        )

        When("작성자 ID가 동일한 경우") {
            val scheduleComment = ScheduleComment(
                scheduleId = scheduleId,
                writer = writer,
                content = "테스트 댓글 내용"
            )

            Then("댓글 수정이 허용된다") {
                val sameIdMember = StudyGroupMember(
                    id = writerId, // Writer.id와 동일한 ID
                    groupId = ObjectId.get(),
                    memberId = writerId,
                    nickname = "differentNickname",
                    profileImage = MemberProfileImageVo(type = "preset", url = "2"),
                    role = GroupMemberRole.MEMBER
                )

                val newContent = "동일 ID로 수정"
                scheduleComment.updateContent(sameIdMember, newContent)
                scheduleComment.content shouldBe newContent
            }
        }

        When("작성자 ID가 다른 경우") {
            val scheduleComment = ScheduleComment(
                scheduleId = scheduleId,
                writer = writer,
                content = "테스트 댓글 내용"
            )

            Then("댓글 수정이 거부된다") {
                val differentIdMember = StudyGroupMember(
                    id = ObjectId.get(), // Writer.id와 다른 ID
                    groupId = ObjectId.get(),
                    memberId = ObjectId.get(),
                    nickname = "testWriter", // 같은 닉네임이어도
                    profileImage = MemberProfileImageVo(type = "preset", url = "3"),
                    role = GroupMemberRole.MEMBER
                )

                shouldThrow<ForbiddenException> {
                    scheduleComment.updateContent(differentIdMember, "수정 시도")
                }
            }
        }
    }
})
