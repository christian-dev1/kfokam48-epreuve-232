package com.kfokam48.presencelab.controleur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.kfokam48.presencelab.entite.Etudiant;
import com.kfokam48.presencelab.entite.Exercice;
import com.kfokam48.presencelab.entite.Promotion;
import com.kfokam48.presencelab.entite.Relecture;
import com.kfokam48.presencelab.entite.SessionCours;
import com.kfokam48.presencelab.entite.StatutExercice;
import com.kfokam48.presencelab.repository.EtudiantRepository;
import com.kfokam48.presencelab.repository.ExerciceRepository;
import com.kfokam48.presencelab.repository.PromotionRepository;
import com.kfokam48.presencelab.repository.RelectureRepository;
import com.kfokam48.presencelab.repository.SessionCoursRepository;

/**
 * B6 — Test d'intégration de l'endpoint imposé POST /api/relectures/{id} : vraie pile Spring,
 * migrations Flyway sur H2 en mémoire, codes HTTP et format d'erreur du contrat.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RelectureIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private PromotionRepository promotions;
    @Autowired private EtudiantRepository etudiants;
    @Autowired private SessionCoursRepository sessions;
    @Autowired private ExerciceRepository exercices;
    @Autowired private RelectureRepository relectures;

    private Etudiant alice;
    private Etudiant bruno;
    private Exercice exercice;
    private Relecture relecture;

    @BeforeEach
    void preparer() {
        Promotion promotion = promotions.save(new Promotion("Promo test relecture"));
        alice = etudiants.save(new Etudiant("Alice", promotion));
        bruno = etudiants.save(new Etudiant("Bruno", promotion));
        SessionCours session = sessions.save(new SessionCours("Séance test", promotion, "TST001", Instant.now()));
        exercice = exercices.save(new Exercice(session, alice, "https://github.com/alice/tp", Instant.now()));
        exercice.mettreEnAttenteDeRelecture();
        relecture = relectures.save(new Relecture(exercice, bruno, Instant.now()));
    }

    private ResultActions rendre(Long id, String json) throws Exception {
        return mvc.perform(post("/api/relectures/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void EF7_uneRelectureValideRenvoie200EtLExercicePasseRelu() throws Exception {
        rendre(relecture.getId(), "{\"note\": 15, \"commentaire\": \"Bon travail\", \"relecteurId\": " + bruno.getId() + "}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(15))
                .andExpect(jsonPath("$.rendue").value(true));

        assertThat(exercices.findById(exercice.getId()).orElseThrow().getStatut()).isEqualTo(StatutExercice.RELU);
    }

    @Test
    void RG13_uneNoteHorsBornesRenvoie400NoteInvalide() throws Exception {
        rendre(relecture.getId(), "{\"note\": 21, \"commentaire\": \"Trop\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOTE_INVALIDE"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void RG13_uneNoteDecimaleEstRefuseeEtNonTronquee() throws Exception {
        rendre(relecture.getId(), "{\"note\": 12.5, \"commentaire\": \"Décimale\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
    }

    @Test
    void RG12_relireSonPropreExerciceRenvoie403() throws Exception {
        rendre(relecture.getId(), "{\"note\": 20, \"commentaire\": \"Moi\", \"relecteurId\": " + alice.getId() + "}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTO_RELECTURE"));
    }

    @Test
    void RG14_uneRelectureDejaRendueRenvoie409() throws Exception {
        rendre(relecture.getId(), "{\"note\": 14, \"commentaire\": \"Premier envoi\"}").andExpect(status().isOk());

        rendre(relecture.getId(), "{\"note\": 18, \"commentaire\": \"Je change d'avis\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));
    }

    @Test
    void uneRelectureInconnueRenvoie404AuFormatImpose() throws Exception {
        rendre(999_999L, "{\"note\": 10, \"commentaire\": \"?\"}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RELECTURE_INCONNUE"));
    }

    @Test
    void EF6_leRelecteurVoitSaRelectureSansLeNomDeLAuteur() throws Exception {
        mvc.perform(get("/api/relectures").param("relecteurId", bruno.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lien").value("https://github.com/alice/tp"))
                .andExpect(jsonPath("$[0].rendue").value(false))
                .andExpect(jsonPath("$[0].auteur").doesNotExist());
    }
}
