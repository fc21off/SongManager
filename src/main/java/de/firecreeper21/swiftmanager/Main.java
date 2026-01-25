package de.firecreeper21.swiftmanager;

import de.firecreeper21.swiftmanager.model.Song;
import de.firecreeper21.swiftmanager.repository.InMemorySongRepository;
import de.firecreeper21.swiftmanager.repository.SongRepository;

import java.util.List;

public class Main {

    static void main() {

        SongRepository repo = new InMemorySongRepository();

        repo.save(new Song("Style", "1989", 231));
        repo.save(new Song("Wildest Dreams", "1989", 220));
        repo.save(new Song("The Fate of Ophelia", "The Life of a Showgirl",226));

        IO.println("Amount of Songs in Repository: " + repo.findAll().size());

        for(Song s : repo.findAll()) {
            IO.println("Song found: " + s);
        }

    }

}
