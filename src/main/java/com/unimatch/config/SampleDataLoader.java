package com.unimatch.config;

import com.unimatch.domain.Interest;
import com.unimatch.domain.InterestType;
import com.unimatch.repository.InterestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SampleDataLoader implements CommandLineRunner {

    private final InterestRepository interestRepository;

    public SampleDataLoader(InterestRepository interestRepository) {
        this.interestRepository = interestRepository;
    }

/** Crea los intereses predeterminados **/
    @Override
    public void run(String... args) {
        interest("Rock alternativo", InterestType.MUSIC);
        interest("Pop", InterestType.MUSIC);
        interest("Interstellar", InterestType.MOVIE);
        interest("Parasite", InterestType.MOVIE);
        interest("Dark", InterestType.SERIES);
        interest("Arctic Monkeys", InterestType.ARTIST);
        interest("Taylor Swift", InterestType.ARTIST);
        interest("Bad Bunny", InterestType.ARTIST);
        interest("Hip hop", InterestType.MUSIC_GENRE);
        interest("Reggaeton", InterestType.MUSIC_GENRE);
        interest("Ciencia ficcion", InterestType.MOVIE_GENRE);
        interest("Terror", InterestType.MOVIE_GENRE);
    }

    /**
     * Obtiene un interes por nombre o lo crea con el tipo indicado si no existe.
     */
    private Interest interest(String name, InterestType type) {
        return interestRepository.findByName(name)
                .orElseGet(() -> interestRepository.save(new Interest(name, type)));
    }
}
