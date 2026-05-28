package com.climatizacion.sistema_clima.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarCorreo(String destino, String asunto, String cuerpoHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(destino);
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true);
            mailSender.send(message);
            System.out.println("✅ Correo enviado a " + destino);
        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar correo: " + e.getMessage());
            throw new RuntimeException("Error al enviar correo: " + e.getMessage());
        }
    }

    // ========== NUEVOS MÉTODOS PARA NOTIFICACIONES ==========

    public void enviarCorreoBienvenida(String destino, String nombre) {
        String asunto = "¡Bienvenido a ClimaPro!";
        String cuerpo = "<h2>Hola " + nombre + ",</h2>" +
                "<p>Gracias por registrarte en ClimaPro. Ahora puedes explorar nuestro catálogo y realizar tus compras.</p>" +
                "<p>Si tienes alguna duda, contáctanos a través de nuestra página de contacto.</p>" +
                "<br><p>Equipo de Servi A/C Pro.</p>";
        enviarCorreo(destino, asunto, cuerpo);
    }

    public void enviarCorreoPedidoCreado(String destino, String nombre, Long idPedido) {
        String asunto = "Pedido #" + idPedido + " recibido";
        String cuerpo = "<h2>Hola " + nombre + ",</h2>" +
                "<p>Hemos recibido tu pedido <strong>#" + idPedido + "</strong>. Pronto iniciaremos su procesamiento.</p>" +
                "<p>Puedes seguir el estado en tu perfil.</p>" +
                "<br><p>Equipo de Servi A/C Pro.</p>";
        enviarCorreo(destino, asunto, cuerpo);
    }

    public void enviarCorreoCambioEstadoPedido(String destino, String nombre, Long idPedido, String estadoAnterior, String nuevoEstado) {
        String asunto = "Actualización de tu pedido #" + idPedido;
        String cuerpo = "<h2>Hola " + nombre + ",</h2>" +
                "<p>Tu pedido <strong>#" + idPedido + "</strong> ha cambiado de estado:</p>" +
                "<p><strong>Estado anterior:</strong> " + estadoAnterior + "<br>" +
                "<strong>Nuevo estado:</strong> " + nuevoEstado + "</p>" +
                "<p>Puedes ver los detalles en tu perfil.</p>" +
                "<br><p>Equipo de Servi A/C Pro.</p>";
        enviarCorreo(destino, asunto, cuerpo);
    }

    public void enviarCorreoNuevaCita(String destino, String nombreTecnico, String nombreCliente, String fechaHora, String direccion) {
        String asunto = "Nueva cita asignada";
        String cuerpo = "<h2>Hola " + nombreTecnico + ",</h2>" +
                "<p>Tienes una nueva cita asignada:</p>" +
                "<ul>" +
                "<li><strong>Cliente:</strong> " + nombreCliente + "</li>" +
                "<li><strong>Fecha y hora:</strong> " + fechaHora + "</li>" +
                "<li><strong>Dirección:</strong> " + direccion + "</li>" +
                "</ul>" +
                "<p>Por favor, confirma tu asistencia en la aplicación.</p>" +
                "<br><p>Equipo de Servi A/C Pro.</p>";
        enviarCorreo(destino, asunto, cuerpo);
    }

    public void enviarCorreoCambioEstadoCita(String destino, String nombreCliente, Long idCita, String nuevoEstado) {
        String asunto = "Actualización de tu cita #" + idCita;
        String cuerpo = "<h2>Hola " + nombreCliente + ",</h2>" +
                "<p>Te informamos que el estado de tu cita <strong>#" + idCita + "</strong> ha cambiado.</p>" +
                "<p><strong>Nuevo estado:</strong> " + nuevoEstado.replace("_", " ") + "</p>" +
                "<p>Puedes revisar los detalles entrando a tu perfil en ClimaPro.</p>" +
                "<br><p>Gracias por confiar en el equipo de Servi A/C Pro.</p>";
        enviarCorreo(destino, asunto, cuerpo);
    }
}