package net.noti_me.dymit.dymit_backend_api.common.logging.discord

data class Embed(
    val title: String,
    val description: String
)

data class DiscordMessageDto(
    val content: String,
    val embeds : List<Embed> = emptyList()
) {

}