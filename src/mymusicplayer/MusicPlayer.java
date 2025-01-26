package mymusicplayer;

/**
 * MusicPlayer interface defines the essential functionalities for a music player application.
 * It allows for playing, pausing, stopping, adding songs, and managing volume.
 */
public interface MusicPlayer {

    /**
     * Plays the currently selected song.
     */
    void playSong();

    /**
     * Pauses the currently playing song.
     */
    void pauseSong();

    /**
     * Stops the currently playing song.
     */
    void stopSong();

    /**
     * Adds a song to the playlist.
     *
     * @param title          The title of the song.
     * @param path           The file path of the song.
     * @param albumArtPath   The file path of the album art.
     */
    void addSong(String title, String path, String albumArtPath);

    /**
     * Skips to the next song in the playlist.
     */
    void nextSong();

    /**
     * Goes back to the previous song in the playlist.
     */
    void previousSong();

    /**
     * Sets the volume level of the player.
     *
     * @param volume The volume level (0.0 to 1.0).
     */
    void setVolume(float volume);

    /**
     * Gets the current volume level of the player.
     *
     * @return The current volume level.
     */
    float getVolume();

    /**
     * Displays the current song information.
     *
     * @return A string representation of the current song.
     */
    String getCurrentSongInfo();
} 