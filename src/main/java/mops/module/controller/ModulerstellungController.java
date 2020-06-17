package mops.module.controller;

import static mops.module.keycloak.KeycloakMopsAccount.createAccountFromPrincipal;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import mops.module.database.Antrag;
import mops.module.database.Modul;
import mops.module.database.Modulkategorie;
import mops.module.services.AntragService;
import mops.module.services.ModulService;
import mops.module.services.ModulWrapperService;
import mops.module.wrapper.ModulWrapper;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.ModelAndView;

@Validated
@Controller
@SessionScope
@RequiredArgsConstructor
@RequestMapping("/module")
public class ModulerstellungController {

    private final ModulService modulService;
    private final AntragService antragService;


    /**
     * Fügt alle benötigten Attribute zum Model hinzu.
     *
     * @param model Modell für die HTML-Datei.
     * @param token Der Token von keycloak für die Berechtigung.
     */
    @ModelAttribute
    @RolesAllowed({"ROLE_orga", "ROLE_sekretariat"})
    public void addAttributes(Model model, KeycloakAuthenticationToken token) {
        model.addAttribute("formatter", DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        model.addAttribute("account", createAccountFromPrincipal(token));
        model.addAttribute("allCategories", Modulkategorie.values());
        model.addAttribute("allModules", modulService.getAllModule());

        List<Modul> allSichtbareModule = modulService.getAllSichtbareModule();
        model.addAttribute("allVisibleModules", allSichtbareModule);

        ArrayList<LinkedList<Modul>> allVersions =
                antragService.getAllVersionsListFor(allSichtbareModule);
        model.addAttribute("allVersions", allVersions);
        ArrayList<LinkedList<Antrag>> allAntraege =
                antragService.getAllAntraegeListFor(allSichtbareModule);
        model.addAttribute("allAntraege", allAntraege);

        List<String> semesterWahl = ModulService.getPastAndNextSemestersForTagging();
        model.addAttribute("allSemesters", semesterWahl);
    }


    /**
     * Get-Mapping für das Generieren eines Modulerstellungsformulars für die eingegebene Anzahl
     * von Veranstaltungen.
     *
     * @param veranstaltungsanzahl Anzahl der Veranstaltungen.
     * @param model                Modell für die HTML-Datei.
     * @return View für die Modulerstellung.
     */
    @GetMapping("/modulerstellung")
    @RolesAllowed({"ROLE_orga", "ROLE_sekretariat"})
    public String modulCreationAntragForm(
            @RequestParam(name = "veranstaltungsanzahl")
            @Min(value = 1, message = "Veranstaltungszahl muss mindestens 1 sein!")
                    int veranstaltungsanzahl,
            Model model) {

        ModulWrapper modulWrapper =
                ModulWrapperService.initializeEmptyWrapper(veranstaltungsanzahl);
        model.addAttribute("modulWrapper", modulWrapper);

        return "modulerstellung";
    }

    /**
     * Mapping für das Generieren eines Modulbearbeitungsformulars für die eingegebene Modul-Id.
     *
     * @param id    id des zu bearbeitenden Moduls.
     * @param model Modell für die HTML-Datei.
     * @return View für die Modulbearbeitung.
     */
    @GetMapping("/modulbearbeitung/{id}")
    @RolesAllowed({"ROLE_orga", "ROLE_sekretariat"})
    public String modulModificationAntragForm(
            @PathVariable String id,
            Model model) {
        Modul modul = modulService.getModulById(Long.parseLong(id));
        ModulWrapper modulWrapper = ModulWrapperService.initializePrefilledWrapper(modul);
        model.addAttribute("modulWrapper", modulWrapper);

        return "modulerstellung";
    }

    /**
     * Post-Mapping für das Anzeigen einer Vorschau für die eingegebenen Daten bei der der
     * Erstellung eines Moduls.
     *
     * @param modulWrapper Wrapper für ein Modul und seine Unter-Objekte
     * @param model        Model für die HTML-Datei.
     * @return View für die Modulvorschau.
     */
    @PostMapping("/modulerstellung_preview")
    @RolesAllowed({"ROLE_orga", "ROLE_sekretariat"})
    public String modulCreationAntragPreview(@Valid ModulWrapper modulWrapper,
                                             Model model) {

        Modul modul = ModulWrapperService.readModulFromWrapper(modulWrapper);
        model.addAttribute("modul", modul);
        model.addAttribute("modulWrapper", modulWrapper);
        return "modulpreview";

    }

    /**
     * Post-Mapping für das Abschicken der eingegebenen Daten und Erstellung eines entsprechenden
     * Antrags bei der der Erstellung eines Moduls.
     *
     * @param modulWrapper Wrapper für ein Modul und seine Unter-Objekte
     * @param model        Model für die HTML-Datei.
     * @param token        Keycloak-Token.
     * @return Zurückleitung auf den "Module bearbeiten"-Reiter.
     */
    @PostMapping("/modulerstellung_confirmation")
    @RolesAllowed({"ROLE_orga", "ROLE_sekretariat"})
    public String modulCreationAntragConfirm(ModulWrapper modulWrapper,
                                             Model model,
                                             KeycloakAuthenticationToken token) {

        String antragsteller = ((KeycloakPrincipal) token.getPrincipal()).getName();
        model.addAttribute("account", createAccountFromPrincipal(token));
        Modul modul = ModulWrapperService.readModulFromWrapper(modulWrapper);

        Antrag antrag = antragService.addModulCreationAntrag(modul, antragsteller);
        if (token.getAccount().getRoles().contains("sekretariat")) {
            antragService.approveModulCreationAntrag(antrag);
        }

        return "modulbeauftragter";
    }

    /**
     * Post-Mapping für das Zurückkehren aus der Vorschau zum Formular mit den eingegebenen Daten
     * bei der der Erstellung eines Moduls.
     *
     * @param modulWrapper Wrapper für ein Modul und seine Unter-Objekte
     * @param model        Model für die HTML-Datei.
     * @return View für die Modulerstellung.
     */
    @PostMapping("/modulerstellung_back_to_edit")
    @RolesAllowed({"ROLE_orga", "ROLE_sekretariat"})
    public String modulCreationAntragBackToEdit(ModulWrapper modulWrapper,
                                                Model model) {

        Modul modul = ModulWrapperService.readModulFromWrapper(modulWrapper);

        ModulWrapper refilledModulWrapper =
                ModulWrapperService.initializePrefilledWrapper(modul);

        model.addAttribute("modulWrapper", refilledModulWrapper);

        return "modulerstellung";
    }

    /**
     * Post-Mapping für das Anzeigen einer Vorschau für die eingegebenen Daten bei der der
     * Bearbeitung eines Moduls.
     *
     * @param modulWrapper Wrapper für ein Modul und seine Unter-Objekte
     * @param model        Model für die HTML-Datei.
     * @return View für die Modulvorschau.
     */
    @PostMapping("/modulbearbeitung_preview")
    @RolesAllowed({"ROLE_orga", "ROLE_sekretariat"})
    public String modulModificationAntragPreview(
            ModulWrapper modulWrapper,
            Model model) {

        Modul modul = ModulWrapperService.readModulFromWrapper(modulWrapper);
        model.addAttribute("modul", modul);
        model.addAttribute("modulWrapper", modulWrapper);
        return "modulpreview";
    }

    /**
     * Post-Mapping für das Abschicken der eingegebenen Daten und Erstellung eines entsprechenden
     * Antrags bei der der Bearbeitung eines Moduls.
     *
     * @param modulWrapper Wrapper für ein Modul und seine Unter-Objekte
     * @param token        Keycloak-Token.
     * @return Zurückleitung auf den "Module bearbeiten"-Reiter.
     */
    @PostMapping("/modulbearbeitung_confirmation")
    @RolesAllowed({"ROLE_orga", "ROLE_sekretariat"})
    public String modulModificationAntragConfirmation(
            ModulWrapper modulWrapper,
            KeycloakAuthenticationToken token) {

        String antragsteller = ((KeycloakPrincipal) token.getPrincipal()).getName();

        Modul neuesModul = ModulWrapperService.readModulFromWrapper(modulWrapper);
        Long modulIdLong = modulWrapper.getModul().getId();
        Modul altesModul = modulService.getModulById(modulIdLong);

        neuesModul.refreshMapping();
        Modul diffModul = ModulService.calculateModulDiffs(altesModul, neuesModul);

        if (diffModul != null) {
            Antrag antrag = antragService.addModulModificationAntrag(neuesModul, antragsteller);
            if (token.getAccount().getRoles().contains("sekretariat")) {
                antragService.approveModulModificationAntrag(antrag);
                return "modulbeauftragter";
            }
        }

        return "modulbeauftragter";
    }

    /**
     * Post-Mapping für das Zurückkehren aus der Vorschau zum Formular mit den eingegebenen Daten
     * bei der der Bearbeitung eines Moduls.
     *
     * @param modulWrapper Wrapper für ein Modul und seine Unter-Objekte
     * @param model        Model für die HTML-Datei.
     * @return View für die Modulbearbeitung.
     */
    @PostMapping("/modulbearbeitung_back_to_edit")
    @RolesAllowed({"ROLE_orga", "ROLE_sekretariat"})
    public String modulModificationAntragBackToEdit(ModulWrapper modulWrapper,
                                                    Model model) {

        Modul modul = ModulWrapperService.readModulFromWrapper(modulWrapper);

        ModulWrapper refilledModulWrapper =
                ModulWrapperService.initializePrefilledWrapper(modul);

        model.addAttribute("modulWrapper", refilledModulWrapper);
        return "modulerstellung";
    }

    /**
     * ExceptionHandler, der bei falscher Eingabe (Veranstaltungsanzahl) Fehlerseite zurückgibt.
     *
     * @param exeption Die Fehlermeldung, die durch die fehlerhafte Eingabe erzeugt wird
     * @return View error
     */
    @ExceptionHandler(ConstraintViolationException.class)
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

    /**
     * ExceptionHandler, der bei falscher Eingabe bei Modulerstellung Fehlerseite zurückgibt.
     *
     * @param bindException Die Fehlermeldung, die durch die fehlerhafte Eingabe erzeugt wird
     * @return View error
     */
    @ExceptionHandler(BindException.class)
    public ModelAndView handleBindException(BindException bindException) {
        ModelAndView modelAndView = new ModelAndView();
        List<String> exceptions =
                bindException.getAllErrors()
                        .parallelStream()
                        .map(x -> x.getDefaultMessage())
                        .collect(Collectors.toList());
        modelAndView.addObject("exceptions", exceptions);
        modelAndView.setViewName("error");
        return modelAndView;
    }
}
