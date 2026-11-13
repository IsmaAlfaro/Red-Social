package com.unimatch.repository;

import com.unimatch.domain.UserAccount;
import com.unimatch.domain.StudentProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserAccountRepository {
    private static final String FILE = "accounts.csv";
    private final StudentProfileRepository profileRepository;
    public UserAccountRepository(StudentProfileRepository profileRepository) { this.profileRepository = profileRepository; }
    public Optional<UserAccount> findByEmail(String email) { for (UserAccount account : read()) if (account.getEmail().equals(email)) return Optional.of(account); return Optional.empty(); }
    public boolean existsByEmail(String email) { return findByEmail(email).isPresent(); }
    public UserAccount save(UserAccount account) {
        synchronized (CsvStore.lock()) {
            StudentProfile profile = profileRepository.save(account.getProfile()); List<List<String>> rows = CsvStore.read(FILE); long id = CsvStore.nextId(rows);
            UserAccount stored = new UserAccount(id, account.getEmail(), account.getPasswordHash(), profile); rows.add(CsvStore.row(String.valueOf(id), stored.getEmail(), stored.getPasswordHash(), String.valueOf(profile.getId()))); CsvStore.write(FILE, rows); return stored;
        }
    }
    private List<UserAccount> read() { List<UserAccount> result = new ArrayList<UserAccount>(); for (List<String> row : CsvStore.read(FILE)) { Optional<StudentProfile> profile = profileRepository.findById(Long.valueOf(row.get(3))); if (profile.isPresent()) result.add(new UserAccount(Long.valueOf(row.get(0)), row.get(1), row.get(2), profile.get())); } return result; }
}
