package de.st197974.songmanager.ui.panels;

import com.formdev.flatlaf.FlatLightLaf;
import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.service.FavoritesService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class FavoritesPanel extends JPanel {

    private final FavoritesService favoritesService;
    private final DefaultListModel<Song> favoriteModel = new DefaultListModel<>();
    private JList<Song> favoriteList;

    private final Color DANGER_COLOR = new Color(255, 107, 107);

    public FavoritesPanel(FavoritesService favoritesService) {

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        this.favoritesService = favoritesService;

        Color BG_COLOR = new Color(245, 245, 247);
        setBackground(BG_COLOR);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        buildUI();
        loadFavorites();
    }

    private void buildUI() {
        JPanel headerPanel = getJPanel();

        favoriteList = new JList<>(favoriteModel);
        favoriteList.setFixedCellHeight(40);
        favoriteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        favoriteList.setBackground(Color.WHITE);
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

        JButton removeBtn = new JButton("Remove from Favorites");
        styleRemoveButton(removeBtn);
        removeBtn.addActionListener(_ -> removeFavorite());

        actionPanel.add(leftActions, BorderLayout.WEST);
        actionPanel.add(removeBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private static JPanel getJPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Your Favorite Songs");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(50, 50, 50));

        JLabel subtitleLabel = new JLabel("Easy overview over all your favorites!");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);

        JPanel textWrapper = new JPanel(new GridLayout(2, 1));
        textWrapper.setOpaque(false);
        textWrapper.add(titleLabel);
        textWrapper.add(subtitleLabel);

        headerPanel.add(textWrapper, BorderLayout.WEST);
        return headerPanel;
    }

    private DefaultListCellRenderer createFavoriteRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel itemPanel = new JPanel(new BorderLayout(15, 0));
                itemPanel.setBackground(isSelected ? new Color(199, 221, 253) : Color.WHITE);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                        BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));

                if (value instanceof Song song) {
                    JLabel titleLabel = new JLabel("<html><span style='color: #FFAE00;'>★</span> <b>" + song.title() + "</b></html>");
                    titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

                    JLabel artistLabel = new JLabel(song.artist());
                    artistLabel.setForeground(Color.GRAY);
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

        btn.setBackground(Color.WHITE);
        btn.setForeground(DANGER_COLOR);
        btn.setBorder(BorderFactory.createLineBorder(DANGER_COLOR, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(DANGER_COLOR);
                btn.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
                btn.setForeground(DANGER_COLOR);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBackground(DANGER_COLOR.darker());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (btn.getBounds().contains(e.getPoint())) {
                    btn.setBackground(DANGER_COLOR);
                } else {
                    btn.setBackground(Color.WHITE);
                }
            }
        });
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
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Remove '" + selected.title() + "' from favorites?",
                    "Confirm Removal", JOptionPane.YES_NO_OPTION);

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
}