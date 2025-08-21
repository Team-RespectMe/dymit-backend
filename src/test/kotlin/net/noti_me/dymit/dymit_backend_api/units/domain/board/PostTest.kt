package net.noti_me.dymit.dymit_backend_api.units.domain.board

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.ProfileImageVo
import org.bson.types.ObjectId

class PostTest : BehaviorSpec({

    given("Post 인스턴스가 주어졌을 때") {
        val writerId = ObjectId()
        val writer = Writer(
            id = writerId,
            nickname = "테스트작성자",
            image = ProfileImageVo(type = "profile", url = "http://example.com/image.jpg")
        )
        val post = Post(
            id = ObjectId(),
            groupId = ObjectId(),
            writer = writer,
            title = "원본 제목",
            content = "원본 내용"
        )

        `when`("updateTitle을 호출할 때") {
            then("작성자가 아닌 사용자가 제목을 수정하려고 하면 ForbiddenException이 발생한다") {
                val otherUserId = ObjectId().toHexString()

                shouldThrow<ForbiddenException> {
                    post.updateTitle(otherUserId, "새로운 제목")
                }.message shouldBe "본인의 게시글이 아닙니다."
            }

            then("작성자가 빈 제목으로 수정하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    post.updateTitle(writerId.toHexString(), "")
                }.message shouldBe "제목은 비워둘 수 없습니다."
            }

            then("작성자가 공백만 있는 제목으로 수정하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    post.updateTitle(writerId.toHexString(), "   ")
                }.message shouldBe "제목은 비워둘 수 없습니다."
            }

            then("작성자가 유효한 제목으로 수정하면 제목이 성공적으로 변경된다") {
                val newTitle = "수정된 제목"
                post.updateTitle(writerId.toHexString(), newTitle)

                post.title shouldBe newTitle
            }
        }

        `when`("updateContents를 호출할 때") {
            then("작성자가 아닌 사용자가 내용을 수정하려고 하면 ForbiddenException이 발생한다") {
                val otherUserId = ObjectId().toHexString()

                shouldThrow<ForbiddenException> {
                    post.updateContent(otherUserId, "새로운 내용")
                }.message shouldBe "본인의 게시글이 아닙니다."
            }

            then("작성자가 빈 내용으로 수정하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    post.updateContent(writerId.toHexString(), "")
                }.message shouldBe "본문은 비워둘 수 없습니다."
            }

            then("작성자가 공백만 있는 내용으로 수정하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    post.updateContent(writerId.toHexString(), "   ")
                }.message shouldBe "본문은 비워둘 수 없습니다."
            }

            then("작성자가 유효한 내용으로 수정하면 내용이 성공적으로 변경된다") {
                val newContents = "수정된 내용"
                post.updateContent(writerId.toHexString(), newContents)

                post.content shouldBe newContents
            }
        }
    }
})
