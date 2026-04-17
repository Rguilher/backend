package br.com.studiogui.backend.service;

import br.com.studiogui.backend.model.Appointment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    private String senderEmail = "onboarding@resend.dev";

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendAppointmentReminder(Appointment appointment) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            Context context = new Context();
            context.setVariable("name", appointment.getClient().getName());
            context.setVariable("service", appointment.getService().getName());
            context.setVariable("time", appointment.getDateTime().format(DateTimeFormatter.ofPattern("HH:mm")));

            String html = templateEngine.process("appointment-reminder", context);

            helper.setSubject("Lembrete de Agendamento - Studio Gui");
            helper.setTo(appointment.getClient().getEmail());
            helper.setText(html, true);

            helper.setFrom(senderEmail, "Studio Gui");

            mailSender.send(mimeMessage);

            System.out.println("Successfully sent reminder email to: " + appointment.getClient().getEmail() +
                    " for service: " + appointment.getService().getName());
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}