package org.example.aircompany.services;

import org.example.aircompany.model.Aircraft;
import org.example.aircompany.repositories.AircraftRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AircraftService {

    private final AircraftRepository aircraftRepository;

    public AircraftService(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    // Получение всех самолетов (используется в FlightController для выпадающего списка)
    public List<Aircraft> findAllAircrafts() {
        return aircraftRepository.findAll();
    }

    // Получение самолета по ID
    public Optional<Aircraft> findAircraftById(Long id) {
        return aircraftRepository.findById(id);
    }

    // Сохранение/Обновление самолета
    public Aircraft saveAircraft(Aircraft aircraft) {
        return aircraftRepository.save(aircraft);
    }

    // Удаление самолета
    public void deleteAircraft(Long id) {
        // Доп. логика: проверка, не привязан ли самолет к активным рейсам
        aircraftRepository.deleteById(id);
    }

    public long countByStatus(String statusName) {
        return aircraftRepository.findAll()
                .stream()
                .filter(a -> a.getStatus().name().equals(statusName))
                .count();
    }

}