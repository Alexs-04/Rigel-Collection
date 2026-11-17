package com.korebit.rigel.integration

import com.korebit.rigel.service.email.EmailService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@DisplayName("Application without email configuration")
@TestPropertySource(
    properties = [
        "app.mail.from=",
        "spring.mail.username=",
        "spring.mail.password=",
    ]
)
class MailOptionalIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var emailService: EmailService

    @Test
    @DisplayName("Starts with email disabled and inviting a user still works without sending anything")
    fun inviteWithoutMail() {
        assertFalse(emailService.enabled)

        val email = "${unique("invited")}@test.local"
        performJson(
            post("/admin/users/invite"), loginAsRoot(),
            json("name", "Invited", "username", unique("inv"), "email", email),
        )
            .andExpect(status().is2xxSuccessful())

        assertNotNull(consumerRepository.findByEmail(email))
        verifyNoInteractions(mailSender)
    }
}
