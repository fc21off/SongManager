package de.st197974.songmanager.service;

import de.st197974.songmanager.repository.InMemorySongRepository;
import de.st197974.songmanager.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;

public class DiscographyServiceTest {

    private SongRepository repository;
    private DiscographyService service;

    @BeforeEach
    void setUp() {
        repository = new InMemorySongRepository();
        service = new DiscographyService(repository);
    }


}
