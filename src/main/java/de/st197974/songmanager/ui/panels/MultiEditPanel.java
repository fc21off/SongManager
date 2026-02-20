package de.st197974.songmanager.ui.panels;

import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.service.DiscographyService;
import de.st197974.songmanager.service.FavoritesService;
import de.st197974.songmanager.ui.AppTheme;
import de.st197974.songmanager.ui.SongManagerUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class MultiEditPanel extends JPanel {

    private final DiscographyService discographyService;
    private final FavoritesService favoritesService;

    private final SongManagerUI mainUI;

    private JTable songTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private JLabel titleLabel;

    private JButton cleanupButton;

    private final List<JButton> primaryButtons = new ArrayList<>();

    public MultiEditPanel(DiscographyService discographyService, FavoritesService favoritesService, SongManagerUI mainUI) {
        this.discographyService = discographyService;
        this.favoritesService = favoritesService;
        this.mainUI = mainUI;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        buildUI();
        setupTableContextMenu();

        updateThemeColors();
    }


    @Override
    public void updateUI() {
        super.updateUI();
        if (songTable != null) {
            updateThemeColors();
        }
    }

    private void updateThemeColors() {
        Color bg = AppTheme.isDark() ? UIManager.getColor("Panel.background") : new Color(245, 245, 247);
        setBackground(bg);

        if (titleLabel != null) {
            titleLabel.setForeground(AppTheme.isDark() ? Color.WHITE : Color.BLACK);
        }

        if (songTable != null) {
            Color tableBg = AppTheme.isDark() ? UIManager.getColor("Table.background") : Color.WHITE;
            Color tableFg = AppTheme.isDark() ? Color.WHITE : Color.BLACK;
            Color gridColor = AppTheme.isDark() ? new Color(60, 60, 60) : new Color(220, 220, 220);

            songTable.setBackground(tableBg);
            songTable.setForeground(tableFg);
            songTable.setGridColor(gridColor);

            songTable.setSelectionBackground(AppTheme.selection());
            songTable.setSelectionForeground(AppTheme.isDark() ? Color.WHITE : Color.BLACK);

            songTable.getTableHeader().setBackground(AppTheme.isDark() ? new Color(50, 50, 50) : new Color(240, 240, 240));
            songTable.getTableHeader().setForeground(AppTheme.isDark() ? Color.LIGHT_GRAY : Color.BLACK);
        }

        if (scrollPane != null) {
            Color borderColor = AppTheme.isDark() ? new Color(60, 60, 60) : new Color(220, 220, 220);
            scrollPane.setBorder(BorderFactory.createLineBorder(borderColor));
        }

        for (JButton btn : primaryButtons) {
            btn.setBackground(AppTheme.accent());
        }
    }

    private void buildUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        titleLabel = new JLabel("Multi Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtons.setOpaque(false);

        actionButtons.add(createPrimaryButton("Set Artist", _ -> applyMultiEdit("Artist")));
        actionButtons.add(createPrimaryButton("Set Album", _ -> applyMultiEdit("Album")));
        actionButtons.add(createSecondaryButton("Delete Selected", _ -> deleteSelectedSongs()));
        actionButtons.add(createSecondaryButton("Find Duplicates", _ -> highlightDuplicates()));
        actionButtons.add(createSecondaryButton("Refresh List", _ -> loadAllSongs()));

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);

        cleanupButton = createCleanupButton("Cleanup Duplicates", _ -> autoCleanup());
        cleanupButton.setVisible(false);

        headerPanel.add(actionButtons, BorderLayout.EAST);
        footerPanel.add(cleanupButton);

        add(headerPanel, BorderLayout.NORTH);
        add(footerPanel, BorderLayout.SOUTH);

        String[] columns = {"ID", "Title", "Artist", "Album", "Duration"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        songTable = new JTable(tableModel);
        styleTableStructure();

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        songTable.setRowSorter(sorter);

        scrollPane = new JScrollPane(songTable);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void styleTableStructure() {
        songTable.setRowHeight(35);
        songTable.setShowVerticalLines(false);
        songTable.setIntercellSpacing(new Dimension(0, 1));
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

        btn.setBackground(AppTheme.accent());
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(150, 35));

        primaryButtons.add(btn);
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

    private JButton createCleanupButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.addActionListener(listener);
        btn.putClientProperty("JButton.buttonType", "square");
        btn.putClientProperty("JButton.arc", 0);

        btn.setBackground(AppTheme.accent());
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(250, 35));

        primaryButtons.add(btn);
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
                int modelRow = songTable.convertRowIndexToModel(row);
                String id = (String) tableModel.getValueAt(modelRow, 0);

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
            cleanupButton.setVisible(false);
            revalidate();
            JOptionPane.showMessageDialog(this, "No duplicates found! Your library is clean.");
            return;
        }

        tableModel.setRowCount(0);
        for (Song s : allSongs) {
            if (duplicateIds.contains(s.id())) {
                tableModel.addRow(new Object[]{s.id(), "⚠ " + s.title(), s.artist(), s.album(), s.formatTime(s.durationInSeconds())});
            }
        }

        cleanupButton.setVisible(true);
        revalidate();
        repaint();

        JOptionPane.showMessageDialog(this, "Found " + duplicateIds.size() + " potential duplicates!");
    }

    private void autoCleanup() {
        List<Song> allSongs = discographyService.getAll();

        java.util.Map<String, List<Song>> groups = allSongs.stream().collect(java.util.stream.Collectors.groupingBy(s -> (s.title() + "|" + s.artist()).toLowerCase().trim()));

        int deletedCount = 0;
        for (List<Song> group : groups.values()) {
            if (group.size() > 1) {
                Song keepThisOne = group.stream().min((s1, s2) -> {
                    boolean f1 = favoritesService.isFavorite(s1.id());
                    boolean f2 = favoritesService.isFavorite(s2.id());
                    if (f1 && !f2) return -1;
                    if (!f1 && f2) return 1;
                    return 0;
                }).get();

                for (Song s : group) {
                    if (!s.id().equals(keepThisOne.id())) {
                        discographyService.deleteSong(s.id());
                        deletedCount++;
                    }
                }

            }
        }

        cleanupButton.setVisible(false);
        loadAllSongs();
        revalidate();
        repaint();
        JOptionPane.showMessageDialog(this, "Deleted " + deletedCount + " duplicates!");

    }

    private void setupTableContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem showInLibrary = getItem();

        JMenuItem editSong = new JMenuItem("Edit Song");
        editSong.addActionListener(_ -> {
            int row = songTable.getSelectedRow();
            if (row != -1) {
                int modelRow = songTable.convertRowIndexToModel(row);
                String id = (String) tableModel.getValueAt(modelRow, 0);
                Song s = discographyService.getSongById(id);
                if (s != null) {
                    mainUI.showSongForm(s);
                    loadAllSongs();
                }
            }
        });

        JMenuItem deleteSong = new JMenuItem("Delete Song");
        deleteSong.addActionListener(_ -> {
            int row = songTable.getSelectedRow();
            if (row != -1) {
                int modelRow = songTable.convertRowIndexToModel(row);
                String id = (String) tableModel.getValueAt(modelRow, 0);
                Song s = discographyService.getSongById(id);
                if (s != null) {
                    int confirm = JOptionPane.showConfirmDialog(this, "Delete '" + s.title() + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        discographyService.deleteSong(s.id());
                        loadAllSongs();
                    }
                }
            }
        });

        popupMenu.add(showInLibrary);
        popupMenu.addSeparator();
        popupMenu.add(editSong);
        popupMenu.add(deleteSong);

        songTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                handlePopup(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                handlePopup(e);
            }

            private void handlePopup(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {

                    int row = songTable.rowAtPoint(e.getPoint());

                    if (row != -1) {
                        songTable.setRowSelectionInterval(row, row);

                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

    }

    private JMenuItem getItem() {
        JMenuItem showInLibrary = new JMenuItem("Show in Library");
        showInLibrary.addActionListener(_ -> {
            int row = songTable.getSelectedRow();
            if (row != -1) {
                int modelRow = songTable.convertRowIndexToModel(row);
                String id = (String) tableModel.getValueAt(modelRow, 0);
                Song s = discographyService.getSongById(id);
                if (s != null) {
                    mainUI.navigateToSong(s);
                }
            }
        });
        return showInLibrary;
    }

    public Song getSelectedSongFromTable() {
        int row = songTable.getSelectedRow();
        if (row != -1) {
            int modelRow = songTable.convertRowIndexToModel(row);
            String id = (String) tableModel.getValueAt(modelRow, 0);
            return discographyService.getSongById(id);
        }
        return null;
    }

    private void deleteSelectedSongs() {
        int[] selectedRows = songTable.getSelectedRows();

        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Select Songs first!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete " + selectedRows.length + " songs?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            for (int row : selectedRows) {

                Song currentSong = discographyService.getSongById((String) tableModel.getValueAt(songTable.convertRowIndexToModel(row), 0));
                boolean isFav = favoritesService.isFavorite(currentSong.id());

                if (isFav) {
                    favoritesService.removeFavorite((String) tableModel.getValueAt(songTable.convertRowIndexToModel(row), 0));
                }

                int modelRow = songTable.convertRowIndexToModel(row);
                String id = (String) tableModel.getValueAt(modelRow, 0);

                discographyService.deleteSong(id);
            }

            loadAllSongs();
            JOptionPane.showMessageDialog(this, "Deleted " + selectedRows.length + " songs!");

        }

    }

}