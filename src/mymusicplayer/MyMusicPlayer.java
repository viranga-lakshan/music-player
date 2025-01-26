 package mymusicplayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javazoom.jl.player.Player;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import be.tarsos.dsp.AudioEvent;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javax.swing.JOptionPane;
import java.io.FileNotFoundException;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;

public class MyMusicPlayer extends JFrame implements MusicPlayer {
    private PlayList playlist;
    private JButton playButton, pauseButton, nextButton, prevButton, addButton;
    private JButton volumeUpButton, volumeDownButton;
    private JLabel currentSongLabel;
    private JLabel albumArtLabel;
    private AdvancedPlayer player;
    private boolean isPlaying = false;
    private float volume = 1.0f; // Volume level (0.0 to 1.0)
    private JSlider volumeSlider;
    private JPanel equalizerPanel;
    private Timer timer;
    private JList<String> songList;
    private DefaultListModel<String> songListModel;
    private int[] frequencyLevels; // Declare frequencyLevels here
    private AudioDispatcher dispatcher;

    public MyMusicPlayer() {
        playlist = new PlayList();
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Dub Music Player");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLayout(new BorderLayout());

        // Set background color
        getContentPane().setBackground(new Color(0, 0, 139)); // Dark blue color

        // Volume Control
        volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setMajorTickSpacing(10);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        add(volumeSlider, BorderLayout.NORTH);

        // Create buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(50, 50, 50)); // Darker button panel
        buttonPanel.setLayout(new FlowLayout());

        playButton = createButton("", new Color(70, 130, 180), Color.WHITE, "src/mymusicplayer/icons/play (4).png");
        pauseButton = createButton("", new Color(70, 130,180), Color.WHITE, "src/mymusicplayer/icons/pause.png");
        nextButton = createButton("", new Color(70, 130,180), Color.WHITE, "src/mymusicplayer/icons/f.png");
        prevButton = createButton("", new Color(70, 130,180), Color.WHITE,"src/mymusicplayer/icons/p.png"); // No icon for previous
        addButton = createButton("Add Song", new Color( 70, 130,180), Color.WHITE, null); // No icon for add song
        volumeUpButton = createButton("v+", new Color( 70, 130,180), Color.WHITE, null); // No icon for volume up
        volumeDownButton = createButton("v-", new Color( 70, 130,180), Color.WHITE, null); // No icon for volume down

        buttonPanel.add(prevButton);
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(addButton);
        buttonPanel.add(volumeDownButton);
        buttonPanel.add(volumeUpButton);

        // Create song info panel
        currentSongLabel = new JLabel("No song playing", SwingConstants.CENTER);
        currentSongLabel.setForeground(Color.WHITE); // White text for song info
        currentSongLabel.setBackground(new Color(30, 30, 30)); // Dark background
        currentSongLabel.setOpaque(true); // Make background visible

        // Create album art label
        albumArtLabel = new JLabel(new ImageIcon("path/to/default/icon.png"), SwingConstants.CENTER);
        albumArtLabel.setBackground(new Color(30, 30, 30)); // Dark background
        albumArtLabel.setOpaque(true); // Make background visible

        // Equalizer Panel
        equalizerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawEqualizer(g);
            }
        };
        equalizerPanel.setPreferredSize(new Dimension(400, 150));

        // Initialize the song list model and JList
        songListModel = new DefaultListModel<>();
        songList = new JList<>(songListModel);
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane songListScrollPane = new JScrollPane(songList);
        songListScrollPane.setPreferredSize(new Dimension(150, 200)); // Set preferred size

        // Add components to frame
        add(albumArtLabel, BorderLayout.CENTER);
        add(currentSongLabel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
        add(equalizerPanel, BorderLayout.CENTER);
        add(songListScrollPane, BorderLayout.WEST); // Add to the left side of the frame

        // Add button listeners
        addButton.addActionListener(e -> addSong());
        playButton.addActionListener(e -> playSong());
        pauseButton.addActionListener(e -> pauseSong());
        nextButton.addActionListener(e -> nextSong());
        prevButton.addActionListener(e -> previousSong());
        volumeUpButton.addActionListener(e -> increaseVolume());
        volumeDownButton.addActionListener(e -> decreaseVolume());
    }

    private JButton createButton(String text, Color backgroundColor, Color textColor, String iconPath) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(100, 40)); // Adjust size as needed

        // Set the icon
        if (iconPath != null) {
            ImageIcon icon = new ImageIcon(iconPath);
            // Scale the icon to fit the button
            Image scaledImage = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImage));
            button.setHorizontalTextPosition(SwingConstants.CENTER); // Center text horizontally
            button.setVerticalTextPosition(SwingConstants.BOTTOM); // Position text below the icon
        }

        button.setFont(new Font("Arial", Font.BOLD, 12));
        
        return button;
    }

    @Override
    public void playSong() {
        try {
            if (playlist.getCurrentSong() != null) {
                if (player != null) {
                    player.close();
                }

                // Stop the audio dispatcher if it's running
                stopAudioDispatcher();

                File mp3File = new File(playlist.getCurrentSong().getPath());
                FileInputStream fis = new FileInputStream(mp3File);
                BufferedInputStream bis = new BufferedInputStream(fis);
                player = new AdvancedPlayer(bis);
                new Thread(() -> {
                    try {
                        player.play();
                        analyzeAudioFrequencies(); // Start analyzing frequencies
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, "Error playing audio: " + e.getMessage());
                    }
                }).start();

                currentSongLabel.setText("Now playing: " + playlist.getCurrentSong().getTitle());
                isPlaying = true;
                startEqualizer(); // Start the equalizer
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error playing song: " + e.getMessage());
        }
    }

    @Override
    public void pauseSong() {
        if (player != null && isPlaying) {
            player.close();
            isPlaying = false;
            stopEqualizer(); // Stop the equalizer
            stopAudioDispatcher(); // Stop the audio dispatcher if it's running
        }
    }

    @Override
    public void stopSong() {
        if (player != null) {
            player.close();
            isPlaying = false;
            stopEqualizer(); // Stop the equalizer
        }
    }

    @Override
    public void addSong(String title, String path, String albumArtPath) {
        playlist.addSong(title, path, albumArtPath);
        songListModel.addElement(title); // Add song to the JList
        JOptionPane.showMessageDialog(this, "Song added to playlist!");
    }

    @Override
    public void nextSong() {
        if (player != null) {
            player.close();
        }
        stopAudioDispatcher(); // Stop the audio dispatcher if it's running
        playlist.nextSong();
        playSong();
    }

    @Override
    public void previousSong() {
        if (player != null) {
            player.close();
        }
        stopAudioDispatcher(); // Stop the audio dispatcher if it's running
        playlist.previousSong();
        playSong();
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
        // Implement volume adjustment logic here
    }

    @Override
    public float getVolume() {
        return volume;
    }

    private void setPlayerVolume(float volume) {
        // Assuming the AdvancedPlayer has a method to set volume
        if (player != null) {
            // This is a placeholder; you need to implement the actual volume adjustment logic
            // For example, if using a library that supports volume adjustment, apply it here
            // player.setVolume(volume); // Uncomment and implement this line based on your audio library
        }
    }

    private void increaseVolume() {
        if (volume < 1.0f) {
            volume += 0.1f; // Increase volume by 10%
            System.out.println("Volume increased to: " + volume);
            setPlayerVolume(volume); // Apply the volume change
        }
    }

    private void decreaseVolume() {
        if (volume > 0.0f) {
            volume -= 0.1f; // Decrease volume by 10%
            System.out.println("Volume decreased to: " + volume);
            setPlayerVolume(volume); // Apply the volume change
        }
    }

    private void drawEqualizer(Graphics g) {
        int barWidth = 20; // Increased bar width for larger bars
        frequencyLevels = new int[40]; // Initialize frequencyLevels here

        // Generate random frequency levels for demonstration
        for (int i = 0; i < frequencyLevels.length; i++) {
            frequencyLevels[i] = (int) (Math.random() * 150); // Increased max height for bars
            g.setColor(Color.GREEN);
            g.fillRect(i * barWidth, 150 - frequencyLevels[i], barWidth - 1, frequencyLevels[i]);
        }

        // Load and draw the image with a larger size
        ImageIcon equalizerImage = new ImageIcon("path/to/equalizer/image.png"); // Update with your image path
        g.drawImage(equalizerImage.getImage(), 0, 0, 800, 300, null); // Draw the image larger

        // Change album art based on frequency levels
        updateAlbumArt(frequencyLevels);
    }

    private void updateAlbumArt(int[] frequencyLevels) {
        int averageFrequency = 0;
        for (int level : frequencyLevels) {
            averageFrequency += level;
        }
        averageFrequency /= frequencyLevels.length;

        String albumArtPath;
        if (averageFrequency < 30) {
            albumArtPath = "path/to/low_frequency_art.png"; // Low frequency art
        } else if (averageFrequency < 70) {
            albumArtPath = "path/to/medium_frequency_art.png"; // Medium frequency art
        } else {
            albumArtPath = "path/to/high_frequency_art.png"; // High frequency art
        }

        // Update the album art label with the new image
        ImageIcon albumArtIcon = new ImageIcon(albumArtPath);
        albumArtLabel.setIcon(albumArtIcon);
    }

    private void startEqualizer() {
        timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                equalizerPanel.repaint();
            }
        });
        timer.start();
    }

    private void stopEqualizer() {
        if (timer != null) {
            timer.stop(); // Stop the timer
            timer = null; // Clear the reference
        }
    }

    private void analyzeAudioFrequencies() {
        try {
            File mp3File = new File(playlist.getCurrentSong().getPath());
            
            if (!mp3File.exists()) {
                // Log the error instead of showing a message
                System.err.println("File not found: " + mp3File.getPath());
                return;
            }

            dispatcher = AudioDispatcherFactory.fromFile(mp3File, 1024, 512);
            
            PitchDetectionHandler handler = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult result, AudioEvent event) {
                    float pitch = result.getPitch();
                    if (pitch != -1) {
                        System.out.println("Detected pitch: " + pitch);
                    }
                }
            };

            dispatcher.addAudioProcessor(new PitchProcessor(PitchEstimationAlgorithm.YIN, 44100, 1024, handler));
            new Thread(dispatcher, "Audio Dispatcher").start();

            // Update frequency levels and redraw equalizer
            frequencyLevels = new int[40]; // Ensure frequencyLevels is initialized
            for (int i = 0; i < frequencyLevels.length; i++) {
                frequencyLevels[i] = (int) (Math.random() * 100); // Replace with actual frequency analysis
            }
            equalizerPanel.repaint(); // Trigger a repaint to update the equalizer display

        } catch (Exception e) {
            // Log the error instead of showing a message
            System.err.println("Error analyzing audio: " + e.getMessage());
        }
    }

    private void addSong() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Audio Files", "wav", "mp3");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String title = selectedFile.getName(); // Use the file name as the title
            String path = selectedFile.getAbsolutePath();
            String defaultAlbumArtPath = "path/to/default/icon.png"; // Change this to your default path

            // Call the addSong method with the required parameters
            playlist.addSong(title, path, defaultAlbumArtPath);
            songListModel.addElement(title); // Add song to the JList
            JOptionPane.showMessageDialog(this, "Song added to playlist!");
        }
    }

    @Override
    public String getCurrentSongInfo() {
        // Implement logic to return current song information
        return playlist.getCurrentSong() != null ? playlist.getCurrentSong().getTitle() : "No song playing";
    }

    private void stopAudioDispatcher() {
        // Implement logic to stop the audio dispatcher if it's running
        // This may involve keeping a reference to the dispatcher and stopping it
        // For example:
        if (dispatcher != null) {
            dispatcher.stop(); // Assuming you have a reference to the dispatcher
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MyMusicPlayer().setVisible(true);
        });
    }
}