package com.kfokam48.presencelab.controleur;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.kfokam48.presencelab.entite.Etudiant;
import com.kfokam48.presencelab.entite.Exercice;
import com.kfokam48.presencelab.entite.Presence;
import com.kfokam48.presencelab.entite.Promotion;
import com.kfokam48.presencelab.entite.Relecture;
import com.kfokam48.presencelab.entite.SessionCours;
import com.kfokam48.presencelab.entite.SourcePresence;
import com.kfokam48.presencelab.repository.EtudiantRepository;
import com.kfokam48.presencelab.repository.ExerciceRepository;
import com.kfokam48.presencelab.repository.PresenceRepository;
import com.kfokam48.presencelab.repository.PromotionRepository;
import com.kfokam48.presencelab.repository.RelectureRepository;
import com.kfokam48.presencelab.repository.SessionCoursRepository;

/** EF8 : GET /api/tableau — moyenne calculée par l'API (RG16), relectures en attente (RG17), 404. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TableauIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private PromotionRepository promotions;
    @Autowired private EtudiantRepository etudiants;
    @Autowired private SessionCoursRepository sessions;
    @Autowired private PresenceRepository presences;
    @Autowired private ExerciceRepository exercices;
    @Autowired private RelectureRepository relectures;

    @Test
    void RG16_RG17_leTableauAgregeLesDonneesDeLaPromotion() throws Exception {
        Promotion promo = promotions.save(new Promotion("Promo tableau"));
        Etudiant alice = etudiants.save(new Etudiant("Alice", promo));
        Etudiant bruno = etudiants.save(new Etudiant("Bruno", promo));
        SessionCours s1 = sessions.save(new SessionCours("S1", promo, "TAB001", Instant.now()));
        SessionCours s2 = sessions.save(new SessionCours("S2", promo, "TAB002", Instant.now()));
        presences.save(new Presence(s1, alice, SourcePresence.ETUDIANT, Instant.now()));
        presences.save(new Presence(s2, alice, SourcePresence.ETUDIANT, Instant.now()));
        presences.save(new Presence(s1, bruno, SourcePresence.FORMATEUR, Instant.now()));

        Exercice e1 = exercices.save(new Exercice(s1, alice, "https://x.cm/1", Instant.now()));
        Exercice e2 = exercices.save(new Exercice(s2, alice, "https://x.cm/2", Instant.now()));
        Relecture r1 = relectures.save(new Relecture(e1, bruno, Instant.now()));
        r1.rendre(12, "ok", Instant.now());
        e1.marquerRelu();
        Relecture r2 = relectures.save(new Relecture(e2, bruno, Instant.now()));
        r2.rendre(15, "bien", Instant.now());
        e2.marquerRelu();
        Exercice e3 = exercices.save(new Exercice(s1, bruno, "https://x.cm/3", Instant.now()));
        e3.mettreEnAttenteDeRelecture();
        relectures.save(new Relecture(e3, alice, Instant.now())); // Alice doit encore relire Bruno

        mvc.perform(get("/api/tableau").param("promotionId", promo.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nom").value("Alice"))
                .andExpect(jsonPath("$[0].presences").value(2))
                .andExpect(jsonPath("$[0].exercicesDeposes").value(2))
                .andExpect(jsonPath("$[0].moyenne").value(13.5))
                .andExpect(jsonPath("$[0].relecturesEnAttente").value(1))
                .andExpect(jsonPath("$[1].nom").value("Bruno"))
                .andExpect(jsonPath("$[1].moyenne").value(nullValue()))
                .andExpect(jsonPath("$[1].exercicesEnAttente").value(1));
    }

    @Test
    void unePromotionInconnueRenvoie404() throws Exception {
        mvc.perform(get("/api/tableau").param("promotionId", "999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }

    @Test
    void sansPromotionIdLaRequeteEstRefusee() throws Exception {
        mvc.perform(get("/api/tableau"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARAMETRE_MANQUANT"));
    }
}
