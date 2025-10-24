package joamonca.reactinator.json;

import joamonca.reactinator.EmojiData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Reader {
    String path;
    JSONArray jsonArray;

    public Reader(String path) {
        this.path = path;
    }

    public boolean OpenFile() {
        try {
            File file = new File(path);

            // Si el fichero no existe en el directorio de trabajo, lo crea.
            if (!file.exists()) {
                System.out.println("Fichero '" + path + "' no encontrado. Creando uno por defecto.");
                // Escribe un array JSON vacío como contenido inicial.
                Files.write(Paths.get(path), "[]".getBytes(StandardCharsets.UTF_8));
            }

            // Lee el fichero desde el sistema de ficheros.
            try (InputStream is = new FileInputStream(file)) {
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                jsonArray = new JSONArray(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean isTargetID(String id) {
        for (int i = 0; i < jsonArray.length(); i++) {
            // El ID en JSON es un número, pero en JDA es un String. Hay que comparar como Strings.
            if (String.valueOf(jsonArray.getJSONObject(i).getLong("id")).equals(id)) {
                return true;
            }
        }
        return false;
    }

    public EmojiData getEmoji(String id) {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject messageConfig = jsonArray.getJSONObject(i);
            if (String.valueOf(messageConfig.getLong("id")).equals(id)) {
                JSONObject emoji = messageConfig.getJSONObject("emoji");
                String name = emoji.getString("name");
                long emojiID = emoji.getLong("id");
                boolean isAnimated = emoji.getBoolean("animated");
                return new EmojiData(name, emojiID, isAnimated);
            }
        }
        return null;
    }
}