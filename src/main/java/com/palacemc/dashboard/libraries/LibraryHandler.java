package com.palacemc.dashboard.libraries;

import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Innectic
 * @since 12/30/2016
 */
public final class LibraryHandler {

    public static void loadLibraries(Class clazz) {
        if (clazz == null) return;
        // Loop through list from json
        try {
            if (clazz.getResourceAsStream("/libraries.json") == null) return;
            JSONParser jsonParser = new JSONParser();
            JSONArray libraryArray = (JSONArray) jsonParser.parse(new InputStreamReader(clazz.getResourceAsStream("/libraries.json")));
            for (Object object : libraryArray) {
                JSONObject jsonObject = (JSONObject) object;
                MavenObject mavenObject;
                String groupId = (String) jsonObject.get("groupId");
                String artifactId = (String) jsonObject.get("artifactId");
                String version = (String) jsonObject.get("version");
                String repo = (String) jsonObject.get("repo");
                if (repo == null || repo.trim().isEmpty()) {
                    mavenObject = new MavenObject(groupId, artifactId, version);
                } else {
                    mavenObject = new MavenObject(groupId, artifactId, version, repo);
                }
                handle(mavenObject);
            }
        } catch (ParseException | IOException e) {
            System.out.println("Error parsing library");
            e.printStackTrace();
        }
    }

    private static void handle(MavenObject library) {
        Set<File> jars = new HashSet<>();
        try {
            File location = createAndGetWriteLocation(library);
            if (!location.exists()) {
                System.out.println("Downloading " + getFileName(library) + " from " + library.getRepo());
                try (InputStream inputStream = getUrl(library.getRepo(), library).openStream()) {
                    Files.copy(inputStream, location.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            jars.add(location);
        } catch (Exception e) {
            System.out.println("Could not load library " + library.getArtifactId());
            e.printStackTrace();
        }
        for (File jar : jars) {
            try {
                addFile(jar);
            } catch (IOException e) {
                System.out.println("Could not load jar file " + jar.getName());
                continue;
            }
            System.out.println("Loaded library " + jar.getName());
        }
    }

    private static URL getUrl(String repo, MavenObject library) throws MalformedURLException {
        return new URL(repo + "/" + getPath(library) + getFileName(library));
    }

    private static String getPath(MavenObject library) {
        return library.getGroupId().replaceAll("\\.", "/") + "/" + library.getArtifactId() + "/" + library.getVersion() + "/";
    }

    private static String getFileName(MavenObject library) {
        return library.getArtifactId() + "-" + library.getVersion() + ".jar";
    }

    private static File createAndGetWriteLocation(MavenObject library) throws IOException {
        File rootDir = new File(".libs");
        if ((!rootDir.exists() || !rootDir.isDirectory()) && !rootDir.mkdir()) {
            throw new IOException("Could not create root directory .libs");
        }
        File path = new File(rootDir, getPath(library));
        path.mkdirs();
        return new File(path, getFileName(library));
    }

    private static void addFile(File file) throws IOException {
        addURL(file.toURI().toURL());
    }

    private static void addURL(URL url) throws IOException {
        // Check if this is already loaded
        URLClassLoader sysLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> sysClass = URLClassLoader.class;
        try {
            Method method = sysClass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysLoader, url);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }

    private static class MavenObject {
        @Getter private String groupId = null;
        @Getter private String artifactId = null;
        @Getter private String version = null;
        @Getter private String repo = "http://repo1.maven.org/maven2";

        MavenObject(String groupId, String artifactId, String version) {
            this(groupId, artifactId, version, "http://repo1.maven.org/maven2");
        }

        MavenObject(String groupId, String artifactId, String version, String repo) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.repo = repo;
        }
    }
}