package com.korebit.rigel.service.email

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,

    @param:Value("\${app.base-url}")
    private val baseUrl: String,

    @param:Value("\${app.mail.from}")
    private val fromEmail: String,
) {

    /**
     * Sends an account activation email with a one-time link.
     */
    fun sendActivationEmail(to: String, recipientName: String, token: String) {
        val link = "$baseUrl/activate?token=$token"
        send(
            to = to,
            subject = "Activa tu cuenta en Rigel Collection",
            body = buildActivationBody(recipientName, link),
        )
    }

    /**
     * Sends a password reset email with a one-time link.
     */
    fun sendPasswordResetEmail(to: String, recipientName: String, token: String) {
        val link = "$baseUrl/reset-password?token=$token"
        send(
            to = to,
            subject = "Restablecer contraseña — Rigel Collection",
            body = buildPasswordResetBody(recipientName, link),
        )
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private fun send(to: String, subject: String, body: String) {
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
            <h2 style="margin-top:0;color:#1e293b;">Bienvenido a Rigel Collection</h2>
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