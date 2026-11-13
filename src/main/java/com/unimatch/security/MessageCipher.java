package com.unimatch.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class MessageCipher {
    private static final Path KEY_FILE = Paths.get("data", "message.key");
    private final SecretKeySpec key;

    public MessageCipher() {
        try {
            Files.createDirectories(KEY_FILE.getParent());
            if (!Files.exists(KEY_FILE)) {
                byte[] bytes = new byte[32];
                new SecureRandom().nextBytes(bytes);
                Files.write(KEY_FILE, Base64.getEncoder().encode(bytes));
            }
            key = new SecretKeySpec(Base64.getDecoder().decode(Files.readAllBytes(KEY_FILE)), "AES");
        } catch (IOException exception) { throw new IllegalStateException("No se pudo cargar la clave de mensajes", exception); }
    }

    /** Cifra el texto antes de almacenarlo en el CSV junto con un IV aleatorio. */
    public String encrypt(String text) {
        try {
            byte[] iv = new byte[12]; new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            byte[] value = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, value, 0, iv.length); System.arraycopy(encrypted, 0, value, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(value);
        } catch (GeneralSecurityException exception) { throw new IllegalStateException("No se pudo cifrar el mensaje", exception); }
    }

    /** Descifra mensajes nuevos y mantiene legibles los mensajes antiguos sin cifrar. */
    public String decrypt(String value) {
        try {
            byte[] bytes = Base64.getDecoder().decode(value); if (bytes.length < 13) return value;
            byte[] iv = java.util.Arrays.copyOfRange(bytes, 0, 12); byte[] encrypted = java.util.Arrays.copyOfRange(bytes, 12, bytes.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) { return value;
        } catch (GeneralSecurityException exception) { throw new IllegalStateException("No se pudo descifrar el mensaje", exception); }
    }
}
