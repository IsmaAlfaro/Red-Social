package com.unimatch.repository;

import com.unimatch.domain.ProfileRejection;
import com.unimatch.domain.StudentProfile;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileRejectionRepository {
    private static final String FILE = "rejections.csv";
    private final StudentProfileRepository profiles;
    public ProfileRejectionRepository(StudentProfileRepository profiles) { this.profiles = profiles; }
    /** Comprueba si el perfil origen ya descarto al perfil destino. */
    public boolean existsBySourceProfileIdAndTargetProfileId(Long source, Long target) { for (ProfileRejection rejection : read()) if (rejection.getSourceProfile().getId().equals(source) && rejection.getTargetProfile().getId().equals(target)) return true; return false; }
    /** Almacena el descarte solo cuando no existe ya para la misma pareja de perfiles. */
    public ProfileRejection save(ProfileRejection rejection) { synchronized (CsvStore.lock()) { if (existsBySourceProfileIdAndTargetProfileId(rejection.getSourceProfile().getId(), rejection.getTargetProfile().getId())) return rejection; List<List<String>> rows = CsvStore.read(FILE); long id = CsvStore.nextId(rows); rows.add(CsvStore.row(String.valueOf(id), String.valueOf(rejection.getSourceProfile().getId()), String.valueOf(rejection.getTargetProfile().getId()))); CsvStore.write(FILE, rows); return new ProfileRejection(id, rejection.getSourceProfile(), rejection.getTargetProfile()); } }
    /** Hidrata los descartes cuyas referencias de perfil siguen existiendo. */
    private List<ProfileRejection> read() { List<ProfileRejection> result = new ArrayList<ProfileRejection>(); for (List<String> row : CsvStore.read(FILE)) { StudentProfile source = profiles.findById(Long.valueOf(row.get(1))).orElse(null); StudentProfile target = profiles.findById(Long.valueOf(row.get(2))).orElse(null); if (source != null && target != null) result.add(new ProfileRejection(Long.valueOf(row.get(0)), source, target)); } return result; }
}
