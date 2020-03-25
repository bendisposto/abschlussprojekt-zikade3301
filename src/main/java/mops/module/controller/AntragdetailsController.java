package mops.module.controller;


import static mops.module.keycloak.KeycloakMopsAccount.createAccountFromPrincipal;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import mops.module.controller.ModulWrapper;
import mops.module.database.Antrag;
import mops.module.database.Modul;
import mops.module.database.Veranstaltung;
import mops.module.database.Veranstaltungsform;
import mops.module.database.Zusatzfeld;
import mops.module.services.AntragService;
import mops.module.services.JsonService;
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

    /** Antragdetails Mapping.
     *
     * @param id Id des Antrags.
     * @param token Der Token von keycloak für die Berechtigung.
     * @param model Modell für die HTML Datei.
     * @return HTML antragdetails.
     */

    @SuppressWarnings("uncheccked")  //TODO - Begründen
    @RequestMapping(value = "/antragdetails/{id}", method = RequestMethod.GET)
    @Secured("ROLE_sekretariat")
    public String antragdetails(
            @PathVariable String id,
            KeycloakAuthenticationToken token,
            Model model) {

        Modul modul = JsonService.jsonObjectToModul(
                antragService.getAntragById(Long.parseLong(id)).getJsonModulAenderung());

        ModulWrapper antrag = new ModulWrapper(modul, null, null, null);
        antrag.initPrefilled(6, 2);

        model.addAttribute("antragId", id);
        model.addAttribute("account",createAccountFromPrincipal(token));
        model.addAttribute("antrag", antrag);

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
            @PathVariable String id,
            ModulWrapper antragAngenommen,
            Model model,
            KeycloakAuthenticationToken token) {

        //Auspacken des Wrappers
        for (int i = 0; i < antragAngenommen.getVeranstaltungen().size(); i++) {
            antragAngenommen.getVeranstaltungen().get(i).setVeranstaltungsformen(
                    new HashSet<>(antragAngenommen.getVeranstaltungsformen()[i]));
            antragAngenommen.getVeranstaltungen().get(i).setZusatzfelder(
                    new HashSet<>(antragAngenommen.getZusatzfelder()[i]));
        }
        Set<Veranstaltung> veranstaltungenSet =
                new HashSet<>(antragAngenommen.getVeranstaltungen());

        Modul modul = antragAngenommen.getModul();
        modul.setVeranstaltungen(veranstaltungenSet);

        String jsonModulAenderung = JsonService.modulToJsonObject(modul);

        Antrag antrag = antragService.getAntragById(Long.parseLong(id));
        antrag.setJsonModulAenderung(jsonModulAenderung);
        antragService.approveModulCreationAntrag(antrag);

        model.addAttribute("account",createAccountFromPrincipal(token));
        return "redirect:/module/administrator";
    }

}
