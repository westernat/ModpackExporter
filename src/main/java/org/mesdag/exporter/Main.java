package org.mesdag.exporter;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mesdag.exporter.FileUtil.*;

public class Main {
    public static void main(String[] args) throws IOException {
        File manifestFile = new File(outputDir + "manifest.json");
        File parentDir = manifestFile.getParentFile();
        if (!parentDir.exists()) parentDir.mkdirs();
        try (FileWriter writer = new FileWriter(manifestFile)) {
            writer.write(getManifest());
        }
        File zip = new File(outputDir + config.get("name") + (server_only ? "[Server]" : "") + "-" + config.get("version") + ".zip");
        if (!zip.exists()) zip.createNewFile();

        byte[] buf = new byte[1024];
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));
            int len;
            ArrayList<File> overrides = getOverrideFiles();
            int size = overrides.size();
            int saved = 0;
            for (File override : overrides) {
                FileInputStream fis = new FileInputStream(override);
                zos.putNextEntry(new ZipEntry("overrides" + File.separator + getRelativePath(override.getAbsolutePath(), main_path)));
                while ((len = fis.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                fis.close();
                saved++;
                float progress = (float) saved / size;
                CLIHelper.printSchedule((int) (progress * 100));
            }
            FileInputStream fis = new FileInputStream(manifestFile);
            zos.putNextEntry(new ZipEntry(manifestFile.getName()));
            while ((len = fis.read(buf)) > 0) {
                zos.write(buf, 0, len);
            }
            zos.closeEntry();
            fis.close();
            zos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
