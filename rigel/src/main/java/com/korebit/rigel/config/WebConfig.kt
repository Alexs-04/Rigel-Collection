package com.korebit.rigel.config

import com.korebit.rigel.interceptor.LogInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * CORS and request-logging setup.
 *
 * The frontend origin configured in `origin` is always allowed. Setting
 * `app.cors.local-network.enabled=true` additionally accepts any browser origin that matches
 * `app.cors.local-network.patterns` (by default every host of the `192.168.0.0/24` LAN, on any
 * port), so the app can be opened from other devices on the same network.
 */
@Configuration
class WebConfig(
    private val logInterceptor: LogInterceptor,
    @param:Value("\${origin}")
    private val origin: String,
    @param:Value("\${app.cors.local-network.enabled:false}")
    private val localNetworkEnabled: Boolean,
    @param:Value("\${app.cors.local-network.patterns:$DEFAULT_LOCAL_NETWORK_PATTERN}")
    private val localNetworkPatterns: List<String>,
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        val registration = registry.addMapping("/**")
            .allowedOrigins(origin)
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("Authorization", "X-Correlation-Id")
            .allowCredentials(true)

        if (localNetworkEnabled) {
            val patterns = localNetworkPatterns.map(String::trim).filter(String::isNotEmpty)
            registration.allowedOriginPatterns(*patterns.toTypedArray())
        }
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(logInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/error",
                "/favicon.ico",
                "/actuator/**",
                "/**/*.css",
                "/**/*.js",
                "/**/*.map",
                "/**/*.ico",
                "/**/*.png",
                "/**/*.jpg",
                "/**/*.svg",
            )
    }

    companion object {
        /** Any host of the 192.168.0.0/24 network, any port. */
        const val DEFAULT_LOCAL_NETWORK_PATTERN = "http://192.168.0.*:[*]"
    }
}
