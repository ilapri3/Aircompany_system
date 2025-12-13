package org.example.aircompany.services;

import org.example.aircompany.model.Aircraft;
import org.example.aircompany.model.Flight;
import org.example.aircompany.model.FlightLog;
import org.example.aircompany.model.User;
import org.example.aircompany.repositories.AircraftRepository;
import org.example.aircompany.repositories.FlightLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
//
//@Service
//public class FlightLogService {
//
//    private final FlightLogRepository flightLogRepository;
//    private final FlightRepository flightRepository;
//
//    public FlightLogService(FlightLogRepository flightLogRepository,
//                            FlightRepository flightRepository) {
//        this.flightLogRepository = flightLogRepository;
//        this.flightRepository = flightRepository;
//    }
//
//    // Сохранение записи
//    public FlightLog saveLog(FlightLog log) {
//        return flightLogRepository.save(log);
//    }
//
//    // Логи пилота
//    public List<FlightLog> findLogsByPilot(User pilot) {
//        return flightLogRepository.findAll()
//                .stream()
//                .filter(log -> log.getPilot().equals(pilot))
//                .toList();
//    }
//
//    // Логи по рейсу
//    public List<FlightLog> findLogsByFlight(Long flightId) {
//        Flight flight = flightRepository.findById(flightId)
//                .orElseThrow(() -> new IllegalArgumentException("Рейс не найден"));
//        return flightLogRepository.findAll()
//                .stream()
//                .filter(l -> l.getFlight().equals(flight))
//                .toList();
//    }
//
//    public List<FlightLog> findAll() {
//        return flightLogRepository.findAll();
//    }
//
//}
@Service
public class FlightLogService {

    private final FlightLogRepository flightLogRepository;
    private final AircraftRepository aircraftRepository;

    public FlightLogService(FlightLogRepository flightLogRepository,
                           AircraftRepository aircraftRepository) {
        this.flightLogRepository = flightLogRepository;
        this.aircraftRepository = aircraftRepository;
    }

    @Transactional
    public FlightLog save(FlightLog log) {
        // Сохраняем журнал
        FlightLog savedLog = flightLogRepository.save(log);
        
        // Обновляем статус самолета в зависимости от результата технической проверки
        Flight flight = log.getFlight();
        if (flight != null && flight.getAircraft() != null) {
            Aircraft aircraft = flight.getAircraft();
            
            if (log.getTechnicalCheck() != null) {
                if (log.getTechnicalCheck() == FlightLog.TechnicalCheck.pass) {
                    // Если проверка пройдена, самолет становится active
                    aircraft.setStatus(Aircraft.AircraftStatus.active);
                } else if (log.getTechnicalCheck() == FlightLog.TechnicalCheck.fail) {
                    // Если обнаружены проблемы, самолет уходит на обслуживание
                    aircraft.setStatus(Aircraft.AircraftStatus.in_maintenance);
                }
                aircraftRepository.save(aircraft);
            }
        }
        
        return savedLog;
    }

    public List<FlightLog> findAll() {
        return flightLogRepository.findAllByOrderByLogTimeDesc();
    }

    public List<FlightLog> findLogsByPilot(User pilot) {
        return flightLogRepository.findByPilotOrderByLogTimeDesc(pilot);
    }

}
