package com.unimatch.config;

import com.unimatch.domain.Interest;
import com.unimatch.domain.InterestType;
import com.unimatch.domain.StudentProfile;
import com.unimatch.repository.InterestRepository;
import com.unimatch.repository.StudentProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SampleDataLoader implements CommandLineRunner {

    private final InterestRepository interestRepository;
    private final StudentProfileRepository profileRepository;

    public SampleDataLoader(InterestRepository interestRepository, StudentProfileRepository profileRepository) {
        this.interestRepository = interestRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    public void run(String... args) {
        Interest rock = interest("Rock alternativo", InterestType.MUSIC);
        Interest pop = interest("Pop", InterestType.MUSIC);
        Interest interstellar = interest("Interstellar", InterestType.MOVIE);
        Interest parasite = interest("Parasite", InterestType.MOVIE);
        Interest dark = interest("Dark", InterestType.SERIES);
        interest("Arctic Monkeys", InterestType.ARTIST);
        interest("Taylor Swift", InterestType.ARTIST);
        interest("Bad Bunny", InterestType.ARTIST);
        interest("Hip hop", InterestType.MUSIC_GENRE);
        interest("Reggaeton", InterestType.MUSIC_GENRE);
        interest("Ciencia ficcion", InterestType.MOVIE_GENRE);
        interest("Terror", InterestType.MOVIE_GENRE);

        if (profileRepository.count() > 0) {
            return;
        }

        StudentProfile camila = new StudentProfile(
                "Camila Torres", "camila.torres@universidad.edu", "Ingenieria de Sistemas",
                "Busco recomendaciones de cine y conciertos para el fin de semana."
        );
        camila.addInterest(rock);
        camila.addInterest(interstellar);
        camila.addInterest(dark);

        StudentProfile mateo = new StudentProfile(
                "Mateo Rojas", "mateo.rojas@universidad.edu", "Diseno Grafico",
                "Fan del cine, las bandas sonoras y descubrir musica nueva."
        );
        mateo.addInterest(pop);
        mateo.addInterest(interstellar);
        mateo.addInterest(parasite);

        profileRepository.save(camila);
        profileRepository.save(mateo);
    }

    private Interest interest(String name, InterestType type) {
        return interestRepository.findByName(name)
                .orElseGet(() -> interestRepository.save(new Interest(name, type)));
    }
}
