package mops.module.controller;

import static mops.module.keycloak.KeycloakMopsAccount.createAccountFromPrincipal;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import mops.module.database.Modul;
import mops.module.database.Veranstaltung;
import mops.module.services.ModulService;
import mops.module.services.VeranstaltungService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping("/module")
public class SemesterTagController {

    private final ModulService modulService;
    private final VeranstaltungService veranstaltungService;

    /**
     * Controller, der das Request für die Erstellung eines SemesterTags entgegennimmt.
     *
     * @param veranstaltungsIds Liste der Veranstaltungen, die ein Tag erhalten sollen
     * @param semester          Der SemesterTag, der den Veranstaltungen hinzugefügt werden soll
     * @param model             Model für die HTML-Datei.
     * @param token             Der Token von keycloak für die Berechtigung.
     * @return View Modulbeauftragter
     */
    @PostMapping("/semesterTag/create")
    @Secured("ROLE_sekretariat")
    public String addSemesterTagToVeranstaltung(
            @RequestParam(value = "semester", defaultValue = "")
            @NotEmpty(message = "Geben Sie ein gültiges Semester an!")
                    String semester,
            @RequestParam(value = "veranstaltungsIds", defaultValue = "")
            @NotEmpty(message = "Wählen Sie mindestens eine Veranstaltung aus!")
                    List<String> veranstaltungsIds,
            Model model, KeycloakAuthenticationToken token) {

        model.addAttribute("account", createAccountFromPrincipal(token));

        for (String veranstaltungsId : veranstaltungsIds) {
            Veranstaltung veranstaltung =
                    veranstaltungService.getVeranstaltungById(Long.parseLong(veranstaltungsId));
            Long modulId = veranstaltung.getModul().getId();
            modulService.tagVeranstaltungSemester(
                    semester,
                    Long.parseLong(veranstaltungsId),
                    modulId);
        }
        return "redirect:/module/modulbeauftragter";
    }

    /**
     * Controller, der den Request für das Löschen eines SemesterTags entgegennimmt.
     *
     * @param tagToDelete              Der SemesterTag, der gelöscht werden soll
     * @param idVeranstaltungTagDelete ID der Veranstaltung, die das Tag beinhaltet
     * @param idModulTagDelete         ID des Moduls, das die Veranstaltung beinhaltet
     * @param model                    Model für die HTML-Datei.
     * @param token                    Der Token von keycloak für die Berechtigung.
     * @return View Modulbeauftragter
     */
    @PostMapping("/semesterTag/delete")
    @Secured("ROLE_sekretariat")
    public String removeSemesterTagToVeranstaltung(
            @RequestParam(name = "tagToDelete", required = true) String tagToDelete,
            @RequestParam(name = "idVeranstaltungTagDelete") String idVeranstaltungTagDelete,
            @RequestParam(name = "idModulTagDelete") String idModulTagDelete,
            Model model,
            KeycloakAuthenticationToken token) {

        model.addAttribute("account", createAccountFromPrincipal(token));
        modulService.deleteTagVeranstaltungSemester(
                tagToDelete,
                Long.parseLong(idVeranstaltungTagDelete),
                Long.parseLong(idModulTagDelete)
        );
        return "redirect:/module/modulbeauftragter";
    }

    /**
     * Mapping für das Generieren eines Modals zum löschen einer Semesterplanung.
     *
     * @param semesterTag Die Semesterplanung, die angezeigt werden soll.
     * @param model       Model für die HTML-Datei.
     * @param token       Der Token von keycloak für die Berechtigung.
     * @return Modal deletesemestertags
     */
    @GetMapping("/deletesemester")
    @Secured("ROLE_sekretariat")
    public String getDeleteSemesterplanung(
            @RequestParam(name = "semesterTag") String semesterTag,
            Model model,
            KeycloakAuthenticationToken token) {
        model.addAttribute("semester", semesterTag);
        model.addAttribute("veranstaltungen",
                veranstaltungService.getVeranstaltungenBySemester(semesterTag));
        model.addAttribute("account", createAccountFromPrincipal(token));

        List<String> semesterWahl = ModulService.getPastAndNextSemestersForTagging();
        model.addAttribute("allSemesters", semesterWahl);
        return "/deletesemestertags";
    }

    /**
     * Controller, der den Request für das Löschen einer Semesterplanung entgegennimmt.
     *
     * @param semesterTag Das Semester, dessen Planung gelöscht werden soll
     * @param model       Model für die HTML-Datei.
     * @param token       Der Token von keycloak für die Berechtigung.
     * @return View Modulbeauftragter
     */
    @PostMapping("/deletesemester")
    @Secured("ROLE_sekretariat")
    public String dropSemesterplanung(
            @RequestParam(name = "semesterTag") String semesterTag,
            Model model,
            KeycloakAuthenticationToken token) {

        List<Veranstaltung> veranstaltung =
                veranstaltungService.getVeranstaltungenBySemester(semesterTag);

        for (Veranstaltung v : veranstaltung) {
            Modul modul = v.getModul();
            modulService.deleteTagVeranstaltungSemester(
                    semesterTag,
                    Long.parseLong(v.getId().toString()),
                    Long.parseLong(modul.getId().toString()));
        }

        return "redirect:/module/modulbeauftragter";
    }

    /**
     * ExceptionHandler, der bei falscher Eingabe Fehlerseite zurückgibt.
     *
     * @param exeption Die Fehlermeldung, die durch die fehlerhafte Eingabe erzeugt wird
     * @return View error
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleError(ConstraintViolationException exeption) {
        ModelAndView modelAndView = new ModelAndView();
        List<String> exceptions =
                exeption.getConstraintViolations()
                        .parallelStream()
                        .map(x -> x.getMessageTemplate())
                        .collect(Collectors.toList());

        modelAndView.addObject("exceptions", exceptions);
        modelAndView.setViewName("error");
        return modelAndView;
    }
}
