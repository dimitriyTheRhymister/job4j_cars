package ru.job4j.cars.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.cars.model.Engine;
import ru.job4j.cars.repository.EngineRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class EngineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineService.class);
    private final EngineRepository engineRepository;

    @Transactional
    public Engine save(Engine engine) {
        return engineRepository.save(engine);
    }

    public List<Engine> findAll() {
        return engineRepository.findAllOrderById();
    }

    public Optional<Engine> findById(int id) {
        return engineRepository.findById(id);
    }

    @Transactional
    public void delete(int id) {
        engineRepository.delete(id);
    }

    public List<Engine> findByPowerBetween(int minPower, int maxPower) {
        return engineRepository.findByPowerBetween(minPower, maxPower);
    }

    public List<Engine> findByVolumeGreaterThan(double minVolume) {
        return engineRepository.findByVolumeGreaterThan(minVolume);
    }
}