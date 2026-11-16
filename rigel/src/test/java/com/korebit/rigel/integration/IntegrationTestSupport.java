package com.korebit.rigel.integration;

import com.jayway.jsonpath.JsonPath;
import com.korebit.rigel.enums.Role;
import com.korebit.rigel.model.beans.Consumer;
import com.korebit.rigel.repository.ConsumerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Boots the full application against the in-memory H2 database ({@code test} profile)
 * and drives it through MockMvc, including the real security filter chain.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationTestSupport {

    protected static final String ROOT_EMAIL = "root@root";
    protected static final String ROOT_PASSWORD = "root";

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected ConsumerRepository consumerRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;

    /** No real emails are sent from tests. */
    @MockitoBean
    protected JavaMailSender mailSender;

    protected record Tokens(String access, String refresh) {
        String bearer() {
            return "Bearer " + access;
        }
    }

    protected Tokens login(String email, String password) throws Exception {
        String body = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("email", email, "password", password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new Tokens(JsonPath.read(body, "$.access_token"), JsonPath.read(body, "$.refresh_token"));
    }

    protected Tokens loginAsRoot() throws Exception {
        return login(ROOT_EMAIL, ROOT_PASSWORD);
    }

    /** Persists an active user directly and returns its email. Password is "secret123". */
    protected String createUser(Role role) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Consumer consumer = new Consumer();
        consumer.setName("User " + suffix);
        consumer.setUsername("u" + suffix);
        consumer.setEmail("u" + suffix + "@test.local");
        consumer.setPassword(passwordEncoder.encode("secret123"));
        consumer.setRole(role);
        consumer.setActive(true);
        consumerRepository.save(consumer);
        return consumer.getEmail();
    }

    protected ResultActions perform(MockHttpServletRequestBuilder request, Tokens tokens) throws Exception {
        return mvc.perform(request.header(HttpHeaders.AUTHORIZATION, tokens.bearer()));
    }

    protected ResultActions performJson(MockHttpServletRequestBuilder request, Tokens tokens, String body) throws Exception {
        return perform(request.contentType(MediaType.APPLICATION_JSON).content(body), tokens);
    }

    protected static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /** Minimal JSON object builder for flat string payloads: json("k1", "v1", "k2", "v2"). */
    protected static String json(String... keyValues) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i > 0) sb.append(',');
            sb.append('"').append(keyValues[i]).append("\":");
            String value = keyValues[i + 1];
            sb.append(value == null ? "null" : '"' + value.replace("\"", "\\\"") + '"');
        }
        return sb.append('}').toString();
    }
}
