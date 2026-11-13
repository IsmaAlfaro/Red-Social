package com.unimatch.audit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Object LOCK = new Object();
    private static final Path FILE = Paths.get("data", "audit.csv");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public AuditService() {
        normalizeExistingDates();
    }

    /** Registra de forma atomica una accion, su responsable y el detalle en el historial CSV. */
    public void record(String action, String actor, String detail) {
        synchronized (LOCK) {
            try {
                Files.createDirectories(FILE.getParent());
                List<String> rows = new ArrayList<String>();
                if (Files.exists(FILE)) rows.addAll(Files.readAllLines(FILE, StandardCharsets.UTF_8));
                rows.add(value(DATE_FORMAT.format(Instant.now())) + "," + value(action) + "," + value(actor) + "," + value(detail));
                Path temporary = FILE.resolveSibling("audit.csv.tmp");
                Files.write(temporary, rows, StandardCharsets.UTF_8);
                try {
                    Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                    Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException exception) { throw new IllegalStateException("No se pudo guardar la auditoria", exception); }
        }
    }

    /** Escapa un campo para conservarlo como una unica celda CSV. */
    private String value(String field) { return "\"" + field.replace("\\", "\\\\").replace("\"", "\"\"").replace("\r", "\\r").replace("\n", "\\n") + "\""; }

    /** Convierte fechas ISO antiguas al formato breve una sola vez al iniciar. */
    private void normalizeExistingDates() {
        synchronized (LOCK) {
            try {
                if (!Files.exists(FILE)) return;
                List<String> rows = Files.readAllLines(FILE, StandardCharsets.UTF_8);
                boolean changed = false;
                for (int index = 0; index < rows.size(); index++) {
                    String row = rows.get(index);
                    int end = row.indexOf("\",");
                    if (row.startsWith("\"") && end > 1) {
                        try {
                            String date = row.substring(1, end);
                            String formatted = DATE_FORMAT.format(Instant.parse(date));
                            rows.set(index, value(formatted) + row.substring(end));
                            changed = true;
                        } catch (java.time.format.DateTimeParseException ignored) {
                            // The row already uses the short format.
                        }
                    }
                }
                if (changed) Files.write(FILE, rows, StandardCharsets.UTF_8);
            } catch (IOException exception) { throw new IllegalStateException("No se pudo normalizar la auditoria", exception); }
        }
    }
}
