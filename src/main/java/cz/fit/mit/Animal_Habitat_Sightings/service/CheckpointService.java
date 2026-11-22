package cz.fit.mit.Animal_Habitat_Sightings.service;

import cz.fit.mit.Animal_Habitat_Sightings.AnimalHabitatSightingsApplication;
import cz.fit.mit.Animal_Habitat_Sightings.model.Checkpoint;
import cz.fit.mit.Animal_Habitat_Sightings.repository.CheckpointRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;

@Service
public class CheckpointService {

    private final CheckpointRepository checkpointRepository;

    public CheckpointService(CheckpointRepository checkpointRepository) {
        this.checkpointRepository = checkpointRepository;
    }

    public void addCheckpoint(Checkpoint checkpoint) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            checkpointRepository.insertCheckpoint(conn, checkpoint);
        }
    }

    public List<Checkpoint> getAllCheckpoints() throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            return checkpointRepository.getAllCheckpoints(conn);
        }
    }

    public void deleteCheckpoint(long checkpointId) throws Exception {
        try (Connection conn = AnimalHabitatSightingsApplication.getConnection()) {
            checkpointRepository.deleteCheckpoint(conn, checkpointId);
        }
    }
}
