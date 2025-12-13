package org.example.aircompany.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "passengers")
@Data
@NoArgsConstructor
public class Passenger {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "passport_number", nullable = false, unique = true, length = 20)
    private String passportNumber;

    @Column(name = "contact_email", unique = true, length = 55)
    private String contactEmail;

    @Column(name = "contact_phone", unique = true, length = 18)
    private String contactPhone;

    @Column(name = "registration_date")
    private LocalDate registrationDate;
}