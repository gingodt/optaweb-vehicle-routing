package org.optaweb.tsp.optawebtspplanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class TspMapController {

    private static Logger logger = LoggerFactory.getLogger(TspMapController.class);

    private final PlaceRepository repository;

    @Autowired
    public TspMapController(PlaceRepository repository) {
        this.repository = repository;
    }

    @MessageMapping("/place")
    @SendTo("/topic/route")
    public Iterable<Place> create(Place place) {
        Place savedPlace = repository.save(place);
        logger.info("Created {}", savedPlace);
        return repository.findAll();
    }

    @MessageMapping({"/place/{id}/delete"})
    @SendTo("/topic/route")
    public Iterable<Place> delete(@DestinationVariable Long id) {
        repository.deleteById(id);
        logger.info("Deleted place {}", id);
        return repository.findAll();
    }
}
