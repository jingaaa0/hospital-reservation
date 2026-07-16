package com.hospital.reservation.inquiry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiries")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 254)
    private String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean privacyAgreed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Inquiry() {
    }

    private Inquiry(String name, String phoneNumber, String email, String content) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.content = content;
        this.privacyAgreed = true;
        this.status = InquiryStatus.RECEIVED;
        this.createdAt = LocalDateTime.now();
    }

    public static Inquiry create(String name, String phoneNumber, String email, String content) {
        return new Inquiry(name, phoneNumber, email, content);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getContent() {
        return content;
    }

    public InquiryStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void changeStatus(InquiryStatus status) {
        this.status = status;
    }
}
