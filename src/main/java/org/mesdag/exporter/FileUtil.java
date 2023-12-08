package org.mesdag.exporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileUtil {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String userDir = System.getProperty("user.dir") + File.separator;
    public static final String outputDir = userDir + "output" + File.separator;
    public static final HashMap<String, Object> config;

    static {
        try {
            config = getConfig();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String main_path = config.get("main-path") + File.separator;
    private static final ArrayList<String> mods$blacklist = getFullPaths("mods$blacklist");
    private static final ArrayList<String> mods$client_side = getFullPaths("mods$client-side");
    public static final boolean server_only = (boolean) config.get("server-only");

    private static ArrayList<String> getFullPaths(String key) {
        ArrayList<String> list = new ArrayList<>();
        for (String modName : obj2ArrayList(config.get(key), String.class)) {
            list.add(main_path + "mods" + File.separator + modName);
        }
        return list;
    }

    private static HashMap<String, Object> getConfig() throws FileNotFoundException {
        HashMap<String, Object> config;
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(userDir + "exporter_config.json"), StandardCharsets.UTF_8)) {
            config = gson.fromJson(reader, new TypeToken<HashMap<String, Object>>() {
            }.getType());
        } catch (Exception e) {
            throw new FileNotFoundException(e.getMessage());
        }
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 2000;
        int month = calendar.get(Calendar.MONTH) + 1;
        String date = String.valueOf(calendar.get(Calendar.DATE));
        if (date.length() == 1) date = "0" + date;
        String version = config.get("status") + "-" + year + "." + month + "." + date;
        config.put("version", version);
        return config;
    }

    public static String getManifest() {
        HashMap<String, Object> manifest = new HashMap<>();
        manifest.put("manifestType", "minecraftModpack");
        manifest.put("manifestVersion", 1);
        manifest.put("name", config.get("name"));
        manifest.put("version", config.get("version"));
        manifest.put("author", config.get("author"));
        manifest.put("overrides", "overrides");
        HashMap<String, Object> minecraft = new HashMap<>();
        minecraft.put("version", "1.18.2");
        HashMap<String, Object> loader = new HashMap<>();
        loader.put("id", "forge-40.2.10");
        loader.put("primary", true);
        ArrayList<HashMap<String, Object>> modLoaders = new ArrayList<>(List.of(loader));
        minecraft.put("modLoaders", modLoaders);
        manifest.put("minecraft", minecraft);
        manifest.put("files", Map.of());
        return gson.toJson(manifest);
    }

    private static ArrayList<File> getAllFiles(File file) {
        ArrayList<File> allFiles = new ArrayList<>();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    allFiles.addAll(getAllFiles(file1));
                }
            }
        } else {
            allFiles.add(file);
        }
        return allFiles;
    }

    private static <T> ArrayList<T> obj2ArrayList(Object obj, Class<T> clazz) {
        ArrayList<T> list = new ArrayList<>();
        for (Object o : (ArrayList<?>) obj) {
            list.add(clazz.cast(o));
        }
        return list;
    }

    public static String getRelativePath(String absolutePath, String parentPath) {
        return absolutePath.substring(parentPath.length());
    }

    public static ArrayList<File> getOverrideFiles() {
        ArrayList<File> overrideFiles = new ArrayList<>();

        ArrayList<String> require = obj2ArrayList(config.get("require"), String.class);
        for (String path : require) {
            if (path.endsWith("*")) {
                path = path.substring(0, path.length() - 2);
            }
            File dir = new File(main_path + path);
            if (!dir.exists()) {
                System.out.println(dir.getAbsolutePath() + " not found");
                continue;
            }
            overrideFiles.addAll(getAllFiles(dir));
        }

        File modsDir = new File(main_path + "mods");
        File[] mods = modsDir.listFiles();
        if (mods != null) {
            for (File mod : mods) {
                if (mod.isFile() && !mods$blacklist.contains(mod.getAbsolutePath())) {
                    if (server_only && mods$client_side.contains(mod.getAbsolutePath())) {
                        continue;
                    }
                    overrideFiles.add(mod);
                }
            }
        }

        return overrideFiles;
    }
}
