package net.noti_me.dymit.dymit_backend_api.units.server_notice.domain

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.server_notice.domain.ServerNoticeWriter
import net.noti_me.dymit.dymit_backend_api.server_notice.domain.ServerNoticeRequester
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.member.dto.ServerNoticeMemberDto
import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.server_notice.domain.ServerNotice
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import org.bson.types.ObjectId

internal class ServerNoticeTest : BehaviorSpec({

    val member = createMemberEntity(
        roles = listOf(MemberRole.ROLE_MEMBER)
    )
    val admin = createMemberEntity(
        roles = listOf(MemberRole.ROLE_ADMIN)
    )
    val memberRequester = member.toServerNoticeRequester()
    val adminRequester = admin.toServerNoticeRequester()

    var serverNotice: ServerNotice? = null

    beforeEach {
        serverNotice = ServerNotice.create(
            writer = adminRequester,
            category = "일반",
            title = "초기 제목",
            content = "초기 내용"
        )
    }

    given("ServerNotice 객체를 생성할 필드가 주어진다.") {
        val title = "공지사항 제목"
        val content = "공지사항 본문 내용"

        `when`("Admin 권한이 있는 사용자가 공지사항을 생성하면") {
           then("ServerNotice 객체가 정상적으로 생성된다.") {
               val serverNotice = shouldNotThrowAny {
                   ServerNotice.create(
                       writer = adminRequester,
                       category = "일반",
                       title = title,
                       content = content
                   )
               }

               serverNotice.writer.id shouldBe admin.id!!
               serverNotice.title shouldBe title
               serverNotice.content shouldBe content
           }
        }

        `when`("Admin 권한이 없는 사용자가 공지사항을 생성하면") {
            then("예외가 발생한다.") {
                shouldThrow< ForbiddenException > {
                    ServerNotice.create(
                        writer = memberRequester,
                        category = "일반",
                        title = title,
                        content = content
                    )
                }
            }
        }
    }

    given("수정할 본문 내용이 주어진다.") {
        val newContent = "수정된 공지사항 본문 내용"

        `when`("관리자가 공지사항의 본문을 수정하면") {
           then("본문 내용이 정상적으로 수정된다.") {
               serverNotice?.updateContent(adminRequester, newContent)
               serverNotice?.content shouldBe newContent
           }
        }

        `when`("비관리자가 공지사항의 본문을 수정하면") {
           then("예외가 발생한다.") {
                shouldThrow< ForbiddenException > {
                     serverNotice?.updateContent(memberRequester, newContent)
                }
           }
        }
    }

    given("수정할 제목이 주어진다.") {
        val newTitle = "수정된 공지사항 제목"
        `when`("관리자가 공지사항의 제목을 수정하면") {
            then("제목이 정상적으로 수정된다.") {
                serverNotice?.updateTitle(adminRequester, newTitle)
                serverNotice?.title shouldBe newTitle
            }
        }

        `when`("비관리자가 공지사항의 제목을 수정하면") {
            then("예외가 발생한다.") {
                shouldThrow< ForbiddenException > {
                    serverNotice?.updateTitle(memberRequester, newTitle)
                }
            }
        }
    }
}) {

    companion object {
        fun createServerNotice(
            id: ObjectId? = null,
            writer: Member = createMemberEntity(roles = listOf(MemberRole.ROLE_ADMIN)),
            category: String = "일반",
            title: String = "테스트 공지사항 제목",
            content: String = "테스트 공지사항 내용"
        ): ServerNotice {
            val notice = if ( id == null ) {
                ServerNotice.create(
                    writer = writer.toServerNoticeRequester(),
                    category = category,
                    title = title,
                    content = content
                )
            } else {
                ServerNotice(
                    id = id,
                    category = category,
                    writer = ServerNoticeWriter.of(
                        id = writer.id!!,
                        nickname = writer.nickname,
                        imageType = writer.profileImage.type,
                        imageUrl = writer.profileImage.thumbnail
                    ),
                    title = title,
                    content = content,
                )
            }
            return notice
        }

        fun createServerNoticeMemberDto(member: Member): ServerNoticeMemberDto {
            return ServerNoticeMemberDto(
                id = member.id!!,
                nickname = member.nickname,
                imageType = member.profileImage.type,
                imageUrl = member.profileImage.thumbnail,
                admin = member.roles.contains(MemberRole.ROLE_ADMIN)
            )
        }
    }
}

private fun Member.toServerNoticeRequester(): ServerNoticeRequester {
    return ServerNoticeRequester(
        id = id!!,
        nickname = nickname,
        imageType = profileImage.type,
        imageUrl = profileImage.thumbnail,
        admin = roles.contains(MemberRole.ROLE_ADMIN)
    )
}
