package net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.board

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.board.dto.StudyGroupBoardData

interface StudyGroupBoardPort {

    fun createDefaultBoard(
        groupId: String,
        boardName: String
    )

    fun loadFirstBoard(groupId: String): StudyGroupBoardData?
}
