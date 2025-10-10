package net.noti_me.dymit.dymit_backend_api.configs

import net.noti_me.dymit.dymit_backend_api.application.auth.dto.JwtClaims
import org.ehcache.jsr107.EhcacheCachingProvider
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.jcache.JCacheCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.cache.Caching
import javax.cache.configuration.MutableConfiguration
import javax.cache.expiry.CreatedExpiryPolicy
import javax.cache.expiry.Duration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(): JCacheCacheManager {
        val cachingProvider = Caching.getCachingProvider(EhcacheCachingProvider::class.java.name)
        val cacheManager = cachingProvider.cacheManager

        // accessToken 캐시만 별도 설정
        if (cacheManager.getCache<String, JwtClaims>("accessToken") == null) {
            cacheManager.createCache("accessToken", accessTokenCacheConfig())
        }

        return JCacheCacheManager(cacheManager)
    }

    private fun accessTokenCacheConfig(): MutableConfiguration<String, JwtClaims> {
        return MutableConfiguration<String, JwtClaims>()
            .setTypes(String::class.java, JwtClaims::class.java)
            .setStoreByValue(false)
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration(TimeUnit.MINUTES, 1)))
            .setStatisticsEnabled(true)
    }
}