package mops.module.services;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jdk.internal.module.ModuleReferenceImpl;
import lombok.RequiredArgsConstructor;
import mops.module.database.Antrag;
import mops.module.database.Modul;
import mops.module.database.Veranstaltung;
import mops.module.repositories.AntragRepository;
import mops.module.repositories.ModulSnapshotRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModulService {

    private final AntragRepository antragRepository;
    private final ModulSnapshotRepository modulSnapshotRepository;


    /**
     * Berechnet die Differenzen zwischen zwei Modulen.
     *
     * @param oldModul Altes Modul
     * @param newModul Neues Modul
     * @return Ein Änderungsmodul, bei dem ein Feld ohne Änderung den Wert null hat bedeutet und
     *         ein Feld mit einem Wert den neuen Wert nach der Änderung enthält.
     */
    public static Modul calculateModulDiffs(Modul oldModul, Modul newModul) {
        Modul changes = new Modul();
        boolean foundDiffs = false;

        if (oldModul == null || newModul == null) {
            throw new IllegalArgumentException("Mindestens ein Modul ist null!");
        }

        changes.setId(oldModul.getId());

        for (Field field : newModul.getClass().getDeclaredFields()) {
            if (checkSingleField(oldModul, newModul, changes, field)) {
                foundDiffs = true;
            }
        }
        if (!foundDiffs) {
            return null;
        }
        return changes;
    }

    private static boolean checkSingleField(Modul altesmodul, Modul neuesmodul,
                                            Modul changes, Field field) {
        field.setAccessible(true);

        if ("datumAenderung".equals(field.getName())) {
            return false;
        }

        try {
            if (field.get(altesmodul) == null) {
                if (field.get(neuesmodul) != null) {
                    field.set(changes, field.get(neuesmodul));
                    return true;
                }
            } else if (!field.get(altesmodul).equals(field.get(neuesmodul))) {
                field.set(changes, field.get(neuesmodul));
                return true;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }


    static void applyAntragOnModul(Modul modul, Antrag antrag) {
        Modul modulChanges = JsonService.jsonObjectToModul(antrag.getJsonModulAenderung());

        for (Field field : modul.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            try {
                if (field.get(modulChanges) == null) {
                    continue;
                }
                if (!field.get(modulChanges).equals(field.get(modul))) {
                    field.set(modul, field.get(modulChanges));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Modul> getAllModule() {
        return StreamSupport.stream(modulSnapshotRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public List<Modul> getAllSichtbareModule() {
        return getAllModule().stream().filter(Modul::getSichtbar).collect(Collectors.toList());
    }

    public List<Modul> getModuleBySemester(String semester) {
        return modulSnapshotRepository.findModuleBySemester(semester);
    }

    public Modul getModulById(Long id) {
        return modulSnapshotRepository.findById(id).orElse(null);
    }

    public void tagSemesterForVeranstaltung(String semesterTag, Long veranstaltungId, Long modulId) {

        Veranstaltung veranstaltung = getModulById(modulId).getVeranstaltungen().stream().filter(v -> v.getId().equals(veranstaltungId)).findFirst().orElse(null);
        Set<String> semesterOld = veranstaltung.getSemester();
        semesterOld.add(semesterTag);

        modulSnapshotRepository.save(getModulById(modulId));
    }
}
