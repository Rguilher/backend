package br.com.studiogui.backend.job;

import br.com.studiogui.backend.model.Appointment;
import br.com.studiogui.backend.repository.AppointmentRepository;
import br.com.studiogui.backend.service.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AppointmentJob {
    private final AppointmentRepository repository;
    private final EmailService emailService;

    public AppointmentJob(AppointmentRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void checkUpcomingAppointments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startWindow = now.plusMinutes(110);
        LocalDateTime endWindow = now.plusMinutes(130);

        List<Appointment> upcomingAppointments = repository
                .findUpcomingAppointmentsNotNotified(startWindow, endWindow);

        for (Appointment appointment : upcomingAppointments) {
            emailService.sendAppointmentReminder(appointment);
            appointment.setReminderSent(true);
            repository.save(appointment);
        }
    }
}