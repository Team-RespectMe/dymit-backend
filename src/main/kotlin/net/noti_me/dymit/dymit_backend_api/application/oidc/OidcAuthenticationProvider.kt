package net.noti_me.dymit.dymit_backend_api.application.oidc

import net.noti_me.dymit.dymit_backend_api.application.oidc.idToken.CommonOidcIdTokenPayload

interface OidcAuthenticationProvider {

    fun getPayload(idToken: String): CommonOidcIdTokenPayload

    fun support(providerName: String): Boolean
}