package com.korebit.rigel.integration;

import com.korebit.rigel.dto.request.UserStatusRequest;
import com.korebit.rigel.enums.Role;
import com.korebit.rigel.model.beans.Consumer;
import com.korebit.rigel.service.ConsumerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("JWT authentication flow (H2 + MockMvc)")
class AuthFlowIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ConsumerService consumerService;

    @Test
    @DisplayName("Login returns an access/refresh pair that grants access to protected endpoints")
    void loginAndAccess() throws Exception {
        Tokens tokens = loginAsRoot();

        perform(get("/product/all"), tokens).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Wrong password and unknown email answer 401 with a JSON error, never 500")
    void badCredentials() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("email", ROOT_EMAIL, "password", "nope")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"))
                .andExpect(jsonPath("$.correlationId", notNullValue()));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("email", "ghost@nowhere", "password", "x")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("An invited user (no password yet) gets 401 instead of a 500")
    void invitedUserWithoutPassword() throws Exception {
        Consumer invited = new Consumer();
        invited.setName("Invited");
        invited.setUsername(unique("inv"));
        invited.setEmail(unique("inv") + "@test.local");
        invited.setRole(Role.USER);
        invited.setActive(false);
        consumerRepository.save(invited);

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("email", invited.getEmail(), "password", "")))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("email", invited.getEmail(), "password", "anything")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Missing, malformed or refresh tokens on protected endpoints answer 401 (not 403)")
    void unauthenticatedIs401() throws Exception {
        mvc.perform(get("/product/all"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autorizado"));

        mvc.perform(get("/product/all").header(HttpHeaders.AUTHORIZATION, "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());

        Tokens tokens = loginAsRoot();
        mvc.perform(get("/product/all").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.refresh()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh rotates the pair: old refresh and old access stop working")
    void refreshRotation() throws Exception {
        Tokens first = loginAsRoot();

        String body = mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json("refresh_token", first.refresh())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", not(first.access())))
                .andReturn().getResponse().getContentAsString();
        Tokens second = new Tokens(
                com.jayway.jsonpath.JsonPath.read(body, "$.access_token"),
                com.jayway.jsonpath.JsonPath.read(body, "$.refresh_token"));

        perform(get("/product/all"), second).andExpect(status().isOk());
        perform(get("/product/all"), first).andExpect(status().isUnauthorized());

        // Replaying a used refresh token is rejected
        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json("refresh_token", first.refresh())))
                .andExpect(status().isUnauthorized());

        // Access tokens are not accepted as refresh tokens
        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json("refresh_token", second.access())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Logout revokes both access and refresh tokens")
    void logout() throws Exception {
        Tokens tokens = loginAsRoot();

        perform(post("/auth/logout"), tokens).andExpect(status().isNoContent());

        perform(get("/product/all"), tokens).andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json("refresh_token", tokens.refresh())))
                .andExpect(status().isUnauthorized());

        // Idempotent and harmless without a token
        mvc.perform(post("/auth/logout")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("A new login closes the previous session (single active session)")
    void singleSession() throws Exception {
        Tokens first = loginAsRoot();
        Tokens second = loginAsRoot();

        perform(get("/product/all"), first).andExpect(status().isUnauthorized());
        perform(get("/product/all"), second).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Insufficient role answers 403 and the session stays valid")
    void forbiddenIs403() throws Exception {
        Tokens user = login(createUser(Role.USER), "secret123");

        performJson(post("/suppliers/add"), user, json("name", "X", "contactEmail", "x@x", "phoneNumber", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        perform(get("/logs"), user).andExpect(status().isForbidden());
        perform(get("/consumer/api/users"), user).andExpect(status().isForbidden());

        perform(get("/product/all"), user).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deactivating a user kills their session immediately and blocks login")
    void deactivationRevokesSession() throws Exception {
        String email = createUser(Role.USER);
        Tokens user = login(email, "secret123");
        Long id = consumerRepository.findByEmail(email).getId();

        consumerService.updateUserStatus(id, new UserStatusRequest(false), ROOT_EMAIL);

        perform(get("/product/all"), user).andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("email", email, "password", "secret123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuario inactivo. Contacta a un administrador"));
    }

    @Test
    @DisplayName("Public self-registration no longer exists (anyone could create a ROOT account)")
    void noPublicRegistration() throws Exception {
        mvc.perform(post("/consumer/api/add").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"h\",\"username\":\"h\",\"password\":\"h\",\"email\":\"h@h\",\"role\":\"ROOT\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("The legacy GET /tickets/delete/{barcode} endpoint is gone")
    void noDeleteViaGet() throws Exception {
        Tokens user = login(createUser(Role.USER), "secret123");

        perform(get("/tickets/delete/TCK-ANY"), user).andExpect(status().isNotFound());
    }
}
