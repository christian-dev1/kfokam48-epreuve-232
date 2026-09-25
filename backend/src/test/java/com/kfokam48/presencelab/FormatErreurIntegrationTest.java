package com.kfokam48.presencelab;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * ENF5 / B4 : l'application démarre (migration Flyway V1 sur H2, schéma validé par Hibernate)
 * et une adresse inconnue renvoie le format imposé { code, message }, pas la page d'erreur Spring.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FormatErreurIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void uneAdresseInconnueRenvoie404AuFormatImpose() throws Exception {
        mvc.perform(get("/api/n-existe-pas"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }
}
