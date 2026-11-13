package com.unimatch.web.profiles;

import com.unimatch.domain.Interest;
import com.unimatch.repository.InterestRepository;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interests")
public class InterestController {

    private final InterestRepository interestRepository;

    public InterestController(InterestRepository interestRepository) {
        this.interestRepository = interestRepository;
    }

    /** Lista todos los intereses disponibles para los perfiles. */
    @GetMapping
    public List<Interest> list() {
        return interestRepository.findAll();
    }

    /** Crea un interes si su nombre no existe y, en caso contrario, devuelve el existente. */
    @PostMapping
    public Interest create(@Valid @RequestBody CreateInterestRequest request) {
        return interestRepository.findByName(request.getName().trim())
                .orElseGet(() -> interestRepository.save(new Interest(request.getName().trim(), request.getType())));
    }
}
