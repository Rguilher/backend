package br.com.studiogui.backend.repository;

import br.com.studiogui.backend.model.Appointment;
import br.com.studiogui.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByClient(User client);
    List<Appointment> findAllByProfessional(User professional);

    // 🔥 NOVO: Busca agendamentos de um profissional num intervalo de tempo (ex: no dia X)
    // Usaremos isso para validar conflitos.
    // "Mostre a agenda do Profissional X entre as 00:00 e as 23:59 da data tal"
    @Query("SELECT a FROM Appointment a " +
            "WHERE a.professional = :professional " +
            "AND a.status <> 'CANCELED' " + // Ignora os cancelados (eles não ocupam vaga)
            "AND a.dateTime BETWEEN :start AND :end")
    List<Appointment> findProfessionalAgenda(
            @Param("professional") User professional,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Retorna todos os agendamentos ativos do profissional naquele intervalo
    @Query("SELECT a FROM Appointment a " +
            "WHERE a.professional.id = :professionalId " +
            "AND a.status <> 'CANCELED' " +
            "AND a.dateTime >= :start AND a.dateTime < :end")
    List<Appointment> findByProfessionalAndDate(
            @Param("professionalId") Long professionalId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT a FROM Appointment a " +
            "WHERE (a.client.id = :userId OR a.professional.id = :userId) " +
            "AND a.dateTime BETWEEN :start AND :end " +
            "ORDER BY a.dateTime ASC")
    List<Appointment> findByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
            "WHERE (a.professional.id = :professionalId OR a.client.id = :clientId) " +
            "AND a.status <> 'CANCELED' " +
            "AND ((a.dateTime < :endTime) AND (a.dateTime + a.service.durationMin MINUTE > :startTime))")
    boolean existsConflictingAppointment(
            @Param("professionalId") Long professionalId,
            @Param("clientId") Long clientId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
