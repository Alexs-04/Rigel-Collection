package com.korebit.rigel.service.email

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

/**
 * Sends transactional emails.
 *
 * Email is optional: when `app.mail.from` is blank (no `EMAIL_ADDRESS` configured) nothing is sent and
 * the one-time link is written to the server log instead, so the application can run without SMTP.
 */
@Service
class EmailService(
    private val mailSender: JavaMailSender,

    @param:Value("\${app.base-url}")
    private val baseUrl: String,

    @param:Value("\${app.mail.from:}")
    private val fromEmail: String,
) {

    private val log = LoggerFactory.getLogger(EmailService::class.java)
    private val normalizedBaseUrl = baseUrl.trimEnd('/')

    /** `true` when a sender address is configured and emails are actually delivered. */
    val enabled: Boolean = fromEmail.isNotBlank()

    @PostConstruct
    fun reportStatus() {
        if (!enabled) {
            log.warn(
                "Email is not configured (EMAIL_ADDRESS is empty): activation and password-reset " +
                    "links will be logged instead of sent."
            )
        }
    }

    /**
     * Sends an account activation email with a one-time link.
     */
    fun sendActivationEmail(to: String, recipientName: String, token: String) {
        val link = "$normalizedBaseUrl/activate?token=$token"
        send(
            to = to,
            subject = "Activa tu cuenta en Rigel",
            body = buildActivationBody(recipientName, link),
            link = link,
        )
    }

    /**
     * Sends a password reset email with a one-time link.
     */
    fun sendPasswordResetEmail(to: String, recipientName: String, token: String) {
        val link = "$normalizedBaseUrl/reset-password?token=$token"
        send(
            to = to,
            subject = "Restablecer contraseña — Rigel",
            body = buildPasswordResetBody(recipientName, link),
            link = link,
        )
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private fun send(to: String, subject: String, body: String, link: String) {
        if (!enabled) {
            log.warn("Email disabled, not sending \"{}\" to {}. Link: {}", subject, to, link)
            return
        }
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setFrom(fromEmail)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(body, true)
        mailSender.send(message)
    }

    private fun buildActivationBody(name: String, link: String): String = """
        <!DOCTYPE html>
        <html lang="es">
        <head><meta charset="UTF-8"></head>
        <body style="font-family:sans-serif;background:#f4f4f5;margin:0;padding:32px;">
          <div style="max-width:520px;margin:auto;background:#fff;border-radius:12px;padding:40px;box-shadow:0 2px 8px rgba(0,0,0,.08);">
            <h2 style="margin-top:0;color:#1e293b;">Bienvenido a Rigel</h2>
            <p style="color:#475569;">Hola <strong>$name</strong>,</p>
            <p style="color:#475569;">El administrador te ha dado acceso al sistema. Haz clic en el botón de abajo para activar tu cuenta y establecer tu contraseña.</p>
            <div style="text-align:center;margin:32px 0;">
              <a href="$link"
                 style="background:#6366f1;color:#fff;text-decoration:none;padding:14px 32px;border-radius:8px;font-weight:600;font-size:15px;display:inline-block;">
                Activar mi cuenta
              </a>
            </div>
            <p style="color:#94a3b8;font-size:13px;">Este enlace es válido por 48 horas. Si no esperabas este correo, puedes ignorarlo.</p>
            <hr style="border:none;border-top:1px solid #e2e8f0;margin:24px 0;">
            <p style="color:#cbd5e1;font-size:12px;margin:0;">Rigel Collection · Sistema de gestión</p>
          </div>
        </body>
        </html>
    """.trimIndent()

    private fun buildPasswordResetBody(name: String, link: String): String = """
        <!DOCTYPE html>
        <html lang="es">
        <head><meta charset="UTF-8"></head>
        <body style="font-family:sans-serif;background:#f4f4f5;margin:0;padding:32px;">
          <div style="max-width:520px;margin:auto;background:#fff;border-radius:12px;padding:40px;box-shadow:0 2px 8px rgba(0,0,0,.08);">
            <h2 style="margin-top:0;color:#1e293b;">Restablecer contraseña</h2>
            <p style="color:#475569;">Hola <strong>$name</strong>,</p>
            <p style="color:#475569;">Recibimos una solicitud para restablecer la contraseña de tu cuenta. Haz clic en el botón de abajo para continuar.</p>
            <div style="text-align:center;margin:32px 0;">
              <a href="$link"
                 style="background:#6366f1;color:#fff;text-decoration:none;padding:14px 32px;border-radius:8px;font-weight:600;font-size:15px;display:inline-block;">
                Restablecer contraseña
              </a>
            </div>
            <p style="color:#94a3b8;font-size:13px;">Este enlace es válido por 1 hora. Si no solicitaste este cambio, puedes ignorar este correo — tu contraseña no será modificada.</p>
            <hr style="border:none;border-top:1px solid #e2e8f0;margin:24px 0;">
            <p style="color:#cbd5e1;font-size:12px;margin:0;">Rigel Collection · Sistema de gestión</p>
          </div>
        </body>
        </html>
    """.trimIndent()
}