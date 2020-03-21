package mops.module.controller;

import static mops.module.keycloak.KeycloakMopsAccount.createAccountFromPrincipal;

import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;


@Controller
@SessionScope
@RequestMapping("/module")
public class ModulbeauftragterController {

    @GetMapping("/modulbeauftragter")
    @Secured("ROLE_orga")
    public String module(KeycloakAuthenticationToken token, Model model) {
        model.addAttribute("account", createAccountFromPrincipal(token));
        return "modulbeauftragter";
    }


    //    TODO TEST HIERFÜR UND ÜBERDENKEN
    @GetMapping("/modulerstellung")
    @Secured("ROLE_orga")
    public String result(
            @RequestParam(name = "veranstaltungsanzahl", required = true) int veranstaltungsanzahl,
            Model model,
            KeycloakAuthenticationToken token) {
        model.addAttribute("account", createAccountFromPrincipal(token));

        List<String> listForVeranstaltungen = new ArrayList<String>();
        int i = 0;
        if(veranstaltungsanzahl > 0) {
            while(i < veranstaltungsanzahl) {
                String s = String.valueOf(i+1);
                listForVeranstaltungen.add(s);
                i++;
            }
        }
        model.addAttribute("veranstaltungsanzahl", listForVeranstaltungen);
        return "modulerstellung";
    }


    //    TODO MAPPING FÜR DAS ERSTELLEN VON MODULEN




}
