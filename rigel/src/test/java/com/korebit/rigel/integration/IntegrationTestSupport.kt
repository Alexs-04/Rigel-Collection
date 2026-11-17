package com.korebit.rigel.integration

import com.jayway.jsonpath.JsonPath
import com.korebit.rigel.enums.Role
import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.repository.ConsumerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

/**
 * Boots the full application against the in-memory H2 database (`test` profile)
 * and drives it through MockMvc, including the real security filter chain.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationTestSupport {

    @Autowired
    protected lateinit var mvc: MockMvc

    @Autowired
    protected lateinit var consumerRepository: ConsumerRepository

    @Autowired
    protected lateinit var passwordEncoder: PasswordEncoder

    /** No real emails are sent from tests. */
    @MockitoBean
    protected lateinit var mailSender: JavaMailSender

    data class Tokens(val access: String, val refresh: String) {
        val bearer: String get() = "Bearer $access"
    }

    protected fun login(email: String, password: String): Tokens {
        val body = mvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("email", email, "password", password))
        )
            .andExpect(status().isOk())
            .andReturn().response.contentAsString
        return Tokens(JsonPath.read(body, "$.access_token"), JsonPath.read(body, "$.refresh_token"))
    }

    protected fun loginAsRoot(): Tokens = login(ROOT_EMAIL, ROOT_PASSWORD)

    /** Persists an active user directly and returns its email. Password is "secret123". */
    protected fun createUser(role: Role): String {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        val consumer = Consumer(
            name = "User $suffix",
            username = "u$suffix",
            email = "u$suffix@test.local",
            password = passwordEncoder.encode("secret123"),
            role = role,
            active = true,
        )
        consumerRepository.save(consumer)
        return consumer.email
    }

    protected fun perform(request: MockHttpServletRequestBuilder, tokens: Tokens): ResultActions =
        mvc.perform(request.header(HttpHeaders.AUTHORIZATION, tokens.bearer))

    protected fun performJson(request: MockHttpServletRequestBuilder, tokens: Tokens, body: String): ResultActions =
        perform(request.contentType(MediaType.APPLICATION_JSON).content(body), tokens)

    companion object {
        const val ROOT_EMAIL = "root@root"
        const val ROOT_PASSWORD = "root"

        fun unique(prefix: String): String = "$prefix-${UUID.randomUUID().toString().substring(0, 8)}"

        /** Minimal JSON object builder for flat string payloads: json("k1", "v1", "k2", "v2"). */
        fun json(vararg keyValues: String?): String =
            keyValues.toList().chunked(2).joinToString(separator = ",", prefix = "{", postfix = "}") { (key, value) ->
                val encoded = if (value == null) "null" else "\"${value.replace("\"", "\\\"")}\""
                "\"$key\":$encoded"
            }
    }
}
