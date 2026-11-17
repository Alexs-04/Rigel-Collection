package com.korebit.rigel.integration

import com.jayway.jsonpath.JsonPath
import com.korebit.rigel.dto.request.UserStatusRequest
import com.korebit.rigel.enums.Role
import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.service.ConsumerService
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DisplayName("JWT authentication flow (H2 + MockMvc)")
class AuthFlowIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var consumerService: ConsumerService

    private fun postJson(path: String, body: String) =
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))

    @Test
    @DisplayName("Login returns an access/refresh pair that grants access to protected endpoints")
    fun loginAndAccess() {
        val tokens = loginAsRoot()

        perform(get("/product/all"), tokens).andExpect(status().isOk())
    }

    @Test
    @DisplayName("Wrong password and unknown email answer 401 with a JSON error, never 500")
    fun badCredentials() {
        postJson("/auth/login", json("email", ROOT_EMAIL, "password", "nope"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("Credenciales inválidas"))
            .andExpect(jsonPath("$.correlationId", notNullValue()))

        postJson("/auth/login", json("email", "ghost@nowhere", "password", "x"))
            .andExpect(status().isUnauthorized())
    }

    @Test
    @DisplayName("An invited user (no password yet) gets 401 instead of a 500")
    fun invitedUserWithoutPassword() {
        val invited = Consumer(
            name = "Invited",
            username = unique("inv"),
            email = "${unique("inv")}@test.local",
            role = Role.USER,
            active = false,
        )
        consumerRepository.save(invited)

        postJson("/auth/login", json("email", invited.email, "password", ""))
            .andExpect(status().isUnauthorized())
        postJson("/auth/login", json("email", invited.email, "password", "anything"))
            .andExpect(status().isUnauthorized())
    }

    @Test
    @DisplayName("Missing, malformed or refresh tokens on protected endpoints answer 401 (not 403)")
    fun unauthenticatedIs401() {
        mvc.perform(get("/product/all"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("No autorizado"))

        mvc.perform(get("/product/all").header(HttpHeaders.AUTHORIZATION, "Bearer not.a.jwt"))
            .andExpect(status().isUnauthorized())

        val tokens = loginAsRoot()
        mvc.perform(get("/product/all").header(HttpHeaders.AUTHORIZATION, "Bearer ${tokens.refresh}"))
            .andExpect(status().isUnauthorized())
    }

    @Test
    @DisplayName("Refresh rotates the pair: old refresh and old access stop working")
    fun refreshRotation() {
        val first = loginAsRoot()

        val body = postJson("/auth/refresh", json("refresh_token", first.refresh))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token", not(first.access)))
            .andReturn().response.contentAsString
        val second = Tokens(JsonPath.read(body, "$.access_token"), JsonPath.read(body, "$.refresh_token"))

        perform(get("/product/all"), second).andExpect(status().isOk())
        perform(get("/product/all"), first).andExpect(status().isUnauthorized())

        // Replaying a used refresh token is rejected
        postJson("/auth/refresh", json("refresh_token", first.refresh))
            .andExpect(status().isUnauthorized())

        // Access tokens are not accepted as refresh tokens
        postJson("/auth/refresh", json("refresh_token", second.access))
            .andExpect(status().isUnauthorized())
    }

    @Test
    @DisplayName("Logout revokes both access and refresh tokens")
    fun logout() {
        val tokens = loginAsRoot()

        perform(post("/auth/logout"), tokens).andExpect(status().isNoContent())

        perform(get("/product/all"), tokens).andExpect(status().isUnauthorized())
        postJson("/auth/refresh", json("refresh_token", tokens.refresh))
            .andExpect(status().isUnauthorized())

        // Idempotent and harmless without a token
        mvc.perform(post("/auth/logout")).andExpect(status().isNoContent())
    }

    @Test
    @DisplayName("A new login closes the previous session (single active session)")
    fun singleSession() {
        val first = loginAsRoot()
        val second = loginAsRoot()

        perform(get("/product/all"), first).andExpect(status().isUnauthorized())
        perform(get("/product/all"), second).andExpect(status().isOk())
    }

    @Test
    @DisplayName("Insufficient role answers 403 and the session stays valid")
    fun forbiddenIs403() {
        val user = login(createUser(Role.USER), "secret123")

        performJson(post("/suppliers/add"), user, json("name", "X", "contactEmail", "x@x", "phoneNumber", "1"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
        perform(get("/logs"), user).andExpect(status().isForbidden())
        perform(get("/consumer/api/users"), user).andExpect(status().isForbidden())

        perform(get("/product/all"), user).andExpect(status().isOk())
    }

    @Test
    @DisplayName("Deactivating a user kills their session immediately and blocks login")
    fun deactivationRevokesSession() {
        val email = createUser(Role.USER)
        val user = login(email, "secret123")
        val id = consumerRepository.findByEmail(email)!!.id!!

        consumerService.updateUserStatus(id, UserStatusRequest(false), ROOT_EMAIL)

        perform(get("/product/all"), user).andExpect(status().isUnauthorized())
        postJson("/auth/login", json("email", email, "password", "secret123"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Usuario inactivo. Contacta a un administrador"))
    }

    @Test
    @DisplayName("Public self-registration no longer exists (anyone could create a ROOT account)")
    fun noPublicRegistration() {
        postJson("/consumer/api/add", """{"name":"h","username":"h","password":"h","email":"h@h","role":"ROOT"}""")
            .andExpect(status().isUnauthorized())
    }

    @Test
    @DisplayName("The legacy GET /tickets/delete/{barcode} endpoint is gone")
    fun noDeleteViaGet() {
        val user = login(createUser(Role.USER), "secret123")

        perform(get("/tickets/delete/TCK-ANY"), user).andExpect(status().isNotFound())
    }
}
