package net.noti_me.dymit.dymit_backend_api.common.errors

class ConflictException(message: String) : BusinessException(
    status = 409,
    message = message
) {
    constructor() : this("Conflict")
}