package com.unimatch.repository;

import com.unimatch.domain.HiddenContact;
import com.unimatch.domain.StudentProfile;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class HiddenContactRepository {
    private static final String FILE = "hidden_contacts.csv";
    /** Comprueba si el propietario oculto ya tiene oculto al contacto indicado. */
    public boolean existsByOwnerIdAndContactId(Long owner, Long contact) { for (List<String> row : CsvStore.read(FILE)) if (row.get(1).equals(String.valueOf(owner)) && row.get(2).equals(String.valueOf(contact))) return true; return false; }
    /** Guarda la relacion de ocultacion si no se habia registrado antes. */
    public HiddenContact save(HiddenContact contact) { synchronized (CsvStore.lock()) { if (existsByOwnerIdAndContactId(contact.getOwner().getId(), contact.getContact().getId())) return contact; List<List<String>> rows = CsvStore.read(FILE); long id = CsvStore.nextId(rows); rows.add(CsvStore.row(String.valueOf(id), String.valueOf(contact.getOwner().getId()), String.valueOf(contact.getContact().getId()))); CsvStore.write(FILE, rows); return contact; } }
}
