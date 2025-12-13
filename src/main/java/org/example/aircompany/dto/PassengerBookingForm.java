package org.example.aircompany.dto;

import lombok.Data;

@Data
public class PassengerBookingForm {

    private String firstName;
    private String lastName;
    private String passportNumber;
    private String contactEmail;
    private String contactPhone;

    private Long flightId; // скрытое поле
    private String seatNumber; // номер места
}
