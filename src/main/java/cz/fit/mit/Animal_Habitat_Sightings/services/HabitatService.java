package cz.fit.mit.Animal_Habitat_Sightings.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HabitatService {

    public String createHabitat(){
        log.info("This is create habitat");
        return "Habitat Created";
    }

    public String getHabitat(){
        log.info("This is get habitat");
        return "Returned Habitat";
    }
}
