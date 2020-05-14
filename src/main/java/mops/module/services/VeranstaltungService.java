package mops.module.services;

import java.util.List;
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

    /**
     * Gibt alle Veranstaltung zurück,
     * die in einem bestimmten Semester stattfinden.
     *
     * @param semesterTag Semester, in dem Veranstaltungen stattfinden
     */
    public List<Veranstaltung> getVeranstaltungenBySemester(String semesterTag) {
        return veranstaltungsRepository.getVeranstaltungenBySemesterOrderByTitel(semesterTag);
    }
}
