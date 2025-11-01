package net.noti_me.dymit.dymit_backend_api.domain.member

enum class MemberPresetImage(
    val thumbnail: String,
    val original: String
) {
    CHECK(
        original= "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/check_512x512.png",
        thumbnail= "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/check_64x64.png"
    ),
    GROW(
        original = "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/grow_512x512.png",
        thumbnail ="https://d380gc0prbxdbr.cloudfront.net/static/presets/members/grow_64x64.png"
    ),
    KICK(
        original = "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/kick_512x512.png",
        thumbnail = "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/kick_64x64.png"
    ),
    MEET(
        original = "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/meet_512x512.png",
        thumbnail = "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/meet_64x64.png"
    ),
    PROJECT(
        original = "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/project_512x512.png",
        thumbnail = "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/project_64x64.png"
    ),
    STACK(
        original = "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/stack_512x512.png",
        thumbnail = "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/stack_64x64.png"
    )
}

