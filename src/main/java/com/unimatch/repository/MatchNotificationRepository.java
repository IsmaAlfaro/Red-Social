package com.unimatch.repository;

import com.unimatch.domain.MatchNotification;
import com.unimatch.domain.StudentProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MatchNotificationRepository {
    private static final String FILE = "match_notifications.csv";
    private final StudentProfileRepository profiles;
    public MatchNotificationRepository(StudentProfileRepository profiles) { this.profiles = profiles; }
    /** Obtiene la primera notificacion no vista del propietario indicado. */
    public Optional<MatchNotification> findFirstByOwnerIdAndSeenFalseOrderByIdAsc(Long owner) { for (MatchNotification notification : read()) if (notification.getOwner().getId().equals(owner) && !notification.isSeen()) return Optional.of(notification); return Optional.empty(); }
    /** Indica si ya se notifico este match al propietario. */
    public boolean existsByOwnerIdAndMatchId(Long owner, Long match) { for (MatchNotification notification : read()) if (notification.getOwner().getId().equals(owner) && notification.getMatch().getId().equals(match)) return true; return false; }
    /** Inserta o actualiza una notificacion conservando su identificador y estado de lectura. */
    public MatchNotification save(MatchNotification notification) { synchronized (CsvStore.lock()) { List<List<String>> rows = CsvStore.read(FILE); Long id = notification.getId(); List<String> stored = CsvStore.row(String.valueOf(id == null ? CsvStore.nextId(rows) : id), String.valueOf(notification.getOwner().getId()), String.valueOf(notification.getMatch().getId()), String.valueOf(notification.isSeen())); boolean replaced = false; for (int i = 0; i < rows.size(); i++) if (rows.get(i).get(0).equals(stored.get(0))) { rows.set(i, stored); replaced = true; break; } if (!replaced) rows.add(stored); CsvStore.write(FILE, rows); return hydrate(stored); } }
    /** Reconstruye todas las notificaciones desde el almacenamiento CSV. */
    private List<MatchNotification> read() { List<MatchNotification> result = new ArrayList<MatchNotification>(); for (List<String> row : CsvStore.read(FILE)) result.add(hydrate(row)); return result; }
    /** Resuelve los perfiles de una fila o falla si alguna referencia es invalida. */
    private MatchNotification hydrate(List<String> row) { StudentProfile owner = profiles.findById(Long.valueOf(row.get(1))).orElse(null); StudentProfile match = profiles.findById(Long.valueOf(row.get(2))).orElse(null); if (owner == null || match == null) throw new IllegalStateException("Notificacion con perfil inexistente"); return new MatchNotification(Long.valueOf(row.get(0)), owner, match, Boolean.parseBoolean(row.get(3))); }
}
