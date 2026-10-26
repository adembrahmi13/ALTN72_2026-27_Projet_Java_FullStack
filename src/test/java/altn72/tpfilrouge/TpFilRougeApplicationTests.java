package altn72.tpfilrouge;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@SpringBootTest
@AutoConfigureMockMvc
class ConnexionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pageProtegeRedirigeUnVisiteurVersLaConnexion() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/connexion"));
    }

    @Test
    void identifiantsInvalidesReaffichentLaPageDeConnexionAvecErreur() throws Exception {
        mockMvc.perform(post("/connexion").with(csrf())
                        .param("login", "inconnu")
                        .param("motDePasse", "faux"))
                .andExpect(redirectedUrl("/connexion?erreur"));
    }

    @Test
    void pageDeConnexionEstAccessibleSansAuthentification() throws Exception {
        mockMvc.perform(get("/connexion"))
                .andExpect(status().isOk())
                .andExpect(view().name("connexion"));
    }
}