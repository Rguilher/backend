package br.com.studiogui.backend.model;

import br.com.studiogui.backend.model.enums.AppointmentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;


    @ManyToOne
    @JoinColumn(name = "professional_id", nullable = false)
    private User professional;


    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private GuiServices service;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Column(columnDefinition = "TEXT")
    private String observation;
}
