package mops.module.controller;

import static mops.module.keycloak.KeycloakMopsAccount.createAccountFromPrincipal;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mops.module.database.Modul;
import mops.module.database.Veranstaltung;
import mops.module.services.ModulService;
import mops.module.services.VeranstaltungService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
            @RequestParam("veranstaltungsIds") List<String> veranstaltungsIds,
            @RequestParam("semester") String semester,
            Model model,
            KeycloakAuthenticationToken token) {

        model.addAttribute("account", createAccountFromPrincipal(token));
        //Ersten Eintrag ignorieren
        veranstaltungsIds.remove("-1");
        if (veranstaltungsIds != null) {
            for (String veranstaltungsId : veranstaltungsIds) {
                Veranstaltung veranstaltung =
                        veranstaltungService.getVeranstaltungById(Long.parseLong(veranstaltungsId));
                Long modulId = veranstaltung.getModul().getId();
                modulService.tagVeranstaltungSemester(
                        semester,
                        Long.parseLong(veranstaltungsId),
                        modulId);
            }
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
     * @param semesterTag              Die Semesterplanung, die angezeigt werden soll.
     * @param model                    Model für die HTML-Datei.
     * @param token                    Der Token von keycloak für die Berechtigung.
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
        return "/deletesemestertags";
    }

    /**
     * Controller, der den Request für das Löschen einer Semesterplanung entgegennimmt.
     *
     * @param semesterTag              Das Semester, dessen Planung gelöscht werden soll
     * @param model                    Model für die HTML-Datei.
     * @param token                    Der Token von keycloak für die Berechtigung.
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
}
