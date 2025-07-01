package net.noti_me.dymit.dymit_backend_api.application.auth.oidc

import java.security.interfaces.RSAPublicKey

interface JwksProvider {

    /**
     * kid 를 이용하여 OIDC Provider의 공개키를 가져옵니다.
     * @param kid 키 ID (Key ID)
     * @return OIDC Provider의 공개키
     */
    fun getPublicKey(kid: String): RSAPublicKey

    fun isSupported(provider: String): Boolean
}