package com.unimatch.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Acceso compartido a CSV; las reescrituras se sincronizan y reemplazan el archivo completo. */
final class CsvStore {
    private static final Object LOCK = new Object();
    private static final Path DATA_DIRECTORY = Paths.get("data");

    private CsvStore() { }

    /** Devuelve el bloqueo comun que coordina operaciones compuestas sobre los CSV. */
    static Object lock() { return LOCK; }

    /** Lee las filas del archivo indicado y devuelve una lista vacia si todavia no existe. */
    static List<List<String>> read(String file) {
        synchronized (LOCK) {
            Path path = DATA_DIRECTORY.resolve(file);
            try {
                if (!Files.exists(path)) {
                    return new ArrayList<List<String>>();
                }
                List<List<String>> rows = new ArrayList<List<String>>();
                for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                    if (!line.isEmpty()) {
                        rows.add(parse(line));
                    }
                }
                return rows;
            } catch (IOException exception) {
                throw new IllegalStateException("No se pudo leer " + path, exception);
            }
        }
    }

    /** Escribe todas las filas mediante un temporal y reemplaza el archivo de destino. */
    static void write(String file, List<List<String>> rows) {
        synchronized (LOCK) {
            Path path = DATA_DIRECTORY.resolve(file);
            Path temporary = DATA_DIRECTORY.resolve(file + ".tmp");
            try {
                Files.createDirectories(DATA_DIRECTORY);
                List<String> lines = new ArrayList<String>();
                for (List<String> row : rows) {
                    lines.add(format(row));
                }
                Files.write(temporary, lines, StandardCharsets.UTF_8);
                try {
                    Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                    Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException exception) {
                throw new IllegalStateException("No se pudo guardar " + path, exception);
            }
        }
    }

    /** Calcula el siguiente identificador consecutivo a partir de la primera columna. */
    static long nextId(List<List<String>> rows) {
        long highest = 0;
        for (List<String> row : rows) {
            highest = Math.max(highest, Long.parseLong(row.get(0)));
        }
        return highest + 1;
    }

    /** Crea una fila sin valores nulos para su almacenamiento CSV. */
    static List<String> row(String... values) {
        List<String> row = new ArrayList<String>();
        for (String value : values) {
            row.add(value == null ? "" : value);
        }
        return row;
    }

    /** Serializa una fila aplicando comillas y escapes compatibles con el lector local. */
    private static String format(List<String> row) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < row.size(); index++) {
            if (index > 0) {
                result.append(',');
            }
            result.append('"').append(escape(row.get(index)).replace("\"", "\"\"")).append('"');
        }
        return result.toString();
    }

    /** Analiza una linea CSV respetando comillas escapadas y separadores internos. */
    private static List<String> parse(String line) {
        List<String> values = new ArrayList<String>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    value.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                values.add(unescape(value.toString()));
                value.setLength(0);
            } else {
                value.append(character);
            }
        }
        if (quoted) {
            throw new IllegalStateException("CSV invalido: comilla sin cerrar");
        }
        values.add(unescape(value.toString()));
        return values;
    }

    /** Codifica barras y saltos de linea antes de escribir un campo. */
    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n");
    }

    /** Restaura los caracteres de control codificados al leer un campo. */
    private static String unescape(String value) {
        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (escaped) { result.append(character == 'n' ? '\n' : character == 'r' ? '\r' : character); escaped = false; }
            else if (character == '\\') escaped = true;
            else result.append(character);
        }
        if (escaped) result.append('\\');
        return result.toString();
    }
}
