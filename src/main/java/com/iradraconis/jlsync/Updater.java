package com.iradraconis.jlsync;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.swing.JOptionPane;

public class Updater {
    // Aktuelle Version der Anwendung – passe diesen Wert an
    private static final String CURRENT_VERSION = "v1.0";
    // GitHub API URL für alle Releases
    private static final String GITHUB_RELEASES_API = "https://api.github.com/repos/iradraconis/jLSync/releases";

    public static void checkForUpdates() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL) // folgt Redirects
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_RELEASES_API))
                    .header("Accept", "application/vnd.github.v3+json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println("Fehler beim Abrufen der Release-Daten von GitHub. Status: " + response.statusCode());
                return;
            }

            String responseBody = response.body();
            Gson gson = new Gson();
            JsonArray releases = gson.fromJson(responseBody, JsonArray.class);

            // Ermittele den höchsten Versions-Tag
            String highestTag = null;
            Version highestVersion = null;
            for (JsonElement element : releases) {
                JsonObject releaseObj = element.getAsJsonObject();
                String tagName = releaseObj.get("tag_name").getAsString();
                Version currentVersion = Version.parse(tagName);
                if (highestVersion == null || currentVersion.compareTo(highestVersion) > 0) {
                    highestVersion = currentVersion;
                    highestTag = tagName;
                }
            }

            if (highestTag == null) {
                System.out.println("Keine Releases gefunden.");
                return;
            }

            System.out.println("Aktuelle Version: " + CURRENT_VERSION);
            System.out.println("Höchste Version: " + highestTag);

            if (isNewerVersion(highestTag, CURRENT_VERSION)) {
                int choice = JOptionPane.showConfirmDialog(null,
                        "Ein Update ist verfügbar (" + highestTag + "). Möchten Sie das Update herunterladen und installieren?",
                        "Update verfügbar", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    // Finde das Release-Objekt für den höchsten Tag
                    JsonObject selectedRelease = null;
                    for (JsonElement element : releases) {
                        JsonObject releaseObj = element.getAsJsonObject();
                        if (releaseObj.get("tag_name").getAsString().equals(highestTag)) {
                            selectedRelease = releaseObj;
                            break;
                        }
                    }
                    if (selectedRelease == null) {
                        JOptionPane.showMessageDialog(null, "Kein passendes Release gefunden.", "Update Fehler", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Suche im Release nach dem passenden .jar-Asset
                    JsonArray assets = selectedRelease.getAsJsonArray("assets");
                    String downloadUrl = null;
                    for (int i = 0; i < assets.size(); i++) {
                        JsonObject asset = assets.get(i).getAsJsonObject();
                        String assetName = asset.get("name").getAsString();
                        if (assetName.endsWith(".jar")) {
                            // Hier könntest du entweder den von der API gelieferten Link verwenden:
                            // downloadUrl = asset.get("browser_download_url").getAsString();
                            // Oder den Link manuell zusammensetzen:
                            downloadUrl = "https://github.com/iradraconis/jLSync/releases/download/" + highestTag + "/" + assetName;
                            break;
                        }
                    }
                    if (downloadUrl == null) {
                        JOptionPane.showMessageDialog(null, "Kein passendes Update (jar-Datei) gefunden.", "Update Fehler", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Herunterladen der .jar-Datei in ein temporäres Verzeichnis
                    Path tempJar = Files.createTempFile("jLSync_Update_", ".jar");
                    System.out.println("Lade Update herunter von: " + downloadUrl);
                    HttpRequest downloadRequest = HttpRequest.newBuilder()
                            .uri(URI.create(downloadUrl))
                            .build();
                    HttpResponse<InputStream> downloadResponse = client.send(downloadRequest, HttpResponse.BodyHandlers.ofInputStream());
                    if (downloadResponse.statusCode() == 200) {
                        Files.copy(downloadResponse.body(), tempJar, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Update heruntergeladen nach: " + tempJar.toString());
                        // In-Place Update: Starte einen externen Updater-Prozess, der die alte JAR ersetzt
                        launchNewVersionInPlace(tempJar);
                    } else {
                        JOptionPane.showMessageDialog(null, "Fehler beim Herunterladen des Updates.", "Update Fehler", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                System.out.println("Keine neuen Updates verfügbar.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fehler beim Überprüfen auf Updates.", "Update Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Vergleicht zwei Versionsstrings (z.B. "v0.9" vs. "v1.0")
    private static boolean isNewerVersion(String latest, String current) {
        String latestNum = latest.replaceAll("[^0-9\\.]", "");
        String currentNum = current.replaceAll("[^0-9\\.]", "");
        try {
            String[] latestParts = latestNum.split("\\.");
            String[] currentParts = currentNum.split("\\.");
            int length = Math.max(latestParts.length, currentParts.length);
            for (int i = 0; i < length; i++) {
                int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
                if (latestPart > currentPart) {
                    return true;
                } else if (latestPart < currentPart) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Startet einen externen Updater-Prozess, der die aktuell laufende JAR-Datei
     * durch die heruntergeladene neue Version ersetzt (in-place) und die neue Version startet.
     */
    private static void launchNewVersionInPlace(Path tempJar) {
        try {
            // Erhalte den Pfad der aktuell laufenden JAR-Datei
            String currentJarPath = new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
            // Starte einen separaten Updater-Prozess (InPlaceUpdater als inner class)
            ProcessBuilder pb = new ProcessBuilder(
                    "java", "-cp", currentJarPath, "com.iradraconis.jlsync.Updater$InPlaceUpdater",
                    currentJarPath, tempJar.toString());
            pb.inheritIO();
            pb.start();
            // Beende die aktuelle Anwendung
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fehler beim Starten des In-Place-Updaters.", "Update Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Hilfsklasse zum Parsen und Vergleichen von Versionsnummern.
    // Erwartet einen Tag im Format "v1.2.3" (das führende "v" wird entfernt).
    public static class Version implements Comparable<Version> {
        private int major;
        private int minor;
        private int patch;

        public Version(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        public static Version parse(String tag) {
            if (tag.startsWith("v") || tag.startsWith("V")) {
                tag = tag.substring(1);
            }
            String[] parts = tag.split("\\.");
            int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            return new Version(major, minor, patch);
        }

        @Override
        public int compareTo(Version other) {
            if (this.major != other.major) {
                return this.major - other.major;
            }
            if (this.minor != other.minor) {
                return this.minor - other.minor;
            }
            return this.patch - other.patch;
        }

        @Override
        public String toString() {
            return "v" + major + "." + minor + "." + patch;
        }
    }

    /**
     * Dieser Updater-Prozess wird in einem separaten Java-Prozess gestartet.
     * Er wartet, bis die Hauptanwendung beendet wurde, kopiert dann die heruntergeladene
     * neue JAR in den Pfad der aktuellen JAR-Datei (in-place Update) und startet sie.
     *
     * Der Updater wird über die Kommandozeilenargumente aufgerufen:
     * args[0] = Pfad der aktuellen JAR-Datei
     * args[1] = Pfad zur heruntergeladenen temporären JAR-Datei
     */
    public static class InPlaceUpdater {
        public static void main(String[] args) {
            if (args.length < 2) {
                System.out.println("Usage: InPlaceUpdater <currentJar> <tempJar>");
                System.exit(1);
            }
            String currentJar = args[0];
            String tempJar = args[1];
            try {
                // Warten, bis die Hauptanwendung vollständig beendet ist
                Thread.sleep(5000);
                // Ersetze die alte JAR durch die neue Version
                Files.copy(Paths.get(tempJar), Paths.get(currentJar), StandardCopyOption.REPLACE_EXISTING);
                // Starte die aktualisierte JAR-Datei
                ProcessBuilder pb = new ProcessBuilder("java", "-jar", currentJar);
                pb.inheritIO();
                pb.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
