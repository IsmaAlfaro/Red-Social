package com.unimatch.web;

import com.unimatch.domain.Interest;
import com.unimatch.repository.InterestRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interests")
public class InterestController {

    private final InterestRepository interestRepository;

    public InterestController(InterestRepository interestRepository) {
        this.interestRepository = interestRepository;
    }

    @GetMapping
    public List<Interest> list() {
        return interestRepository.findAll();
    }
}
