package de.st197974.songmanager.ui.panels;

import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.service.FavoritesService;
import de.st197974.songmanager.ui.AppTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class FavoritesPanel extends JPanel {

    private final FavoritesService favoritesService;
    private final DefaultListModel<Song> favoriteModel = new DefaultListModel<>();
    private JList<Song> favoriteList;

    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton removeBtn;
    private JPanel headerPanel;

    public FavoritesPanel(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;

        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        buildUI();
        loadFavorites();

        updateThemeColors();
    }

    @Override
    public void updateUI() {
        super.updateUI();

        if (titleLabel != null) {
            updateThemeColors();
        }
    }

    private void updateThemeColors() {
        Color bg = AppTheme.isDark() ? UIManager.getColor("Panel.background") : new Color(245, 245, 247);
        setBackground(bg);

        if (titleLabel != null) {
            titleLabel.setForeground(AppTheme.isDark() ? Color.WHITE : new Color(50, 50, 50));
            subtitleLabel.setForeground(AppTheme.isDark() ? Color.LIGHT_GRAY : Color.GRAY);
        }

        if (favoriteList != null) {
            favoriteList.setBackground(AppTheme.isDark() ? UIManager.getColor("List.background") : Color.WHITE);
        }

        if (removeBtn != null) {
            styleRemoveButton(removeBtn);
        }
    }

    private void buildUI() {
        headerPanel = createHeaderPanel();

        favoriteList = new JList<>(favoriteModel);
        favoriteList.setFixedCellHeight(40);
        favoriteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        favoriteList.setCellRenderer(createFavoriteRenderer());

        JScrollPane scrollPane = new JScrollPane(favoriteList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);

        leftActions.add(createSortButton("Sort A-Z", _ -> sortSongsAlphabetically()));
        leftActions.add(createSortButton("Sort Artist", _ -> sortSongsByArtist()));

        removeBtn = new JButton("Remove from Favorites");

        removeBtn.addActionListener(_ -> removeFavorite());

        actionPanel.add(leftActions, BorderLayout.WEST);
        actionPanel.add(removeBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        titleLabel = new JLabel("Your Favorite Songs");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        subtitleLabel = new JLabel("Easy overview over all your favorites!");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel textWrapper = new JPanel(new GridLayout(2, 1));
        textWrapper.setOpaque(false);
        textWrapper.add(titleLabel);
        textWrapper.add(subtitleLabel);

        panel.add(textWrapper, BorderLayout.WEST);
        return panel;
    }

    private DefaultListCellRenderer createFavoriteRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel itemPanel = new JPanel(new BorderLayout(15, 0));

                Color normalBg = AppTheme.isDark() ? UIManager.getColor("List.background") : Color.WHITE;
                itemPanel.setBackground(isSelected ? AppTheme.selection() : normalBg);

                Color borderColor = AppTheme.isDark() ? new Color(60, 60, 60) : new Color(220, 220, 220);
                itemPanel.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, borderColor), new EmptyBorder(5, 15, 5, 15)));

                if (value instanceof Song song) {

                    String titleHex = AppTheme.isDark() ? (isSelected ? "ffffff" : "dddddd") : "000000";
                    String starColor = isSelected ? (AppTheme.isDark() ? "#FFD700" : "#FFAE00") : "#FFAE00";

                    JLabel titleLabel = new JLabel("<html><span style='color: " + starColor + ";'>★</span> <b><font color='#" + titleHex + "'>" + song.title() + "</font></b></html>");
                    titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

                    JLabel artistLabel = new JLabel(song.artist());

                    Color artistColor;
                    if (isSelected) {
                        artistColor = AppTheme.isDark() ? Color.LIGHT_GRAY : Color.DARK_GRAY;
                    } else {
                        artistColor = Color.GRAY;
                    }
                    artistLabel.setForeground(artistColor);

                    artistLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));

                    itemPanel.add(titleLabel, BorderLayout.CENTER);
                    itemPanel.add(artistLabel, BorderLayout.EAST);
                }
                return itemPanel;
            }
        };
    }

    private void styleRemoveButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(200, 30));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        btn.putClientProperty("JButton.buttonType", "square");
        btn.putClientProperty("JButton.arc", 0);

        Color danger = AppTheme.danger();
        Color bg = AppTheme.isDark() ? UIManager.getColor("Panel.background") : Color.WHITE;

        btn.setBackground(bg);
        btn.setForeground(danger);
        btn.setBorder(BorderFactory.createLineBorder(danger, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        for (var ml : btn.getMouseListeners()) {
            if (ml instanceof DangerHoverListener) btn.removeMouseListener(ml);
        }

        btn.addMouseListener(new DangerHoverListener(btn));
    }

    private static class DangerHoverListener extends MouseAdapter {
        private final JButton btn;

        public DangerHoverListener(JButton btn) {
            this.btn = btn;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            btn.setBackground(AppTheme.danger());
            btn.setForeground(Color.WHITE);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            btn.setBackground(AppTheme.isDark() ? UIManager.getColor("Panel.background") : Color.WHITE);
            btn.setForeground(AppTheme.danger());
        }

        @Override
        public void mousePressed(MouseEvent e) {
            btn.setBackground(AppTheme.danger().darker());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (btn.getBounds().contains(e.getPoint())) {
                btn.setBackground(AppTheme.danger());
            } else {
                mouseExited(e);
            }
        }
    }

    private JButton createSortButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.addActionListener(listener);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setContentAreaFilled(true);

        btn.setPreferredSize(new Dimension(120, 30));

        btn.putClientProperty("JButton.buttonType", "square");
        btn.putClientProperty("JButton.arc", 0);
        btn.setFocusPainted(false);

        return btn;
    }

    public void loadFavorites() {
        favoriteModel.clear();
        List<Song> favorites = favoritesService.getAllFavorites();
        favorites.forEach(favoriteModel::addElement);
        if (!favoriteModel.isEmpty()) favoriteList.setSelectedIndex(0);
    }

    private void removeFavorite() {
        Song selected = favoriteList.getSelectedValue();
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Remove '" + selected.title() + "' from favorites?", "Confirm Removal", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                favoritesService.removeFavorite(selected.id());
                loadFavorites();
            }
        }
    }

    public Song getSelectedFavorite() {
        return favoriteList.getSelectedValue();
    }

    private void sortSongsAlphabetically() {
        updateModel(favoritesService.getFavoritesSortedAlphabetically());
    }

    private void sortSongsByArtist() {
        updateModel(favoritesService.getFavoritesSortedByArtist());
    }

    private void updateModel(List<Song> sortedSongs) {
        favoriteModel.clear();
        sortedSongs.forEach(favoriteModel::addElement);
        if (!favoriteModel.isEmpty()) {
            favoriteList.setSelectedIndex(0);
        }
    }

    private static class MatteBorder extends javax.swing.border.MatteBorder {
        public MatteBorder(int top, int left, int bottom, int right, Color matteColor) {
            super(top, left, bottom, right, matteColor);
        }
    }
}