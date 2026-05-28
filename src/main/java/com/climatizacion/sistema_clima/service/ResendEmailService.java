package com.climatizacion.sistema_clima.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import com.resend.services.emails.model.SendEmailResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResendEmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    private Resend resendClient;

    @PostConstruct
    public void init() {
        this.resendClient = new Resend(apiKey);
    }

    // Método base privado
    private void enviarCorreoBase(String destinatario, String asunto, String cuerpoHtml) {
        String fromEmail = "onboarding@resend.dev"; // Cambia luego por tu dominio

        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .from("Servi A/C Pro <" + fromEmail + ">")
                .to(destinatario)
                .subject(asunto)
                .html(cuerpoHtml)
                .build();

        try {
            SendEmailResponse response = this.resendClient.emails().send(sendEmailRequest);
            System.out.println("✅ Correo enviado a " + destinatario + " | ID: " + response.getId());
        } catch (ResendException e) {
            System.err.println("❌ Error al enviar correo a " + destinatario + ": " + e.getMessage());
        }
    }

    // ========== MÉTODO GENÉRICO PARA CUALQUIER CORREO ==========
    // Este es el que usa UsuarioImpl para el reset de contraseña
    public void enviarCorreo(String destinatario, String asunto, String cuerpoHtml) {
        enviarCorreoBase(destinatario, asunto, cuerpoHtml);
    }

    // ========== CORREO DE BIENVENIDA ==========
    public void enviarCorreoBienvenida(String destino, String nombre) {
        String asunto = "¡Bienvenido a Servi A/C Pro!";
        String cuerpo = String.format("""
            <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f9f9f9; border-radius: 20px; overflow: hidden;">
                <div style="background: linear-gradient(135deg, #0d6efd 0%%, #00d4ff 100%%); padding: 30px 20px; text-align: center;">
                    <h1 style="color: white; margin: 0;">¡Bienvenido, %s!</h1>
                </div>
                <div style="padding: 30px 25px; background: white;">
                    <p>Gracias por confiar en <strong>Servi A/C Pro</strong>.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://clinquant-tulumba-124b74.netlify.app/catalogo.html" style="background: #0d6efd; color: white; padding: 12px 28px; text-decoration: none; border-radius: 40px;">Ir al catálogo</a>
                    </div>
                </div>
            </div>
            """, nombre);
        enviarCorreoBase(destino, asunto, cuerpo);
    }

    // ========== CORREO DE PEDIDO CREADO ==========
    public void enviarCorreoPedidoCreado(String destino, String nombre, Long idPedido) {
        String asunto = "¡Recibimos tu compra!";
        String cuerpo = String.format("""
            <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f9f9f9; border-radius: 20px; overflow: hidden;">
                <div style="background: linear-gradient(135deg, #0d6efd 0%%, #00d4ff 100%%); padding: 30px 20px; text-align: center;">
                    <h1 style="color: white; margin: 0;">¡Gracias por tu compra!</h1>
                </div>
                <div style="padding: 30px 25px; background: white;">
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Hemos recibido tu pedido correctamente.</p>
                    <div style="background: #eef2ff; border-left: 4px solid #0d6efd; padding: 15px;">
                        <p><strong>Código de seguimiento:</strong> AC-%d</p>
                    </div>
                    <div style="text-align: center; margin-top: 25px;">
                        <a href="https://clinquant-tulumba-124b74.netlify.app/perfil.html" style="background: #0d6efd; color: white; padding: 10px 24px; text-decoration: none; border-radius: 40px;">Ver mi perfil</a>
                    </div>
                </div>
            </div>
            """, nombre, idPedido);
        enviarCorreoBase(destino, asunto, cuerpo);
    }

    // ========== CORREO CAMBIO DE ESTADO DEL PEDIDO ==========
    public void enviarCorreoCambioEstadoPedido(String destino, String nombre, Long idPedido, String estadoAnterior, String nuevoEstado) {
        String asunto = "Tu compra ha cambiado de estado";
        String cuerpo = String.format("""
            <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f9f9f9; border-radius: 20px; overflow: hidden;">
                <div style="background: linear-gradient(135deg, #0d6efd 0%%, #00d4ff 100%%); padding: 25px 20px; text-align: center;">
                    <h2>Actualización de tu compra</h2>
                </div>
                <div style="padding: 25px; background: white;">
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Tu compra (seguimiento <strong>AC-%d</strong>) ha cambiado:</p>
                    <div style="background: #f0fdf4; border-left: 4px solid #22c55e; padding: 12px;">
                        <p><strong>De:</strong> %s</p>
                        <p><strong>A:</strong> %s</p>
                    </div>
                    <div style="text-align: center; margin-top: 25px;">
                        <a href="https://clinquant-tulumba-124b74.netlify.app/perfil.html" style="background: #0d6efd; color: white; padding: 10px 24px; text-decoration: none; border-radius: 40px;">Ver detalles</a>
                    </div>
                </div>
            </div>
            """, nombre, idPedido, estadoAnterior, nuevoEstado);
        enviarCorreoBase(destino, asunto, cuerpo);
    }

    // ========== CORREO NUEVA CITA (para técnico) ==========
    public void enviarCorreoNuevaCita(String destino, String nombreTecnico, String nombreCliente, String fechaHora, String direccion) {
        String asunto = "Nueva cita asignada";
        String cuerpo = String.format("""
            <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f9f9f9; border-radius: 20px; overflow: hidden;">
                <div style="background: linear-gradient(135deg, #0d6efd 0%%, #00d4ff 100%%); padding: 25px 20px; text-align: center;">
                    <h2>📅 Nueva visita programada</h2>
                </div>
                <div style="padding: 25px; background: white;">
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Tienes una cita con <strong>%s</strong>.</p>
                    <ul style="background: #f1f5f9; padding: 15px; border-radius: 12px;">
                        <li>📅 Fecha y hora: %s</li>
                        <li>📍 Dirección: %s</li>
                    </ul>
                </div>
            </div>
            """, nombreTecnico, nombreCliente, fechaHora, direccion);
        enviarCorreoBase(destino, asunto, cuerpo);
    }

    // ========== CORREO CAMBIO DE ESTADO DE CITA (para cliente) ==========
    public void enviarCorreoCambioEstadoCita(String destino, String nombreCliente, Long idCita, String nuevoEstado) {
        String asunto = "Actualización de tu visita técnica";
        String cuerpo = String.format("""
            <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f9f9f9; border-radius: 20px; overflow: hidden;">
                <div style="background: linear-gradient(135deg, #0d6efd 0%%, #00d4ff 100%%); padding: 25px 20px; text-align: center;">
                    <h2>🚀 Estado de tu visita</h2>
                </div>
                <div style="padding: 25px; background: white;">
                    <p>Hola <strong>%s</strong>,</p>
                    <p>El estado de tu visita (código <strong>VIS-%d</strong>) ha cambiado a:</p>
                    <div style="background: #eef2ff; padding: 12px; border-radius: 12px; text-align: center; font-size: 18px; font-weight: bold; color: #0d6efd;">
                        %s
                    </div>
                </div>
            </div>
            """, nombreCliente, idCita, nuevoEstado.replace("_", " "));
        enviarCorreoBase(destino, asunto, cuerpo);
    }
}