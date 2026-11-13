package com.unimatch.repository;

import com.unimatch.domain.ChatMessage;
import com.unimatch.domain.StudentProfile;
import com.unimatch.security.MessageCipher;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ChatMessageRepository {
    private static final String FILE = "messages.csv";
    private final StudentProfileRepository profiles;
    private final MessageCipher cipher;

    public ChatMessageRepository(StudentProfileRepository profiles, MessageCipher cipher) { this.profiles = profiles; this.cipher = cipher; }

    /** Asigna un identificador y almacena un mensaje con sus perfiles remitente y destinatario. */
    public ChatMessage save(ChatMessage message) { synchronized (CsvStore.lock()) { List<List<String>> rows = CsvStore.read(FILE); long id = CsvStore.nextId(rows); rows.add(CsvStore.row(String.valueOf(id), String.valueOf(message.getSenderId()), String.valueOf(message.getRecipientId()), cipher.encrypt(message.getContent()), message.getSentAt().toString())); CsvStore.write(FILE, rows); return new ChatMessage(id, profiles.getById(message.getSenderId()), profiles.getById(message.getRecipientId()), message.getContent(), message.getSentAt()); } }

    /** Recupera los mensajes intercambiados entre los dos perfiles en ambos sentidos. */
    public List<ChatMessage> findConversation(Long first, Long second) { List<ChatMessage> result = new ArrayList<ChatMessage>(); for (List<String> row : CsvStore.read(FILE)) { long sender = Long.parseLong(row.get(1)); long recipient = Long.parseLong(row.get(2)); if ((sender == first && recipient == second) || (sender == second && recipient == first)) result.add(hydrate(row)); } return result; }

    /** Elimina todos los mensajes de la conversacion entre los dos perfiles. */
    public void deleteConversation(Long first, Long second) { synchronized (CsvStore.lock()) { List<List<String>> rows = CsvStore.read(FILE); for (int i = rows.size() - 1; i >= 0; i--) { List<String> row = rows.get(i); long sender = Long.parseLong(row.get(1)); long recipient = Long.parseLong(row.get(2)); if ((sender == first && recipient == second) || (sender == second && recipient == first)) rows.remove(i); } CsvStore.write(FILE, rows); } }

    /** Convierte una fila CSV en un mensaje resolviendo los perfiles referenciados. */
    private ChatMessage hydrate(List<String> row) { return new ChatMessage(Long.valueOf(row.get(0)), profiles.getById(Long.valueOf(row.get(1))), profiles.getById(Long.valueOf(row.get(2))), cipher.decrypt(row.get(3)), Instant.parse(row.get(4))); }
}
