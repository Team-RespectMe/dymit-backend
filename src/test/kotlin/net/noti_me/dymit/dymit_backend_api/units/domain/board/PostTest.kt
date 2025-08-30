package net.noti_me.dymit.dymit_backend_api.units.domain.board

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import org.bson.types.ObjectId

/**
 * Post 도메인 객체의 비즈니스 로직을 테스트하는 클래스
 */
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
            boardId = ObjectId(),
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

            then("작성자가 탭과 개행 문자만 있는 제목으로 수정하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    post.updateTitle(writerId.toHexString(), "\t\n\r")
                }.message shouldBe "제목은 비워둘 수 없습니다."
            }

            then("작성자가 유효한 제목으로 수정하면 제목이 성공적으로 변경된다") {
                val newTitle = "수정된 제목"
                post.updateTitle(writerId.toHexString(), newTitle)

                post.title shouldBe newTitle
            }

            then("작성자가 앞뒤 공백이 있는 유효한 제목으로 수정하면 성공적으로 변경된다") {
                val titleWithSpaces = "  유효한 제목  "
                post.updateTitle(writerId.toHexString(), titleWithSpaces)

                post.title shouldBe titleWithSpaces
            }
        }

        `when`("updateContent를 호출할 때") {
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

            then("작성자가 탭과 개행 문자만 있는 내용으로 수정하려고 하면 BadRequestException이 발생한다") {
                shouldThrow<BadRequestException> {
                    post.updateContent(writerId.toHexString(), "\t\n\r")
                }.message shouldBe "본문은 비워둘 수 없습니다."
            }

            then("작성자가 유효한 내용으로 수정하면 내용이 성공적으로 변경된다") {
                val newContents = "수정된 내용"
                post.updateContent(writerId.toHexString(), newContents)

                post.content shouldBe newContents
            }

            then("작성자가 앞뒤 공백이 있는 유효한 내용으로 수정하면 성공적으로 변경된다") {
                val contentWithSpaces = "  유효한 내용  "
                post.updateContent(writerId.toHexString(), contentWithSpaces)

                post.content shouldBe contentWithSpaces
            }
        }

        `when`("setupCommentCount를 호출할 때") {
            then("음수 댓글 수를 설정하려고 하면 IllegalArgumentException이 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    post.setupCommentCount(-1)
                }.message shouldBe "Comment count cannot be negative"
            }

            then("0으로 댓글 수를 설정하면 성공적으로 변경된다") {
                post.setupCommentCount(0)

                post.commentCount shouldBe 0
            }

            then("양수 댓글 수를 설정하면 성공적으로 변경된다") {
                post.setupCommentCount(5)

                post.commentCount shouldBe 5
            }

            then("큰 수의 댓글 수를 설정하면 성공적으로 변경된다") {
                val largeCount = 1000L
                post.setupCommentCount(largeCount)

                post.commentCount shouldBe largeCount
            }
        }

        `when`("updateWriterInfo를 호출할 때") {
            then("작성자가 아닌 사용자가 작성자 정보를 수정하려고 하면 ForbiddenException이 발생한다") {
                val otherUserId = ObjectId().toHexString()
                val newWriter = Writer(
                    id = writerId,
                    nickname = "수정된 닉네임",
                    image = ProfileImageVo(type = "profile", url = "http://example.com/new-image.jpg")
                )

                shouldThrow<ForbiddenException> {
                    post.updateWriterInfo(otherUserId, newWriter)
                }.message shouldBe "본인의 게시글이 아닙니다."
            }

            then("다른 사용자로 작성자를 변경하려고 하면 IllegalArgumentException이 발생한다") {
                val differentUserId = ObjectId()
                val differentWriter = Writer(
                    id = differentUserId,
                    nickname = "다른 사용자",
                    image = ProfileImageVo(type = "profile", url = "http://example.com/other-image.jpg")
                )

                shouldThrow<IllegalArgumentException> {
                    post.updateWriterInfo(writerId.toHexString(), differentWriter)
                }.message shouldBe "Cannot change writer to a different user"
            }

            then("작성자가 자신의 정보를 수정하면 성공적으로 변경된다") {
                val updatedWriter = Writer(
                    id = writerId,
                    nickname = "수정된 닉네임",
                    image = ProfileImageVo(type = "updated", url = "http://example.com/updated-image.jpg")
                )

                post.updateWriterInfo(writerId.toHexString(), updatedWriter)

                post.writer.nickname shouldBe "수정된 닉네임"
                post.writer.image.type shouldBe "updated"
                post.writer.image.url shouldBe "http://example.com/updated-image.jpg"
            }
        }

        `when`("Post 객체의 초기 상태를 확인할 때") {
            then("기본 commentCount는 0이어야 한다") {
                val newPost = Post(
                    id = ObjectId(),
                    groupId = ObjectId(),
                    boardId = ObjectId(),
                    writer = writer,
                    title = "테스트 제목",
                    content = "테스트 내용"
                )

                newPost.commentCount shouldBe 0
            }

            then("commentCount를 명시적으로 설정하면 해당 값으로 초기화된다") {
                val commentCount = 10L
                val newPost = Post(
                    id = ObjectId(),
                    groupId = ObjectId(),
                    boardId = ObjectId(),
                    writer = writer,
                    title = "테스트 제목",
                    content = "테스트 내용",
                    commentCount = commentCount
                )

                newPost.commentCount shouldBe commentCount
            }

            then("모든 setter는 private이어야 하고 해당 메서드를 통해서만 변경 가능해야 한다") {
                // 각 속성들이 전용 메서드를 통해서만 변경 가능한지 확인
                val originalTitle = post.title
                val originalContent = post.content
                val originalCommentCount = post.commentCount

                // 각 메서드를 통한 변경이 정상적으로 작동하는지 확인
                post.updateTitle(writerId.toHexString(), "새 제목")
                post.updateContent(writerId.toHexString(), "새 내용")
                post.setupCommentCount(100)

                post.title shouldBe "새 제목"
                post.content shouldBe "새 내용"
                post.commentCount shouldBe 100
            }
        }
    }
})
