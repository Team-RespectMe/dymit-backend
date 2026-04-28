package net.noti_me.dymit.dymit_backend_api.domain.study_group

enum class GroupPresetImage(
    val thumbnail: String,
    val original: String
) {
    BOOK(
        thumbnail = "https://d380gc0prbxdbr.cloudfront.net/static/presets/study-groups/book_64x64.png",
        original = "https://d380gc0prbxdbr.cloudfront.net/static/presets/study-groups/book_512x512.png"
    ),
    COFFEE(
        thumbnail = "https://d380gc0prbxdbr.cloudfront.net/static/presets/study-groups/coffee_64x64.png",
        original = "https://d380gc0prbxdbr.cloudfront.net/static/presets/study-groups/coffee_512x512.png"
    ),
    ROOM(
        thumbnail = "https://d380gc0prbxdbr.cloudfront.net/static/presets/study-groups/room_64x64.png",
        original = "https://d380gc0prbxdbr.cloudfront.net/static/presets/study-groups/room_512x512.png"
    ),
    STUDY(
        thumbnail = "https://d380gc0prbxdbr.cloudfront.net/static/presets/study-groups/study_64x64.png",
        original = "https://d380gc0prbxdbr.cloudfront.net/static/presets/study-groups/study_512x512.png"
    ),
    WORK(
        thumbnail = "https://d380gc0prbxdbr.cloudfront.net/static/presets/study-groups/work_64x64.png",
        original = "https://d380gc0prbxdbr.cloudfront.net/static/presets/study-groups/work_512x512.png"
    )
}


