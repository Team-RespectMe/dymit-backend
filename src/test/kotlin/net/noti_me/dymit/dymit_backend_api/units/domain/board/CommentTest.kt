package net.noti_me.dymit.dymit_backend_api.units.domain.board

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.board.PostComment
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.ProfileImageVo
import org.bson.types.ObjectId

class CommentTest : BehaviorSpec({

    given("Comment 인스턴스가 주어졌을 때") {
        val writerId = ObjectId()
        val writer = Writer(
            id = writerId,
            nickname = "댓글작성자",
            image = ProfileImageVo(type = "profile", url = "http://example.com/image.jpg")
        )
        val comment = PostComment(
            id = ObjectId(),
            postId = ObjectId(),
            writer = writer,
            content = "원본 댓글 내용"
        )

        `when`("updateContent를 호출할 때") {
            then("작성자가 아닌 사용자가 댓글을 수정하려고 하면 ForbiddenException이 발생한다") {
                val otherUserId = ObjectId().toHexString()

                shouldThrow<ForbiddenException> {
                    comment.updateContent(otherUserId, "새로운 댓글 내용")
                }.message shouldBe "본인의 댓글이 아닙니다."
            }

            then("작성자가 빈 내용으로 수정하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    comment.updateContent(writerId.toHexString(), "")
                }.message shouldBe "댓글 본문은 비워둘 수 없습니다."
            }

            then("작성자가 공백만 있는 내용으로 수정하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    comment.updateContent(writerId.toHexString(), "   ")
                }.message shouldBe "댓글 본문은 비워둘 수 없습니다."
            }

            then("작성자가 500자를 초과하는 내용으로 수정하려고 하면 BadRequestException이 발생한다") {
                val longContent = "a".repeat(501)

                shouldThrow<BadRequestException> {
                    comment.updateContent(writerId.toHexString(), longContent)
                }.message shouldBe "댓글 본문은 최대 500자까지 작성할 수 있습니다."
            }

            then("작성자가 정확히 500자인 내용으로 수정하면 성공적으로 변경된다") {
                val maxLengthContent = "a".repeat(500)
                comment.updateContent(writerId.toHexString(), maxLengthContent)

                comment.content shouldBe maxLengthContent
            }

            then("작성자가 유효한 내용으로 수정하면 내용이 성공적으로 변경된다") {
                val newContent = "수정된 댓글 내용"
                comment.updateContent(writerId.toHexString(), newContent)

                comment.content shouldBe newContent
            }
        }
    }
})
