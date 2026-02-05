package de.st197974.songmanager.ui.panels;

import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.service.DiscographyService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class MultiEditPanel extends JPanel {

    private final DiscographyService discographyService;
    private JTable songTable;
    private DefaultTableModel tableModel;

    private final Color ACCENT_COLOR = new Color(44, 154, 255);
    private final Color BG_COLOR = new Color(245, 245, 247);

    public MultiEditPanel(DiscographyService discographyService) {
        this.discographyService = discographyService;

        setBackground(BG_COLOR);
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        buildUI();
    }

    private void buildUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Multi Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtons.setOpaque(false);

        actionButtons.add(createPrimaryButton("Set Artist", _ -> applyMultiEdit("Artist")));
        actionButtons.add(createPrimaryButton("Set Album", _ -> applyMultiEdit("Album")));
        actionButtons.add(createSecondaryButton("Find Duplicates", _ -> highlightDuplicates()));
        actionButtons.add(createSecondaryButton("Refresh List", _ -> loadAllSongs()));

        headerPanel.add(actionButtons, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Title", "Artist", "Album", "Duration"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        songTable = new JTable(tableModel);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(songTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void styleTable() {
        songTable.setRowHeight(35);
        songTable.setShowVerticalLines(false);
        songTable.setIntercellSpacing(new Dimension(0, 1));
        songTable.setSelectionBackground(new Color(199, 221, 253));
        songTable.setSelectionForeground(Color.BLACK);
        songTable.setFont(new Font("SansSerif", Font.PLAIN, 13));

        songTable.getColumnModel().getColumn(0).setMinWidth(0);
        songTable.getColumnModel().getColumn(0).setMaxWidth(0);

        songTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        songTable.getTableHeader().setReorderingAllowed(false);
    }

    private JButton createPrimaryButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.addActionListener(listener);
        btn.putClientProperty("JButton.buttonType", "square");
        btn.putClientProperty("JButton.arc", 0);
        btn.setBackground(ACCENT_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(150, 35));
        return btn;
    }

    private JButton createSecondaryButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.addActionListener(listener);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(150, 35));
        btn.putClientProperty("JButton.buttonType", "square");
        btn.putClientProperty("JButton.arc", 0);
        return btn;
    }

    public void loadAllSongs() {
        tableModel.setRowCount(0);
        List<Song> songs = discographyService.getAll();
        for (Song s : songs) {
            tableModel.addRow(new Object[]{s.id(), s.title(), s.artist(), s.album(), s.formatTime(s.durationInSeconds())});
        }
    }

    private void applyMultiEdit(String field) {
        int[] selectedRows = songTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select songs in the table first!");
            return;
        }

        String newValue = JOptionPane.showInputDialog(this, "New " + field + " for " + selectedRows.length + " songs:", "Multi Edit", JOptionPane.PLAIN_MESSAGE);

        if (newValue != null && !newValue.trim().isEmpty()) {
            String val = newValue.trim();

            for (int row : selectedRows) {
                String id = (String) tableModel.getValueAt(row, 0);
                Song existing = discographyService.getSongById(id);

                if (existing != null) {
                    Song updated = null;
                    if (field.equals("Artist")) {
                        updated = new Song(existing.id(), existing.title(), existing.album(), val, existing.durationInSeconds());
                    } else if (field.equals("Album")) {
                        updated = new Song(existing.id(), existing.title(), val, existing.artist(), existing.durationInSeconds());
                    }

                    if (updated != null) {
                        discographyService.updateSongSafely(updated);
                    }
                }
            }
            loadAllSongs();
        }
    }

    private void highlightDuplicates() {
        List<Song> allSongs = discographyService.getAll();

        java.util.Map<String, java.util.List<String>> seen = new java.util.HashMap<>();
        java.util.Set<String> duplicateIds = new java.util.HashSet<>();

        for (Song s : allSongs) {

            String key = (s.title() + "|" + s.artist()).toLowerCase().trim();

            if (seen.containsKey(key)) {

                duplicateIds.add(s.id());
                duplicateIds.add(seen.get(key).getFirst());
                seen.get(key).add(s.id());
            } else {
                java.util.List<String> ids = new java.util.ArrayList<>();
                ids.add(s.id());
                seen.put(key, ids);
            }
        }

        if (duplicateIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No duplicates found! Your library is clean.");
            return;
        }

        tableModel.setRowCount(0);
        for (Song s : allSongs) {
            if (duplicateIds.contains(s.id())) {
                tableModel.addRow(new Object[]{s.id(), "⚠ " + s.title(), s.artist(), s.album(), s.formatTime(s.durationInSeconds())});
            }
        }

        JOptionPane.showMessageDialog(this, "Found " + duplicateIds.size() + " potential duplicates!");
    }
}