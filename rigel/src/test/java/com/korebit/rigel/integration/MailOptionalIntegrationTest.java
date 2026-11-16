package com.korebit.rigel.integration;

import com.korebit.rigel.service.email.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Application without email configuration")
@TestPropertySource(properties = {
        "app.mail.from=",
        "spring.mail.username=",
        "spring.mail.password=",
})
class MailOptionalIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private EmailService emailService;

    @Test
    @DisplayName("Starts with email disabled and inviting a user still works without sending anything")
    void inviteWithoutMail() throws Exception {
        assertFalse(emailService.getEnabled());

        String email = unique("invited") + "@test.local";
        performJson(post("/admin/users/invite"), loginAsRoot(),
                json("name", "Invited", "username", unique("inv"), "email", email))
                .andExpect(status().is2xxSuccessful());

        assertNotNull(consumerRepository.findByEmail(email));
        verifyNoInteractions(mailSender);
    }
}
