package com.korebit.rigel.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class StaticResourceConfig(
    @param:Value("\${image.storage.local-path:./uploads}")
    private val localStoragePath: String,
) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:$localStoragePath/")
    }
}