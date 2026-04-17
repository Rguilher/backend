package br.com.studiogui.backend.model;

import br.com.studiogui.backend.model.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "tb_appointments")
@Entity(name = "Appointment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;


    @ManyToOne
    @JoinColumn(name = "professional_id", nullable = false)
    private User professional;


    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private SalonService service;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Column(name = "reminder_sent", nullable = false)
    private boolean reminderSent = false;
}
