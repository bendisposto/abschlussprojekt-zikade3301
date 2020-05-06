package mops.module.services;

import lombok.RequiredArgsConstructor;
import mops.module.database.Veranstaltung;
import mops.module.repositories.VeranstaltungsRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VeranstaltungService {
    private final VeranstaltungsRepository veranstaltungsRepository;

    /**
     * Gibt eine bestimmte Veranstaltung zurück.
     *
     * @param id Id der Veranstaltung, die zurückgegeben werden soll
     */
    public Veranstaltung getVeranstaltungById(Long id) {
        return veranstaltungsRepository.getVeranstaltungById(id);
    }
}
