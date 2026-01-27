package de.st197974.swiftmanager.service;

import de.st197974.swiftmanager.model.Song;
import de.st197974.swiftmanager.repository.InMemorySongRepository;
import de.st197974.swiftmanager.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DiscographyServiceTest {

    private SongRepository repository;
    private DiscographyService service;

    @BeforeEach
    void setUp() {
        repository = new InMemorySongRepository();
        service = new DiscographyService(repository);
    }


}
