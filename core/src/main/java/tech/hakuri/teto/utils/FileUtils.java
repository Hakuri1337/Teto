package tech.hakuri.teto.utils;

import java.io.File;
import java.io.FileOutputStream;

public class FileUtils {
    static {
        File dir = new File("C:\\Debug");
        if (!dir.exists()) dir.mkdirs();
    }

    public static void save(String name, byte[] data) {
        try {
            File out = new File(String.format("C:\\Debug\\%s", name));
            FileOutputStream fos = new FileOutputStream(out);
            fos.write(data, 0, data.length);
            fos.flush();
            fos.close();
        } catch (Exception e) {
        }
    }
}
