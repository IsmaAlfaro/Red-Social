package com.unimatch.config;

import com.unimatch.domain.Interest;
import com.unimatch.domain.InterestType;
import com.unimatch.domain.StudentProfile;
import com.unimatch.repository.InterestRepository;
import com.unimatch.repository.StudentProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SampleDataLoader implements CommandLineRunner {

    private final InterestRepository interestRepository;
    private final StudentProfileRepository profileRepository;

    public SampleDataLoader(InterestRepository interestRepository, StudentProfileRepository profileRepository) {
        this.interestRepository = interestRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (profileRepository.count() > 0) {
            return;
        }

        Interest rock = interestRepository.save(new Interest("Rock alternativo", InterestType.MUSIC));
        Interest pop = interestRepository.save(new Interest("Pop", InterestType.MUSIC));
        Interest interstellar = interestRepository.save(new Interest("Interstellar", InterestType.MOVIE));
        Interest parasite = interestRepository.save(new Interest("Parasite", InterestType.MOVIE));
        Interest dark = interestRepository.save(new Interest("Dark", InterestType.SERIES));

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
}
