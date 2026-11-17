package com.korebit.rigel.integration

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DisplayName("CORS origins")
class CorsIntegrationTest {

    @Nested
    @DisplayName("Default configuration")
    inner class Default : IntegrationTestSupport() {

        @Test
        @DisplayName("The configured frontend origin is allowed")
        fun configuredOriginAllowed() {
            preflight(mvc, CONFIGURED_ORIGIN)
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, CONFIGURED_ORIGIN))
        }

        @Test
        @DisplayName("Devices on the local network are rejected unless the option is enabled")
        fun localNetworkRejected() {
            preflight(mvc, LAN_ORIGIN).andExpect(status().isForbidden())
        }
    }

    @Nested
    @DisplayName("With app.cors.local-network.enabled=true")
    @TestPropertySource(properties = ["app.cors.local-network.enabled=true"])
    inner class LocalNetwork : IntegrationTestSupport() {

        @Test
        @DisplayName("Any host of 192.168.0.0/24 is allowed, on any port")
        fun localNetworkAllowed() {
            for (origin in listOf(LAN_ORIGIN, "http://192.168.0.7", "http://192.168.0.254:8081")) {
                preflight(mvc, origin)
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin))
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
            }
        }

        @Test
        @DisplayName("The configured frontend origin keeps working")
        fun configuredOriginAllowed() {
            preflight(mvc, CONFIGURED_ORIGIN).andExpect(status().isOk())
        }

        @Test
        @DisplayName("Origins outside the local network are still rejected")
        fun foreignOriginsRejected() {
            for (origin in listOf("http://192.168.1.25:5173", "http://10.0.0.5:5173", "https://evil.example")) {
                preflight(mvc, origin).andExpect(status().isForbidden())
            }
        }
    }

    private companion object {
        const val CONFIGURED_ORIGIN = "http://localhost:5173"
        const val LAN_ORIGIN = "http://192.168.0.25:5173"

        fun preflight(mvc: org.springframework.test.web.servlet.MockMvc, origin: String): ResultActions =
            mvc.perform(
                options("/product/all")
                    .header(HttpHeaders.ORIGIN, origin)
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
            )
    }
}
