package altn72.tpfilrouge;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class EquipementsParStationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listeDesStationsEstAffichee() throws Exception {
        mockMvc.perform(get("/stations"))
                .andExpect(status().isOk())
                .andExpect(view().name("stations"));
    }

    @Test
    void stationInexistanteAfficheLaPageErreurEn404() throws Exception {
        mockMvc.perform(get("/stations/{id}/equipements", Integer.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(view().name("erreur"));
    }
}
