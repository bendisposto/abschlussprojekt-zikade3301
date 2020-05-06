package mops.module.controllertests;

import static mops.module.controllertests.AuthenticationTokenGenerator.generateAuthenticationToken;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mops.module.database.Modul;
import mops.module.database.Veranstaltung;
import mops.module.generator.ModulFaker;
import mops.module.services.ModulService;
import mops.module.services.VeranstaltungService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SemesterTagControllerTest {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mvc;


    @MockBean
    ModulService modulServiceMock;

    @MockBean
    VeranstaltungService veranstaltungService;

    private Modul testmodul;
    private Veranstaltung testVeranstaltung;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .alwaysDo(print())
                .apply(springSecurity())
                .build();

        testmodul = ModulFaker.generateFakeModul();
        testmodul.setId((long) 3301);
        testVeranstaltung = testmodul
                .getVeranstaltungen()
                .stream()
                .findFirst()
                .orElse(null);
        if (testVeranstaltung == null) {
            setUp();
        } else {
            testVeranstaltung.setId(1L);
            testVeranstaltung.setSemester(Collections.singleton("SoSe1995"));
        }


    }

    private final String expect = "redirect:/module/modulbeauftragter";


    @Test
    void testSemesterTagAccessForAdministrator() throws Exception {
        SecurityContextHolder
                .getContext()
                .setAuthentication(generateAuthenticationToken("sekretariat"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        List<String> veranstaltungsIds = Arrays.asList("1");
        params.addAll("veranstaltungsIds", veranstaltungsIds);

        when(veranstaltungService.getVeranstaltungById(Long.parseLong(veranstaltungsIds.get(0)))).thenReturn(testVeranstaltung);

        mvc.perform(post("/module/semesterTag/create")
                .params(params)
                .param("semester", "SoSe2020"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(expect));
    }

    @Test
    void testSemesterTagNoAccessIfNotLoggedIn() {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        List<String> veranstaltungsIds = Arrays.asList("1");
        params.addAll("veranstaltungsIds", veranstaltungsIds);

        assertThrows(AssertionError.class,
                () -> {
                    mvc.perform(post("/module/semesterTag/create")
                            .params(params)
                            .param("semester", "SoSe2020"))
                            .andExpect(status().is3xxRedirection())
                            .andExpect(view().name(expect));
                });
    }

    @Test
    void testSemesterTagNoAccessForOrganizers() {
        SecurityContextHolder
                .getContext()
                .setAuthentication(generateAuthenticationToken("orga"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        List<String> veranstaltungsIds = Arrays.asList("1");
        params.addAll("veranstaltungsIds", veranstaltungsIds);

        assertThrows(AssertionError.class,
                () -> {
                    mvc.perform(post("/module/semesterTag/create")
                            .params(params)
                            .param("semester", "SoSe2020"))
                            .andExpect(status().is3xxRedirection())
                            .andExpect(view().name(expect));
                });
    }

    @Test
    void testSemesterTagNoAccessForStudents() {
        SecurityContextHolder
                .getContext()
                .setAuthentication(generateAuthenticationToken("studentin"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        List<String> veranstaltungsIds = Arrays.asList("1");
        params.addAll("veranstaltungsIds", veranstaltungsIds);

        assertThrows(AssertionError.class,
                () -> {
                    mvc.perform(post("/module/semesterTag/create")
                            .params(params)
                            .param("semester", "SoSe2020"))
                            .andExpect(status().is3xxRedirection())
                            .andExpect(view().name(expect));
                });
    }

    @Test
    void testSemesterTagCallsTagVeranstaltungSemester() throws Exception {
        SecurityContextHolder
                .getContext()
                .setAuthentication(generateAuthenticationToken("sekretariat"));

        List<String> veranstaltungsIds = Arrays.asList("1");
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.addAll("veranstaltungsIds", veranstaltungsIds);

        when(veranstaltungService.getVeranstaltungById(Long.parseLong(veranstaltungsIds.get(0)))).thenReturn(testVeranstaltung);

        mvc.perform(post("/module/semesterTag/create")
                .params(params)
                .param("semester", "SoSe2020"));
        verify(modulServiceMock)
                .tagVeranstaltungSemester(
                        "SoSe2020",
                        Long.parseLong("1"),
                        Long.parseLong("3301")
                );
    }

    @Test
    void testSemesterTagDeleteAccessForAdministrator() throws Exception {
        SecurityContextHolder
                .getContext()
                .setAuthentication(generateAuthenticationToken("sekretariat"));

        mvc.perform(post("/module/semesterTag/delete")
                .param("tagToDelete", "SoSe1995")
                .param("idVeranstaltungTagDelete", "1")
                .param("idModulTagDelete", "3301"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(expect));
    }

    @Test
    void testSemesterTagDeleteNoAccessIfNotLoggedIn() {

        assertThrows(AssertionError.class,
                () -> {
                    mvc.perform(post("/module/semesterTag/delete")
                            .param("tagToDelete", "SoSe1995")
                            .param("idVeranstaltungTagDelete", "1")
                            .param("idModulTagDelete", "3301"))
                            .andExpect(status().is3xxRedirection())
                            .andExpect(view().name(expect));
                });
    }

    @Test
    void testSemesterTagDeleteNoAccessForOrganizers() {
        SecurityContextHolder
                .getContext()
                .setAuthentication(generateAuthenticationToken("orga"));

        assertThrows(AssertionError.class,
                () -> {
                    mvc.perform(post("/module/semesterTag/delete")
                            .param("tagToDelete", "SoSe1995")
                            .param("idVeranstaltungTagDelete", "1")
                            .param("idModulTagDelete", "3301"))
                            .andExpect(status().is3xxRedirection())
                            .andExpect(view().name(expect));
                });
    }

    @Test
    void testSemesterTagDeleteNoAccessForStudents() {
        SecurityContextHolder
                .getContext()
                .setAuthentication(generateAuthenticationToken("studentin"));

        assertThrows(AssertionError.class,
                () -> {
                    mvc.perform(post("/module/semesterTag/delete")
                            .param("tagToDelete", "SoSe1995")
                            .param("idVeranstaltungTagDelete", "1")
                            .param("idModulTagDelete", "3301"))
                            .andExpect(status().is3xxRedirection())
                            .andExpect(view().name(expect));
                });
    }

    @Test
    void testSemesterTagDeleteCallsDeleteTagVeranstaltungSemester() throws Exception {
        SecurityContextHolder
                .getContext()
                .setAuthentication(generateAuthenticationToken("sekretariat"));

        mvc.perform(post("/module/semesterTag/delete")
                .param("tagToDelete", "SoSe1995")
                .param("idVeranstaltungTagDelete", "1")
                .param("idModulTagDelete", "3301"));

        verify(modulServiceMock)
                .deleteTagVeranstaltungSemester(
                        "SoSe1995",
                        Long.parseLong("1"),
                        Long.parseLong("3301")
                );
    }

}