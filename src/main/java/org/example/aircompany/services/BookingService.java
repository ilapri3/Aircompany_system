package org.example.aircompany.services;

import org.example.aircompany.model.Booking;
import org.example.aircompany.model.Flight;
import org.example.aircompany.model.User;
import org.example.aircompany.repositories.BookingRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /** CRUD-операции для Сотрудника службы бронирования */

    // 1. Сохранение/Обновление бронирования (Update)
    // Пассажир создает бронирование через отдельный контроллер, а сотрудник его обновляет (например, меняет статус).
    public Booking saveBooking(Booking booking) {
        // Проверка доступности места при изменении
        if (booking.getSeatNumber() != null && !booking.getSeatNumber().trim().isEmpty()) {
            // Проверяем, не занято ли место другим бронированием (кроме текущего)
            List<Booking> bookingsWithSameSeat = bookingRepository.findByFlight(booking.getFlight()).stream()
                    .filter(b -> booking.getSeatNumber().trim().equals(b.getSeatNumber()) 
                            && !b.getBookingId().equals(booking.getBookingId()))
                    .toList();
            if (!bookingsWithSameSeat.isEmpty()) {
                throw new IllegalStateException("Место " + booking.getSeatNumber() + " уже занято");
            }
        }
        return bookingRepository.save(booking);
    }

    // 2. Чтение всех бронирований
    public List<Booking> findAllBookings() {
        return bookingRepository.findAll();
    }

    // 3. Чтение одного бронирования по ID
    public Optional<Booking> findBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    // 4. Удаление бронирования (Delete)
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    public Booking createBookingForPassenger(User passenger, Flight flight, String seatNumber) {

        if (bookingRepository.existsByUserAndFlight(passenger, flight)) {
            throw new IllegalStateException("Вы уже забронировали этот рейс");
        }

        // Проверка занятости места
        if (seatNumber != null && !seatNumber.trim().isEmpty()) {
            if (bookingRepository.existsByFlightAndSeatNumber(flight, seatNumber.trim())) {
                throw new IllegalStateException("Место " + seatNumber + " уже занято");
            }
        }

        Booking booking = new Booking();
        booking.setUser(passenger);
        booking.setFlight(flight);
        booking.setStatus(Booking.BookingStatus.confirmed);
        if (seatNumber != null && !seatNumber.trim().isEmpty()) {
            booking.setSeatNumber(seatNumber.trim());
        }
        return bookingRepository.save(booking);
    }

    public List<Booking> findBookingsByUser(User user) {
        return bookingRepository.findByUser(user);
    }

    public List<Booking> findBookingsByFlight(Flight flight) {
        return bookingRepository.findByFlight(flight);
    }

    public List<String> getOccupiedSeats(Flight flight) {
        return bookingRepository.findByFlight(flight).stream()
                .map(Booking::getSeatNumber)
                .filter(seat -> seat != null && !seat.trim().isEmpty())
                .toList();
    }
}