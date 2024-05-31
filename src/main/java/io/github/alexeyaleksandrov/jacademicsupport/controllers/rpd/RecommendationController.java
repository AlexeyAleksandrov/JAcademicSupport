package io.github.alexeyaleksandrov.jacademicsupport.controllers.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.repositories.RpdRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.recommendation.RecommendationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;
    private final RpdRepository rpdRepository;

//    @GetMapping("/rpd/create")
//    public ResponseEntity<Rpd> createRpd() {
//
//    }

//    @GetMapping("/rpd/recommendations")
//    public ResponseEntity<Rpd> getRecommendations(@RequestParam(name = "id") Long id) {
//
//    }
}
