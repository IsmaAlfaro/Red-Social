package com.unimatch.repository;

import com.unimatch.domain.Interest;
import com.unimatch.domain.InterestType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InterestRepository {
    private static final String FILE = "interests.csv";

    /** Recupera todos los intereses almacenados. */
    public List<Interest> findAll() { return read(); }
    /** Busca un interes por su nombre exacto. */
    public Optional<Interest> findByName(String name) { for (Interest interest : read()) if (interest.getName().equals(name)) return Optional.of(interest); return Optional.empty(); }
    /** Busca un interes por identificador. */
    public Optional<Interest> findById(Long id) { for (Interest interest : read()) if (interest.getId().equals(id)) return Optional.of(interest); return Optional.empty(); }
    /** Recupera los intereses existentes de los identificadores proporcionados. */
    public List<Interest> findAllById(Iterable<Long> ids) { List<Interest> result = new ArrayList<Interest>(); for (Long id : ids) findById(id).ifPresent(result::add); return result; }
    /** Asigna un identificador y persiste un nuevo interes en el CSV. */
    public Interest save(Interest interest) {
        synchronized (CsvStore.lock()) {
            List<List<String>> rows = CsvStore.read(FILE); long id = CsvStore.nextId(rows);
            Interest stored = new Interest(id, interest.getName(), interest.getType());
            rows.add(CsvStore.row(String.valueOf(id), stored.getName(), stored.getType().name())); CsvStore.write(FILE, rows); return stored;
        }
    }
    /** Convierte las filas CSV en entidades de interes. */
    private List<Interest> read() { List<Interest> result = new ArrayList<Interest>(); for (List<String> row : CsvStore.read(FILE)) result.add(new Interest(Long.valueOf(row.get(0)), row.get(1), InterestType.valueOf(row.get(2)))); return result; }
}
