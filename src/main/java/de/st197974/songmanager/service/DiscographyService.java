package de.st197974.songmanager.service;

import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.repository.SongRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;

/**
 * Provides services for managing and querying a discography of songs.
 * Encapsulates functionality for retrieving, sorting, and manipulating songs stored in a repository.
 * <p>
 * Responsibilities include:
 * - Filtering and retrieving songs based on properties such as album, artist, or duration.
 * - Adding, updating, and deleting songs while performing validations and logging actions.
 * - Sorting songs by different attributes (e.g., title, album, duration).
 * - Managing operations specific to albums and artists.
 */
public record DiscographyService(SongRepository repository) {

    private static final Logger logger = LogManager.getLogger(DiscographyService.class);

    public List<Song> getSongsByAlbum(String albumName) {
        return repository.findAll().stream()
                .filter(song -> song.album().equalsIgnoreCase(albumName))
                .toList();
    }

    public List<Song> getAll() {
        return repository.findAll();
    }

    public int getTotalDurationOfAlbum(String albumName) {
        return repository.findAll().stream()
                .filter(song -> song.album().equalsIgnoreCase(albumName))
                .mapToInt(Song::durationInSeconds)
                .sum();
    }

    public void addSongSafely(Song song) {
        if (song.title() == null || song.title().isEmpty()) {
            logger.warn("Attempted to add song without title!");
            return;
        }

        repository.save(song);

        logger.info("Song {} added successfully!", song.title());
    }

    public String getSongTitleById(String id) {
        return repository.findAll().stream()
                .filter(s -> s.id().equals(id))
                .map(Song::title)
                .findFirst()
                .orElse("Unknown Song");
    }

    public void deleteSong(String id) {

        boolean exists = repository.findAll().stream()
                .anyMatch(song -> song.id().equals(id));

        if (!exists) return;
        logger.info("Song {} was deleted!", getSongTitleById(id));
        repository.deleteByID(id);

    }

    public List<Song> getAllSortedByAlbum() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Song::album, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Song::title, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Song> getAllSortedByDuration() {

        return repository.findAll().stream()
                .sorted(Comparator.comparing(Song::durationInSeconds))
                .toList();

    }

    public List<String> getAllArtists() {
        return repository.findAll().stream()
                .map(Song::artist)
                .filter(a -> a != null && !a.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }


    public List<Song> getSongsByArtist(String artist) {
        return repository.findByArtist(artist);
    }

    public List<Song> getSongsByArtistSortedByAlbum(String artist) {
        return repository.findByArtist(artist).stream()
                .sorted(Comparator.comparing(Song::album, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Song::title, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Song> getSongsByArtistSortedByDuration(String artist) {
        return repository.findByArtist(artist).stream()
                .sorted(Comparator.comparingInt(Song::durationInSeconds))
                .toList();
    }

    public List<Song> getSongsAlphabetically(String artist) {

        return repository.findByArtist(artist).stream()
                .sorted(Comparator.comparing(Song::title, String.CASE_INSENSITIVE_ORDER))
                .toList();

    }

    public void updateSongSafely(Song updatedSong) {

        if (updatedSong.id() == null || updatedSong.title().isEmpty()) {
            logger.warn("Update Failed: Song ID or Title Missing!");
            return;
        }

        repository.save(updatedSong);
        logger.info("Song Updated: {} (ID: {}", updatedSong.title(), updatedSong.id());

    }

    public Song getSongById(String id) {
        return repository.findByID(id);
    }

    public void cleanupInvalidSongs() {
        repository.deleteInvalidSongs();
        logger.info("Restarted Song Repository and cleaned up invalid songs.");
    }

    // Logik für Textdatei imports damit ich den überblick behalte der kommentar hier (temporär)

    public int importSongsFromLines(List<String> lines) {
        int importedCount = 0;
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;

            Song song = parseSmartLine(line);
            if (song != null) {
                addSongSafely(song);
                importedCount++;
            }
        }
        return importedCount;
    }

    private Song parseSmartLine(String line) {
        try {

            String cleanLine = line.trim();

            String durationRegex = "(?:\\s+|\\s*[,.-]\\s*)(\\d{1,2}:\\d{2}|\\d{2,4})\\s*$";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(durationRegex);
            java.util.regex.Matcher matcher = pattern.matcher(cleanLine);

            int duration = 0;
            String textPart = cleanLine;

            if (matcher.find()) {
                String durationString = matcher.group(1);
                duration = parseDurationInternal(durationString);

                textPart = cleanLine.substring(0, matcher.start()).trim();
            }

            String separator = detectSeparator(textPart);

            if (separator == null) return new Song(textPart, "", "Unknown Artist", duration);

            String[] parts = textPart.split(separator);

            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }

            String title = parts.length > 0 ? parts[0] : "Unknown Title";
            String artist = parts.length > 1 ? parts[1] : "Unknown Artist";
            String album = parts.length > 2 ? parts[2] : "";

            return new Song(title, album, artist, duration);

        } catch (Exception e) {
            logger.error("Error parsing line: {}", line);
            return null;
        }
    }

    private String detectSeparator(String text) {
        int commas = countOccurrences(text, ',');
        int dashes = countOccurrences(text, '-');
        int vertLines = countOccurrences(text, '|');

        int dots = text.split("\\s\\.\\s").length - 1;

        if (commas >= 1) return "\\,";
        if (dashes >= 1) return "-";
        if (vertLines >= 1) return "|";
        if (dots >= 1) return "\\s\\.\\s";
        return null;
    }

    private int countOccurrences(String text, char c) {
        int count = 0;
        for (char ch : text.toCharArray()) {
            if (ch == c) count++;
        }
        return count;
    }

    private int parseDurationInternal(String input) {
        if (input.matches("\\d+")) {
            return Integer.parseInt(input);
        }
        String[] parts = input.split(":");
        if (parts.length == 2) {
            int min = Integer.parseInt(parts[0]);
            int sec = Integer.parseInt(parts[1]);
            return min * 60 + sec;
        }
        return 0;
    }

}
