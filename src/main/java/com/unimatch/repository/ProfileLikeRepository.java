package com.unimatch.repository;

import com.unimatch.domain.ProfileLike;
import com.unimatch.domain.StudentProfile;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileLikeRepository {
    private static final String FILE = "likes.csv";
    private final StudentProfileRepository profiles;
    public ProfileLikeRepository(StudentProfileRepository profiles) { this.profiles = profiles; }
    /** Comprueba si el perfil origen ya indico interes por el perfil destino. */
    public boolean existsBySourceProfileIdAndTargetProfileId(Long source, Long target) { for (ProfileLike like : read()) if (like.getSourceProfile().getId().equals(source) && like.getTargetProfile().getId().equals(target)) return true; return false; }
    /** Obtiene todos los intereses emitidos por un perfil. */
    public List<ProfileLike> findBySourceProfileId(Long source) { List<ProfileLike> result = new ArrayList<ProfileLike>(); for (ProfileLike like : read()) if (like.getSourceProfile().getId().equals(source)) result.add(like); return result; }
    /** Almacena el interes si no existe ya para la misma pareja de perfiles. */
    public ProfileLike save(ProfileLike like) { synchronized (CsvStore.lock()) { if (existsBySourceProfileIdAndTargetProfileId(like.getSourceProfile().getId(), like.getTargetProfile().getId())) return like; List<List<String>> rows = CsvStore.read(FILE); long id = CsvStore.nextId(rows); rows.add(CsvStore.row(String.valueOf(id), String.valueOf(like.getSourceProfile().getId()), String.valueOf(like.getTargetProfile().getId()))); CsvStore.write(FILE, rows); return new ProfileLike(id, like.getSourceProfile(), like.getTargetProfile()); } }
    /** Elimina el interes dirigido entre los dos perfiles indicados. */
    public void deleteBySourceProfileIdAndTargetProfileId(Long source, Long target) { synchronized (CsvStore.lock()) { List<List<String>> rows = CsvStore.read(FILE); for (int i = rows.size() - 1; i >= 0; i--) if (rows.get(i).get(1).equals(String.valueOf(source)) && rows.get(i).get(2).equals(String.valueOf(target))) rows.remove(i); CsvStore.write(FILE, rows); } }
    /** Hidrata intereses cuyos perfiles origen y destino aun existen. */
    private List<ProfileLike> read() { List<ProfileLike> result = new ArrayList<ProfileLike>(); for (List<String> row : CsvStore.read(FILE)) { StudentProfile source = profiles.findById(Long.valueOf(row.get(1))).orElse(null); StudentProfile target = profiles.findById(Long.valueOf(row.get(2))).orElse(null); if (source != null && target != null) result.add(new ProfileLike(Long.valueOf(row.get(0)), source, target)); } return result; }
}
