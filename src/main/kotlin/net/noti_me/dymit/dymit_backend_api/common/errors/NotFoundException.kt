package net.noti_me.dymit.dymit_backend_api.common.errors

class NotFoundException(
    message : String
) : BusinessException(status = 404, message = message) {

    constructor() : this("Not Found")
}