package mops.module.repositories;

import java.util.List;
import mops.module.database.Veranstaltung;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VeranstaltungsRepository extends CrudRepository<Veranstaltung, Long> {
    Veranstaltung getVeranstaltungById(Long id);

    List<Veranstaltung> getVeranstaltungenBySemesterOrderByTitel(String semesterTag);
}
