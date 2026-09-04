package net.noti_me.dymit.dymit_backend_api.units.board

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.dto.BoardDto
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.dto.CommentDto
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.dto.PostDto
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.dto.PostDtoV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto.BoardResponse
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto.CommentCommandResponse
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto.CommentListItem
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto.PostDetailResponse
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto.PostListItem
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto.v2.PostDetailResponseV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto.v2.PostListItemV2
import net.noti_me.dymit.dymit_backend_api.board.domain.Board
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardProfileImageType
import net.noti_me.dymit.dymit_backend_api.board.domain.PostCategory
import net.noti_me.dymit.dymit_backend_api.board.domain.PostComment
import net.noti_me.dymit.dymit_backend_api.board.domain.Writer
import org.bson.types.ObjectId
import java.time.Instant

internal class Task99BoardCommentTimestampConversionTest : BehaviorSpec({

    val createdAt = Instant.parse("2026-08-01T14:37:12.345678Z")
    val writer = Writer.of(
        id = ObjectId("507f1f77bcf86cd799439011"),
        nickname = "tester",
        imageType = BoardProfileImageType.PRESET,
        imageUrl = "https://example.com/profile.png"
    )

    Given("a board DTO with a fixed Instant creation time") {
        When("it is converted to a board response") {
            Then("the exact Instant is preserved") {
                val dto = BoardDto(
                    id = "507f1f77bcf86cd799439012",
                    groupId = "507f1f77bcf86cd799439013",
                    name = "공지",
                    createdAt = createdAt,
                    permissions = mutableListOf()
                )

                BoardResponse.from(dto).createdAt shouldBe createdAt
            }
        }
    }

    Given("a post comment with a fixed Instant creation time") {
        When("it is converted to the comment DTO and responses") {
            Then("the exact Instant is preserved through all three board comment boundaries") {
                val entity = PostComment(
                    id = ObjectId("507f1f77bcf86cd799439014"),
                    postId = ObjectId("507f1f77bcf86cd799439015"),
                    writer = writer,
                    content = "댓글",
                    createdAt = createdAt
                )

                val dto = CommentDto.from(entity)

                dto.createdAt shouldBe createdAt
                CommentCommandResponse.from(dto).createdAt shouldBe createdAt
                CommentListItem.from(dto).createdAt shouldBe createdAt
            }
        }

        When("the source timestamp is null") {
            Then("null is preserved by both nullable response conversions") {
                val dto = CommentDto(
                    id = "507f1f77bcf86cd799439016",
                    postId = "507f1f77bcf86cd799439017",
                    writer = writer,
                    content = "댓글",
                    createdAt = null
                )

                CommentCommandResponse.from(dto).createdAt shouldBe null
                CommentListItem.from(dto).createdAt shouldBe null
            }
        }
    }

    Given("a V1 post DTO with a fixed Instant creation time") {
        When("it is converted to post list and detail responses") {
            Then("both response mappings preserve the exact Instant") {
                val dto = PostDto(
                    id = "507f1f77bcf86cd799439018",
                    groupId = "507f1f77bcf86cd799439019",
                    boardId = "507f1f77bcf86cd799439020",
                    writer = writer,
                    title = "게시글",
                    content = "본문",
                    commentCount = 2,
                    createdAt = createdAt
                )

                PostListItem.from(dto).createdAt shouldBe createdAt
                PostDetailResponse.from(dto).createdAt shouldBe createdAt
            }
        }
    }

    Given("a V2 post DTO with a fixed Instant creation time") {
        When("it is converted to post list and detail responses") {
            Then("both response mappings preserve the exact Instant") {
                val dto = PostDtoV2(
                    id = "507f1f77bcf86cd799439021",
                    groupId = "507f1f77bcf86cd799439022",
                    boardId = "507f1f77bcf86cd799439023",
                    writer = writer,
                    title = "게시글",
                    content = "본문",
                    category = PostCategory.QUESTION,
                    scheduleId = null,
                    commentCount = 2,
                    createdAt = createdAt
                )

                PostListItemV2.from(dto).createdAt shouldBe createdAt
                PostDetailResponseV2.from(dto).createdAt shouldBe createdAt
            }
        }
    }
})
