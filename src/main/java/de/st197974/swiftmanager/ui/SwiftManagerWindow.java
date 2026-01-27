package de.st197974.swiftmanager.ui;

import de.st197974.swiftmanager.model.Song;
import de.st197974.swiftmanager.service.DiscographyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class SwiftManagerWindow extends JFrame{

    private final DiscographyService service;
    private final DefaultListModel<Song> listModel;
    private final JLabel statusBar;

    private static final Logger logger = LogManager.getLogger(SwiftManagerWindow.class);

    public SwiftManagerWindow(DiscographyService service) {
        this.service = service;
        this.listModel = new DefaultListModel<>();

        setTitle("SwiftManager 1.0");
        setSize(500, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));
        setResizable(false);

        JPanel searchPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField();
        searchPanel.add(new JLabel(" Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.NORTH);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {filter();}

            @Override
            public void removeUpdate(DocumentEvent e) {filter();}

            @Override
            public void changedUpdate(DocumentEvent e) {filter();}

            private void filter() {
                String query = searchField.getText();
                updateList(query);
            }
        });

        JList<Song> songList = new JList<>(listModel);
        songList.setBackground(new Color(30, 30, 30));
        songList.setForeground(Color.WHITE);
        songList.setSelectionBackground(new Color(30, 215, 96));
        add(new JScrollPane(songList), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        JTextField titleField = new JTextField();
        JTextField albumField = new JTextField();
        JTextField durationField = new JTextField();
        JButton addButton = new JButton("Add Song");

        JButton deleteButton = new JButton("DEL");
        deleteButton.setBackground(new Color(200, 50, 50));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);

        JButton sortByAlbumButton = new JButton("SORT (ALB)");
        sortByAlbumButton.setBackground(new Color(52, 155, 235));
        sortByAlbumButton.setForeground(Color.BLACK);
        sortByAlbumButton.setFocusPainted(false);

        JButton sortByDurationButton = new JButton("Sort (DUR)");
        sortByDurationButton.setBackground(new Color(255, 207, 36));
        sortByDurationButton.setForeground(Color.BLACK);
        sortByDurationButton.setFocusPainted(false);

        JButton getSongInfoButton = new JButton("SONG INFO");
        getSongInfoButton.setBackground(new Color(30, 215, 96));
        getSongInfoButton.setForeground(Color.BLACK);
        getSongInfoButton.setFocusPainted(false);

        statusBar = new JLabel();
        statusBar.setForeground(Color.GRAY);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel southContainer = new JPanel();
        southContainer.setLayout(new BoxLayout(southContainer, BoxLayout.Y_AXIS));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(sortByAlbumButton);
        buttonPanel.add(sortByDurationButton);
        buttonPanel.add(getSongInfoButton);
        buttonPanel.add(deleteButton);

        southContainer.add(statusBar);
        southContainer.add(buttonPanel);
        southContainer.add(inputPanel);

        inputPanel.add(new JLabel(" Enter Title: "));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel(" Enter Album: "));
        inputPanel.add(albumField);
        inputPanel.add(new JLabel(" Enter Duration (in s): "));
        inputPanel.add(durationField);
        inputPanel.add(new JLabel(""));
        inputPanel.add(addButton);

        add(southContainer, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {

            try {
                String title = titleField.getText();
                String album = albumField.getText();
                int duration = Integer.parseInt(durationField.getText());

                Song newSong = new Song(title, album, duration);
                service.addSongSafely(newSong);

                updateList("");

                titleField.setText("");
                albumField.setText("");
                durationField.setText("");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a Number for the Duration!");
            }

        });

        deleteButton.addActionListener(e -> {

            Song selectedSong = songList.getSelectedValue();

            if(selectedSong != null) {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete '" + selectedSong.getTitle() + "'?", "Confirm", JOptionPane.YES_NO_OPTION);

                if(confirm == JOptionPane.YES_OPTION) {
                    service.deleteSong(selectedSong.getId());
                    updateList("");
                    logger.info("UI requested deletion of song!");
                }

            } else {
                JOptionPane.showMessageDialog(this, "Select a song first!");
            }

        });

        getSongInfoButton.addActionListener(e -> {

            Song selectedSong = songList.getSelectedValue();

            if(selectedSong != null) {

                int totalSeconds = selectedSong.getDurationInSeconds();

                int hours = totalSeconds / 3600;
                int minutes = (totalSeconds % 3600) / 60;
                int seconds = totalSeconds % 60;

                String timeStr;
                if(hours > 0) {
                    timeStr = String.format("%d:%02d:%02d", hours, minutes, seconds);
                } else {
                    timeStr = String.format("%02d:%02d", minutes, seconds);
                }

                String info = String.format(
                        "Title: %s\nAlbum: %s\nDuration: %s \n\nID:\n%s",
                        selectedSong.getTitle(),
                        selectedSong.getAlbum(),
                        timeStr,
                        selectedSong.getId()
                );

                JTextArea textArea = new JTextArea();
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setForeground(Color.BLACK);
                textArea.setFont(new Font("Consolas", Font.PLAIN, 13));

                textArea.setText(info);

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(380, 200));

                JOptionPane.showMessageDialog(
                        this,
                        scrollPane,
                        "Song Information",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {
                JOptionPane.showMessageDialog(this, "Select a song first!");
            }

        });

        sortByAlbumButton.addActionListener(e -> {
            updateListSortedByAlbum();
        });

        sortByDurationButton.addActionListener(e -> {
            updateListSortedByDuration();
        });


        updateList("");
        updateStatusBar();

        setVisible(true);

    }

    private void updateList(String query) {
        listModel.clear();
        for (Song s : service.getAll()) {
            if (query.isEmpty() ||
                    s.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    s.getAlbum().toLowerCase().contains(query.toLowerCase())) {

                listModel.addElement(s);
            }
        }

        updateStatusBar();

    }

    private void updateListSortedByAlbum(){
        listModel.clear();

        List<Song> sortedList = service.getAllSortedByAlbum();

        for(Song s : sortedList) {
            listModel.addElement(s);
        }

        updateStatusBar();

    }

    private void updateListSortedByDuration() {
        listModel.clear();

        List<Song> sortedList = service.getAllSortedByDuration();

        for(Song s : sortedList) {
            listModel.addElement(s);
        }

        updateStatusBar();

    }

    private void updateStatusBar(){

        List<Song> allSongs = service.getAll();
        int count = allSongs.size();

        int totalSeconds = allSongs.stream()
                .mapToInt(Song::getDurationInSeconds)
                .sum();

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        String timeStr;
        if(hours > 0) {
            timeStr = String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeStr = String.format("%02d:%02d", minutes, seconds);
        }

        statusBar.setText(String.format("Total Songs: %d | Total Time: %s", count, timeStr));

    }

}
