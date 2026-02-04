package de.st197974.songmanager.ui.panels;

import de.st197974.songmanager.model.Song;
import de.st197974.songmanager.repository.SongRepository;
import de.st197974.songmanager.service.FavoritesService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FavoritesPanel extends JPanel {

    private final FavoritesService favoritesService;
    private final DefaultListModel<Song> favoriteModel = new DefaultListModel<>();
    private JList<Song> favoriteList;

    public FavoritesPanel(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;

        setLayout(new BorderLayout());
        buildUI();
        loadFavorites();
    }

    private void buildUI() {
        favoriteList = new JList<>(favoriteModel);
        favoriteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(favoriteList);
        add(scrollPane, BorderLayout.CENTER);

        JButton removeBtn = new JButton("Remove Favorite");
        removeBtn.addActionListener(e -> removeFavorite());

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        btnPanel.add(removeBtn);

        add(btnPanel, BorderLayout.SOUTH);
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
            favoritesService.removeFavorite(selected.id());
            loadFavorites();
        }
    }

    public Song getSelectedFavorite() {
        return favoriteList.getSelectedValue();
    }

}
