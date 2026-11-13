package com.unimatch.repository;

import com.unimatch.domain.StudentProfile;
import com.unimatch.domain.Interest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import org.springframework.stereotype.Repository;

@Repository
public class StudentProfileRepository {
    private static final String FILE = "profiles.csv";
    private final InterestRepository interestRepository;
    public StudentProfileRepository(InterestRepository interestRepository) { this.interestRepository = interestRepository; }
    public List<StudentProfile> findAll() { return read(); }
    public Optional<StudentProfile> findById(Long id) { for (StudentProfile profile : read()) if (profile.getId().equals(id)) return Optional.of(profile); return Optional.empty(); }
    public StudentProfile getById(Long id) { return findById(id).orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado")); }
    public boolean existsById(Long id) { return findById(id).isPresent(); }
    public long count() { return CsvStore.read(FILE).size(); }
    /** Stores profile data in CSV and converts a browser image payload into a local file. */
    public StudentProfile save(StudentProfile profile) {
        synchronized (CsvStore.lock()) {
            List<List<String>> rows = CsvStore.read(FILE); Long id = profile.getId();
            long storedId = id == null ? CsvStore.nextId(rows) : id;
            profile.setProfileImage(storeImage(storedId, profile.getProfileImage()));
            List<String> stored = row(storedId, profile);
            boolean replaced = false;
            for (int i = 0; i < rows.size(); i++) if (rows.get(i).get(0).equals(stored.get(0))) { rows.set(i, stored); replaced = true; break; }
            if (!replaced) rows.add(stored); CsvStore.write(FILE, rows); return hydrate(stored);
        }
    }
    private List<StudentProfile> read() { List<StudentProfile> result = new ArrayList<StudentProfile>(); for (List<String> row : CsvStore.read(FILE)) result.add(hydrate(row)); return result; }
    private StudentProfile hydrate(List<String> row) {
        StudentProfile profile = new StudentProfile(Long.valueOf(row.get(0)), row.get(1), row.get(2), row.get(3), row.get(4)); profile.setProfileImage(row.get(5));
        if (!row.get(6).isEmpty()) for (String id : row.get(6).split("\\|")) interestRepository.findById(Long.valueOf(id)).ifPresent(profile::addInterest); return profile;
    }
    private List<String> row(long id, StudentProfile profile) { StringBuilder interests = new StringBuilder(); for (Interest interest : profile.getInterests()) { if (interests.length() > 0) interests.append('|'); interests.append(interest.getId()); } return CsvStore.row(String.valueOf(id), profile.getName(), profile.getUniversityEmail(), profile.getDegree(), profile.getBio(), profile.getProfileImage(), interests.toString()); }

    /** Replaces Base64 image data with a compact URL that keeps profiles.csv readable. */
    private String storeImage(long profileId, String image) {
        if (image == null || !image.startsWith("data:image/")) return image;
        try {
            String header = image.substring(0, image.indexOf(','));
            String extension = header.contains("png") ? ".png" : ".jpg";
            byte[] bytes = Base64.getDecoder().decode(image.substring(image.indexOf(',') + 1));
            Path directory = Paths.get("uploads");
            Files.createDirectories(directory);
            Files.write(directory.resolve("profile-" + profileId + extension), bytes);
            return "/uploads/profile-" + profileId + extension;
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo guardar la foto del perfil", exception);
        }
    }
}
