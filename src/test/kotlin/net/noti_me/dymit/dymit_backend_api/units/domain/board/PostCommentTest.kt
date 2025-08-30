package net.noti_me.dymit.dymit_backend_api.units.domain.board

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.board.PostComment
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import org.bson.types.ObjectId

/**
 * PostComment 도메인 객체의 비즈니스 로직을 테스트하는 클래스
 */
class PostCommentTest : BehaviorSpec({

    given("PostComment 인스턴스가 주어졌을 때") {
        val writerId = ObjectId()
        val writer = Writer(
            id = writerId,
            nickname = "댓글작성자",
            image = ProfileImageVo(type = "profile", url = "http://example.com/image.jpg")
        )
        val postComment = PostComment(
            id = ObjectId(),
            postId = ObjectId(),
            writer = writer,
            content = "원본 댓글 내용"
        )

        `when`("updateContent를 호출할 때") {
            then("작성자가 아닌 사용자가 댓글을 수정하려고 하면 ForbiddenException이 발생한다") {
                val otherUserId = ObjectId().toHexString()

                shouldThrow<ForbiddenException> {
                    postComment.updateContent(otherUserId, "새로운 댓글 내용")
                }.message shouldBe "본인의 댓글이 아닙니다."
            }

            then("작성자가 빈 내용으로 수정하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    postComment.updateContent(writerId.toHexString(), "")
                }.message shouldBe "댓글 본문은 비워둘 수 없습니다."
            }

            then("작성자가 공백만 있는 내용으로 수정하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    postComment.updateContent(writerId.toHexString(), "   ")
                }.message shouldBe "댓글 본문은 비워둘 수 없습니다."
            }

            then("작성자가 탭과 개행 문자만 있는 내용으로 수정하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    postComment.updateContent(writerId.toHexString(), "\t\n\r")
                }.message shouldBe "댓글 본문은 비워둘 수 없습니다."
            }

            then("작성자가 500자를 초과하는 내용으로 수정하려고 하면 BadRequestException이 발생한다") {
                val longContent = "a".repeat(501)

                shouldThrow<BadRequestException> {
                    postComment.updateContent(writerId.toHexString(), longContent)
                }.message shouldBe "댓글 본문은 최대 500자까지 작성할 수 있습니다."
            }

            then("작성자가 정확히 500자인 내용으로 수정하면 성공적으로 변경된다") {
                val maxLengthContent = "a".repeat(500)
                postComment.updateContent(writerId.toHexString(), maxLengthContent)

                postComment.content shouldBe maxLengthContent
            }

            then("작성자가 1자인 유효한 내용으로 수정하면 성공적으로 변경된다") {
                val singleCharContent = "a"
                postComment.updateContent(writerId.toHexString(), singleCharContent)

                postComment.content shouldBe singleCharContent
            }

            then("작성자가 유효한 내용으로 수정하면 내용이 성공적으로 변경된다") {
                val newContent = "수정된 댓글 내용"
                postComment.updateContent(writerId.toHexString(), newContent)

                postComment.content shouldBe newContent
            }

            then("작성자가 앞뒤 공백이 있는 유효한 내용으로 수정하면 성공적으로 변경된다") {
                val contentWithSpaces = "  유효한 댓글 내용  "
                postComment.updateContent(writerId.toHexString(), contentWithSpaces)

                postComment.content shouldBe contentWithSpaces
            }

            then("작성자가 특수문자가 포함된 유효한 내용으로 수정하면 성공적으로 변경된다") {
                val contentWithSpecialChars = "댓글 내용! @#$%^&*()_+-=[]{}|;':\",./<>?"
                postComment.updateContent(writerId.toHexString(), contentWithSpecialChars)

                postComment.content shouldBe contentWithSpecialChars
            }

            then("작성자가 한글, 영문, 숫자가 혼합된 내용으로 수정하면 성공적으로 변경된다") {
                val mixedContent = "댓글 content 123"
                postComment.updateContent(writerId.toHexString(), mixedContent)

                postComment.content shouldBe mixedContent
            }
        }

        `when`("PostComment 객체의 초기 상태를 확인할 때") {
            then("초기 content 값이 생성자 파라미터와 동일해야 한다") {
                val initialContent = "초기 댓글 내용"
                val newPostComment = PostComment(
                    id = ObjectId(),
                    postId = ObjectId(),
                    writer = writer,
                    content = initialContent
                )

                newPostComment.content shouldBe initialContent
            }

            then("id가 자동 생성되지 않고 명시적으로 설정된 값을 사용해야 한다") {
                val customId = ObjectId()
                val newPostComment = PostComment(
                    id = customId,
                    postId = ObjectId(),
                    writer = writer,
                    content = "테스트 내용"
                )

                newPostComment.id shouldBe customId
            }

            then("postId와 writer가 생성자 파라미터와 동일해야 한다") {
                val postId = ObjectId()
                val newPostComment = PostComment(
                    id = ObjectId(),
                    postId = postId,
                    writer = writer,
                    content = "테스트 내용"
                )

                newPostComment.postId shouldBe postId
                newPostComment.writer shouldBe writer
            }

            then("content의 setter는 private이어야 하고 updateContent 메서드를 통해서만 변경 가능해야 한다") {
                // content는 private setter이므로 updateContent 메서드를 통해서만 변경 가능
                val newContent = "새로운 내용"

                postComment.updateContent(writerId.toHexString(), newContent)
                postComment.content shouldBe newContent
            }
        }

        `when`("경계값 테스트를 수행할 때") {
            then("정확히 499자인 내용으로 수정하면 성공적으로 변경된다") {
                val content499 = "a".repeat(499)
                postComment.updateContent(writerId.toHexString(), content499)

                postComment.content shouldBe content499
            }

            then("정확히 501자인 내용으로 수정하려고 하면 BadRequestException이 발생한다") {
                val content501 = "a".repeat(501)

                shouldThrow<BadRequestException> {
                    postComment.updateContent(writerId.toHexString(), content501)
                }.message shouldBe "댓글 본문은 최대 500자까지 작성할 수 있습니다."
            }

            then("빈 문자열과 null이 아닌 공백 문자들의 조합을 테스트한다") {
                // 다양한 공백 문자 조합 테스트
                val whitespaceVariations = listOf(
                    " ",
                    "\t",
                    "\n",
                    "\r",
                    " \t",
                    "\n\r",
                    " \t\n\r ",
                    "　", // 전각 공백
                )

                whitespaceVariations.forEach { whitespace ->
                    shouldThrow<BadRequestException> {
                        postComment.updateContent(writerId.toHexString(), whitespace)
                    }.message shouldBe "댓글 본문은 비워둘 수 없습니다."
                }
            }
        }

        `when`("다양한 권한 시나리오를 테스트할 때") {
            then("빈 문자열로 된 requester ID로 수정하려고 하면 ForbiddenException이 발생한다") {
                shouldThrow<ForbiddenException> {
                    postComment.updateContent("", "새로운 내용")
                }.message shouldBe "본인의 댓글이 아닙니다."
            }

            then("null이 아닌 다른 형식의 ID로 수정하려고 하면 ForbiddenException이 발생한다") {
                shouldThrow<ForbiddenException> {
                    postComment.updateContent("invalid-id", "새로운 내용")
                }.message shouldBe "본인의 댓글이 아닙니다."
            }

            then("대소문자가 다른 올바른 ID로 수정하려고 하면 ForbiddenException이 발생한다") {
                val upperCaseId = writerId.toHexString().uppercase()

                shouldThrow<ForbiddenException> {
                    postComment.updateContent(upperCaseId, "새로운 내용")
                }.message shouldBe "본인의 댓글이 아닙니다."
            }
        }
    }
})
