package br.com.studiogui.backend.service;

import br.com.studiogui.backend.model.Appointment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    @Value("${EMAIL_SENDER}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }


    @Async("emailTaskExecutor")
    public void sendPasswordRecoveryEmail(String toEmail, String code) {
        try {
            log.info("Iniciando envio do código de recuperação para o e-mail: {}", toEmail);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            Context context = new Context();
            context.setVariable("code", code);

            String html = templateEngine.process("password-recovery", context);

            helper.setSubject("Código de Recuperação de Senha - Studio Gui");
            helper.setTo(toEmail);
            helper.setText(html, true);
            helper.setFrom(senderEmail, "Studio Gui");

            mailSender.send(mimeMessage);

            log.info("E-mail de recuperação enviado com sucesso para: {}", toEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Erro crítico ao enviar e-mail de recuperação para {}. Motivo: {}", toEmail, e.getMessage());
        }
    }

    @Async("emailTaskExecutor")
    public void sendAppointmentReminder(Appointment appointment) {
        try {
            log.info("Enviando lembrete de agendamento para o cliente: {}", appointment.getClient().getEmail());

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

            log.info("Lembrete de agendamento enviado com sucesso para: {}", appointment.getClient().getEmail());

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Erro ao enviar lembrete para {}. Motivo: {}", appointment.getClient().getEmail(), e.getMessage());
        }
    }
}