package net.noti_me.dymit.dymit_backend_api.common.constraints.nickname

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Validator
import org.slf4j.LoggerFactory

class NicknameValidator : ConstraintValidator<Nickname, String> {

    private val regex = Regex("^(?! )[A-Za-z0-9가-힣]+( [A-Za-z0-9가-힣]+)*\$")

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        logger.debug("Validating nickname: $value")
        if ( value.isNullOrBlank() ) return false
        return regex.matches(value)
    }
}