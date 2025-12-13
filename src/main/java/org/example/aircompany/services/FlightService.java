package org.example.aircompany.services;

import org.example.aircompany.model.Aircraft;
import org.example.aircompany.model.Flight;
import org.example.aircompany.model.User;
import org.example.aircompany.repositories.AircraftRepository;
import org.example.aircompany.repositories.FlightLogRepository;
import org.example.aircompany.repositories.FlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FlightService {

    private final FlightRepository flightRepository;
    private final FlightLogRepository flightLogRepository;
    private final AircraftRepository aircraftRepository;

    public FlightService(FlightRepository flightRepository, 
                        FlightLogRepository flightLogRepository,
                        AircraftRepository aircraftRepository) {
        this.flightRepository = flightRepository;
        this.flightLogRepository = flightLogRepository;
        this.aircraftRepository = aircraftRepository;
    }

    public List<Flight> findAllFlights() {
        return flightRepository.findAll();
    }

    public Optional<Flight> findFlightById(Long id) {
        return flightRepository.findById(id);
    }

    @Transactional
    public Flight saveFlight(Flight flight) {
        // Если это обновление существующего рейса
        if (flight.getFlightId() != null) {
            Optional<Flight> existingFlightOpt = flightRepository.findById(flight.getFlightId());
            if (existingFlightOpt.isPresent()) {
                Flight existingFlight = existingFlightOpt.get();
                
                // Если статус рейса изменился на completed, НЕ меняем статус самолета автоматически
                // Статус самолета будет изменен только после создания лётного журнала пилотом
                // Самолет остается в статусе in_flight до создания журнала
                
                // Если самолет изменился, нужно обработать старый и новый самолет
                if (existingFlight.getAircraft() != null 
                    && flight.getAircraft() != null
                    && !existingFlight.getAircraft().getAircraftId().equals(flight.getAircraft().getAircraftId())) {
                    // Старый самолет - если он был in_flight и больше не используется в активных рейсах
                    Aircraft oldAircraft = existingFlight.getAircraft();
                    updateAircraftStatusIfNeeded(oldAircraft);
                }
            }
        }
        
        // При назначении самолета на рейс (новый или обновленный)
        if (flight.getAircraft() != null) {
            Aircraft aircraft = flight.getAircraft();
            // Если самолет в статусе active, меняем на in_flight
            if (aircraft.getStatus() == Aircraft.AircraftStatus.active) {
                aircraft.setStatus(Aircraft.AircraftStatus.in_flight);
                aircraftRepository.save(aircraft);
            }
        }
        
        return flightRepository.save(flight);
    }
    
    private void updateAircraftStatusIfNeeded(Aircraft aircraft) {
        // Проверяем, есть ли у этого самолета активные рейсы (не completed и не cancelled)
        List<Flight> activeFlights = flightRepository.findByAircraft(aircraft).stream()
                .filter(f -> f.getStatus() != Flight.FlightStatus.completed 
                          && f.getStatus() != Flight.FlightStatus.cancelled)
                .toList();
        
        // Если нет активных рейсов и самолет был in_flight, меняем на in_maintenance
        if (activeFlights.isEmpty() && aircraft.getStatus() == Aircraft.AircraftStatus.in_flight) {
            aircraft.setStatus(Aircraft.AircraftStatus.in_maintenance);
            aircraftRepository.save(aircraft);
        }
    }

    @Transactional
    public void deleteFlight(Long id) {
        Optional<Flight> flightOpt = flightRepository.findById(id);
        if (flightOpt.isPresent()) {
            Flight flight = flightOpt.get();
            Aircraft aircraft = flight.getAircraft();
            
            // Удаляем рейс
            flightRepository.deleteById(id);
            
            // Обновляем статус самолета, если нужно
            if (aircraft != null) {
                updateAircraftStatusIfNeeded(aircraft);
            }
        } else {
            flightRepository.deleteById(id);
        }
    }

    public List<Flight> findFlightsByPilot(User pilot) {
        return flightRepository.findByPilot(pilot);
    }

    /**
     * Проверяет, может ли пилот быть назначен на рейс.
     * Пилот не может быть назначен, если он уже назначен на другой незавершенный рейс.
     * 
     * @param pilot пилот для проверки
     * @param currentFlightId ID текущего рейса (для исключения при обновлении), может быть null
     * @return true, если пилот может быть назначен, false - если уже назначен на другой незавершенный рейс
     */
    public boolean canAssignPilotToFlight(User pilot, Long currentFlightId) {
        if (pilot == null) {
            return true; // Если пилот не назначен, проверка не нужна
        }
        
        List<Flight> activeFlights = flightRepository.findActiveFlightsByPilot(pilot);
        
        // Если это обновление существующего рейса, исключаем его из проверки
        if (currentFlightId != null) {
            activeFlights = activeFlights.stream()
                    .filter(f -> !f.getFlightId().equals(currentFlightId))
                    .toList();
        }
        
        return activeFlights.isEmpty();
    }

    /**
     * Возвращает список пилотов, которые могут быть назначены на рейс.
     * Пилот может быть назначен, если он не имеет активных рейсов (не completed и не cancelled).
     * 
     * @param allPilots список всех пилотов для фильтрации
     * @param currentFlightId ID текущего рейса (для исключения при обновлении), может быть null
     * @return список доступных пилотов (изменяемый список)
     */
    public List<User> getAvailablePilots(List<User> allPilots, Long currentFlightId) {
        return allPilots.stream()
                .filter(pilot -> canAssignPilotToFlight(pilot, currentFlightId))
                .collect(Collectors.toList());
    }

    public List<Flight> findFlightsByPilotWithoutLogs(User pilot) {
        // Получаем все рейсы пилота и фильтруем те, которые:
        // 1. Имеют статус completed
        // 2. Для которых еще нет отчетов
        return flightRepository.findByPilot(pilot).stream()
                .filter(flight -> flight.getStatus() == Flight.FlightStatus.completed)
                .filter(flight -> !flightLogRepository.existsByFlight(flight))
                .toList();
    }

    public List<Flight> searchFlights(String departureCity, String arrivalCity, LocalDate date) {

        if (date == null) {
            return List.of(); // или можешь возвращать flightRepository.findAll()
        }

        LocalDateTime dateStart = date.atStartOfDay();

        String depCity = (departureCity != null && !departureCity.trim().isEmpty())
                ? departureCity.trim()
                : null;

        String arrCity = (arrivalCity != null && !arrivalCity.trim().isEmpty())
                ? arrivalCity.trim()
                : null;

        return flightRepository.searchFlights(depCity, arrCity, dateStart);
    }
    
    /**
     * Исправляет статусы самолетов для всех завершенных рейсов.
     * Вызывается при запуске приложения для синхронизации данных.
     * 
     * ВАЖНО: Статус самолета для завершенных рейсов теперь меняется только при создании
     * лётного журнала пилотом. Самолет остается в статусе in_flight до создания журнала.
     */
    @Transactional
    public void fixAircraftStatusesForCompletedFlights() {
        // Находим все рейсы со статусом completed
        List<Flight> completedFlights = flightRepository.findAll().stream()
                .filter(f -> f.getStatus() == Flight.FlightStatus.completed)
                .toList();
        
        for (Flight flight : completedFlights) {
            Aircraft aircraft = flight.getAircraft();
            if (aircraft != null) {
                // Проверяем, есть ли у этого самолета другие активные рейсы
                List<Flight> activeFlights = flightRepository.findByAircraft(aircraft).stream()
                        .filter(f -> f.getStatus() != Flight.FlightStatus.completed 
                                  && f.getStatus() != Flight.FlightStatus.cancelled)
                        .toList();
                
                // Если есть активные рейсы, самолет должен быть in_flight
                if (!activeFlights.isEmpty() && aircraft.getStatus() != Aircraft.AircraftStatus.in_flight) {
                    aircraft.setStatus(Aircraft.AircraftStatus.in_flight);
                    aircraftRepository.save(aircraft);
                }
                
                // Если нет активных рейсов и для завершенного рейса еще не создан журнал,
                // самолет остается в статусе in_flight до создания журнала.
                // Статус будет изменен при создании журнала в FlightLogService.
            }
        }
    }
}
