package mops.module.controller;


import static mops.module.keycloak.KeycloakMopsAccount.createAccountFromPrincipal;

import java.lang.reflect.Field;
import lombok.RequiredArgsConstructor;
import mops.module.database.Antrag;
import mops.module.database.Modul;
import mops.module.services.AntragService;
import mops.module.services.JsonService;
import mops.module.services.ModulService;
import mops.module.services.ModulWrapperService;
import mops.module.wrapper.ModulWrapper;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.annotation.SessionScope;

@Controller
@SessionScope
@RequiredArgsConstructor
@RequestMapping("/module")
public class AntragdetailsController {

    private final AntragService antragService;
    private final ModulService modulService;

    /** Antragdetails Mapping.
     *
     * @param id Id des Antrags.
     * @param token Der Token von keycloak für die Berechtigung.
     * @param model Modell für die HTML Datei.
     * @return View antragdetails.
     */

    @RequestMapping(value = "/antragdetails/{id}", method = RequestMethod.GET)
    @Secured("ROLE_sekretariat")
    public String antragdetails(
            @PathVariable long id,
            KeycloakAuthenticationToken token,
            Model model) {

        model.addAttribute("account", createAccountFromPrincipal(token));
        model.addAttribute("antragId", id);

        Antrag antrag =  antragService.getAntragById(id);
        Long modulIdDesAntrages = antrag.getModulId();

        //Der Fall Creation
        if (modulIdDesAntrages == null) {
            Modul modulAusAntrag = JsonService.jsonObjectToModul(
                    antrag.getJsonModulAenderung());

            ModulWrapper modulAntragWrapper = ModulWrapperService.initializePrefilledWrapper(modulAusAntrag);

            model.addAttribute("altesModul", modulAntragWrapper);
            //Damit man die selbe html ohne unterschiede verwenden kann.
            model.addAttribute("antrag", modulAntragWrapper);

            return "antragdetails";
        }
        //Der Fall Modifikation
        Modul modulAlt = modulService.getModulById(modulIdDesAntrages);
        Modul modulNeu = new Modul();
        //Versuch ModulAlt zu kopieren
        for (Field field : modulAlt.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            try {
                field.set(modulNeu, field.get(modulAlt));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        ModulService.applyAntragOnModul(modulNeu,antrag);

        ModulWrapper modulAltWrapper = ModulWrapperService.initializePrefilledWrapper(modulAlt);
        ModulWrapper modulNeuWrapper = ModulWrapperService.initializePrefilledWrapper(modulNeu);

        model.addAttribute("altesModul", modulAltWrapper);
        model.addAttribute("antrag", modulNeuWrapper);

        return "antragdetails";
    }

    /** Antrag annehmen.
     *
     * @param id Id des angenommenen Antrages.
     * @param antragAngenommen Antrag der von Der Rolle Sekretariat angenommen wurde.
     * @param token Der Token von keycloak für die Berechtigung.
     * @param model Modell für die HTML Datei.
     * @return Redirect zur Antragsübersicht.
     */

    @PostMapping("/antragdetails/{id}")
    @Secured("ROLE_sekretariat")
    public String antragAnnehmen(
            @PathVariable long id,
            ModulWrapper antragAngenommen,
            Model model,
            KeycloakAuthenticationToken token) {

        Modul modul = ModulWrapperService.readModulFromWrapper(antragAngenommen);

        if (modul.getId() == null) {
            Antrag antrag = antragService.getAntragById(id);
            antrag.setJsonModulAenderung(JsonService.modulToJsonObject(modul));
            antragService.approveModulCreationAntrag(antrag);
        } else {
            Antrag antrag = antragService.getAntragById(id);

            antrag.setJsonModulAenderung(JsonService.modulToJsonObject(modul));

            antragService.approveModulModificationAntrag(antrag);
        }

        model.addAttribute("account",createAccountFromPrincipal(token));
        return "redirect:/module/administrator";
    }

}
