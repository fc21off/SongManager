package de.firecreeper21.swiftmanager.ui;

import de.firecreeper21.swiftmanager.model.Song;
import de.firecreeper21.swiftmanager.service.DiscographyService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class SwiftManagerWindow extends JFrame{

    private final DiscographyService service;
    private final DefaultListModel<String> listModel;

    public SwiftManagerWindow(DiscographyService service) {
        this.service = service;
        this.listModel = new DefaultListModel<>();

        setTitle("SwiftManager 1.0");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));

        JPanel searchPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField();
        searchPanel.add(new JLabel(" Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.NORTH); // Ab in den Norden damit!

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {filter();}

            @Override
            public void removeUpdate(DocumentEvent e) {filter();}

            @Override
            public void changedUpdate(DocumentEvent e) {filter();}

            private void filter() {
                String query = searchField.getText();
                updateList(query); // Wir geben den Suchbegriff an updateList weiter
            }
        });

        JList<String> songList = new JList<>(listModel);
        songList.setBackground(new Color(30, 30, 30));
        songList.setForeground(Color.WHITE);
        songList.setSelectionBackground(new Color(30, 215, 96));
        add(new JScrollPane(songList), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        JTextField titleField = new JTextField();
        JTextField albumField = new JTextField();
        JTextField durationField = new JTextField();
        JButton addButton = new JButton("Add Song");

        inputPanel.add(new JLabel(" Enter Title: "));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel(" Enter Album: "));
        inputPanel.add(albumField);
        inputPanel.add(new JLabel(" Enter Duration (in s): "));
        inputPanel.add(durationField);
        inputPanel.add(new JLabel(""));
        inputPanel.add(addButton);

        add(inputPanel, BorderLayout.SOUTH);

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

        setVisible(true);

    }

    private void updateList(String query) {
        listModel.clear();
        for (Song s : service.getAll()) {
            // Wir prüfen, ob der Titel oder das Album den Suchbegriff enthält (ignorieren Groß/Kleinschreibung)
            if (query.isEmpty() ||
                    s.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    s.getAlbum().toLowerCase().contains(query.toLowerCase())) {

                listModel.addElement(s.toDisplayString());
            }
        }
    }

}
