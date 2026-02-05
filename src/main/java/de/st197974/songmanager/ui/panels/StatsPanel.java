package de.st197974.songmanager.ui.panels;

import de.st197974.songmanager.service.StatsService;
import de.st197974.songmanager.ui.AppTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class StatsPanel extends JPanel {

    private final StatsService service;

    private JPanel cardsPanel;
    private JPanel artistListPanel;
    private JScrollPane scrollPane;
    private JLabel listHeader;

    public StatsPanel(StatsService service) {
        this.service = service;

        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        buildUI();

        updateThemeColors();
    }

    @Override
    public void updateUI() {
        super.updateUI();

        if (cardsPanel != null) {
            updateThemeColors();
        }
    }

    private void updateThemeColors() {
        Color bg = AppTheme.isDark() ? UIManager.getColor("Panel.background") : new Color(245, 245, 247);
        setBackground(bg);

        if (listHeader != null) {
            listHeader.setForeground(AppTheme.isDark() ? Color.WHITE : Color.BLACK);
        }

        if (scrollPane != null) {
            Color borderColor = AppTheme.isDark() ? new Color(60, 60, 60) : new Color(230, 230, 230);
            scrollPane.setBorder(BorderFactory.createLineBorder(borderColor));
            scrollPane.getViewport().setBackground(AppTheme.isDark() ? UIManager.getColor("Panel.background") : Color.WHITE);
        }

        loadStatistics();
    }

    private void buildUI() {
        cardsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        cardsPanel.setOpaque(false);
        add(cardsPanel, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setOpaque(false);

        listHeader = new JLabel("Songs per Artist");
        listHeader.setFont(new Font("SansSerif", Font.BOLD, 18));
        listHeader.setBorder(new EmptyBorder(0, 0, 10, 0));
        centerContainer.add(listHeader, BorderLayout.NORTH);

        artistListPanel = new JPanel();
        artistListPanel.setLayout(new BoxLayout(artistListPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(artistListPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        centerContainer.add(scrollPane, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);
    }

    public void loadStatistics() {
        if (cardsPanel == null || artistListPanel == null) return;

        cardsPanel.removeAll();
        artistListPanel.removeAll();

        Color contentBg = AppTheme.isDark() ? UIManager.getColor("Panel.background") : Color.WHITE;
        artistListPanel.setBackground(contentBg);

        int total = service.getTotalSongs();
        String duration = formatTime(service.getTotalDuration());
        String avg = formatTime(service.getAverageDuration());

        cardsPanel.add(createStatCard("Total Songs", String.valueOf(total)));
        cardsPanel.add(createStatCard("Total Duration", duration));
        cardsPanel.add(createStatCard("Ø Duration", avg));

        int totalFavorites = service.getTotalFavorites();
        int favoritePercentage = total > 0 ? (int) ((totalFavorites * 100) / total) : 0;
        cardsPanel.add(createStatCard("Favorites", favoritePercentage + "% (" + totalFavorites + ")"));

        Map<String, Long> songsPerArtist = service.getSongsPerArtist();
        songsPerArtist.forEach((artist, count) -> artistListPanel.add(createArtistRow(artist, count, total)));

        revalidate();
        repaint();
    }

    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());

        Color cardBg = AppTheme.isDark() ? UIManager.getColor("Table.background") : Color.WHITE;
        Color borderColor = AppTheme.isDark() ? new Color(60, 60, 60) : new Color(220, 220, 220);

        card.setBackground(cardBg);
        card.setBorder(new CompoundBorder(BorderFactory.createLineBorder(borderColor, 1), new EmptyBorder(15, 15, 15, 15)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(AppTheme.isDark() ? Color.LIGHT_GRAY : Color.GRAY);
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblValue.setForeground(AppTheme.accent());

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createArtistRow(String name, Long count, int total) {
        JPanel row = new JPanel(new BorderLayout());

        Color rowBg = AppTheme.isDark() ? UIManager.getColor("Panel.background") : Color.WHITE;
        Color separatorColor = AppTheme.isDark() ? new Color(60, 60, 60) : new Color(240, 240, 240);

        row.setBackground(rowBg);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, separatorColor), new EmptyBorder(10, 15, 10, 15)));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nameLabel.setForeground(AppTheme.isDark() ? Color.WHITE : Color.BLACK);

        JLabel countLabel = new JLabel(count + " Songs");
        countLabel.setForeground(AppTheme.isDark() ? Color.LIGHT_GRAY : Color.DARK_GRAY);

        int percentage = total > 0 ? (int) ((count * 100) / total) : 0;
        JLabel percentLabel = new JLabel(percentage + "%");
        percentLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        percentLabel.setForeground(Color.GRAY);

        row.add(nameLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(percentLabel);
        rightPanel.add(countLabel);

        row.add(rightPanel, BorderLayout.EAST);

        return row;
    }

    private String formatTime(int totalSeconds) {
        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (days > 0 || hours > 0) sb.append(hours).append(":");
        sb.append(String.format("%02d:%02d", minutes, seconds));

        return sb.toString();
    }
}