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
 

public class MyMusicPlayer extends JFrame {
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

    public MyMusicPlayer() {
        playlist = new PlayList();
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Dub Music Player");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLayout(new BorderLayout());

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

        playButton = createButton("Play");
        pauseButton = createButton("Pause");
        nextButton = createButton("Next");
        prevButton = createButton("Previous");
        addButton = createButton("Add Song");
        volumeUpButton = createButton("v+");
        volumeDownButton = createButton("v-");

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

        // Create album art label
        albumArtLabel = new JLabel(new ImageIcon("path/to/default/icon.png"), SwingConstants.CENTER);

        // Equalizer Panel
        equalizerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawEqualizer(g);
            }
        };
        equalizerPanel.setPreferredSize(new Dimension(400, 150));

        // Add components to frame
        add(albumArtLabel, BorderLayout.CENTER);
        add(currentSongLabel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
        add(equalizerPanel, BorderLayout.CENTER);

        // Add button listeners
        addButton.addActionListener(e -> addSong());
        playButton.addActionListener(e -> playSong());
        pauseButton.addActionListener(e -> pauseSong());
        nextButton.addActionListener(e -> playNext());
        prevButton.addActionListener(e -> playPrevious());
        volumeUpButton.addActionListener(e -> increaseVolume());
        volumeDownButton.addActionListener(e -> decreaseVolume());
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(70, 130, 180)); // Steel blue color
        button.setForeground(Color.WHITE); // White text
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(80, 30));
        button.setFont(new Font("Arial", Font.BOLD, 12));
        return button;
    }

    private void addSong() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Audio Files", "wav", "mp3");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String defaultAlbumArtPath = "path/to/default/icon.png"; // Change this to your default path
            playlist.addSong(selectedFile.getName(), selectedFile.getAbsolutePath(), defaultAlbumArtPath);
            JOptionPane.showMessageDialog(this, "Song added to playlist!");
        }
    }

    private void playSong() {
        try {
            if (playlist.getCurrentSong() != null) {
                // Close the previous player if it exists
                if (player != null) {
                    player.close();
                }

                // Open the MP3 file
                File mp3File = new File(playlist.getCurrentSong().getPath());
                System.out.println("Attempting to play: " + mp3File.getAbsolutePath());
                FileInputStream fis = new FileInputStream(mp3File);
                BufferedInputStream bis = new BufferedInputStream(fis);

                // Create an instance of AdvancedPlayer
                player = new AdvancedPlayer(bis);

                // Start playing in a new thread
                new Thread(() -> {
                    try {
                        player.play();
                    } catch (JavaLayerException e) {
                        JOptionPane.showMessageDialog(this, "Error playing audio: " + e.getMessage());
                    }
                }).start();

                // Update UI with album art and current song label
                String albumArtPath = playlist.getCurrentSong().getAlbumArtPath();
                File albumArtFile = new File(albumArtPath);
                if (albumArtFile.exists()) {
                    albumArtLabel.setIcon(new ImageIcon(albumArtPath));
                } else {
                    albumArtLabel.setIcon(new ImageIcon("path/to/default/icon.png"));
                }

                // Set the current song label
                currentSongLabel.setText("Now playing: " + playlist.getCurrentSong().getTitle());
                isPlaying = true;
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "File not found: " + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error playing song: " + e.getMessage());
        }
    }

    private void pauseSong() {
        if (player != null && isPlaying) {
            player.close();
            isPlaying = false;
        }
    }

    private void playNext() {
        if (player != null) {
            player.close();
        }
        playlist.nextSong();
        playSong();
    }

    private void playPrevious() {
        if (player != null) {
            player.close();
        }
        playlist.previousSong();
        playSong();
    }

    private void increaseVolume() {
        if (volume < 1.0f) {
            volume += 0.1f; // Increase volume by 10%
            System.out.println("Volume increased to: " + volume);
        }
    }

    private void decreaseVolume() {
        if (volume > 0.0f) {
            volume -= 0.1f; // Decrease volume by 10%
            System.out.println("Volume decreased to: " + volume);
        }
    }

    private void drawEqualizer(Graphics g) {
        int barWidth = 10;
        int barHeight;
        int[] frequencyLevels = new int[40]; // Array to hold frequency levels

        // Generate random frequency levels for demonstration
        for (int i = 0; i < frequencyLevels.length; i++) {
            frequencyLevels[i] = (int) (Math.random() * 100);
            g.setColor(Color.GREEN);
            g.fillRect(i * barWidth, 150 - frequencyLevels[i], barWidth - 1, frequencyLevels[i]);
        }

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

        albumArtLabel.setIcon(new ImageIcon(albumArtPath));
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
            timer.stop();
        }
    }

    private void analyzeAudioFrequencies() {
        try {
            File mp3File = new File(playlist.getCurrentSong().getPath());
            
            if (!mp3File.exists()) {
                JOptionPane.showMessageDialog(this, "File not found: " + mp3File.getPath());
                return;
            }

            AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(mp3File, 1024, 512);
            
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

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error analyzing audio: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MyMusicPlayer().setVisible(true);
        });
    }
}
