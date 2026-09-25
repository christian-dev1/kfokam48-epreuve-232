package com.kfokam48.presencelab.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kfokam48.presencelab.entite.Etudiant;
import com.kfokam48.presencelab.entite.Promotion;
import com.kfokam48.presencelab.entite.SessionCours;
import com.kfokam48.presencelab.erreur.DejaPresentException;
import com.kfokam48.presencelab.repository.EtudiantRepository;
import com.kfokam48.presencelab.repository.PresenceRepository;
import com.kfokam48.presencelab.repository.PromotionRepository;
import com.kfokam48.presencelab.repository.SessionCoursRepository;

/**
 * Bug #23 — scénario du client rejoué pour de vrai : des requêtes simultanées, chacune dans sa propre
 * transaction (pas de @Transactional ici), sur la vraie base de test.
 */
@SpringBootTest
class PresenceConcurrenceIntegrationTest {

    private static final int NB_ETUDIANTS = 10;

    @Autowired private PresenceService service;
    @Autowired private PromotionRepository promotions;
    @Autowired private EtudiantRepository etudiants;
    @Autowired private SessionCoursRepository sessions;
    @Autowired private PresenceRepository presences;

    private Promotion promotion;
    private SessionCours session;
    private final List<Etudiant> classe = new ArrayList<>();

    @BeforeEach
    void preparer() {
        promotion = promotions.save(new Promotion("Promo concurrence"));
        for (int i = 0; i < NB_ETUDIANTS; i++) {
            classe.add(etudiants.save(new Etudiant("Etudiant " + i, promotion)));
        }
        session = sessions.save(new SessionCours("Séance simultanée", promotion, "CONC42", Instant.now()));
    }

    @AfterEach
    void nettoyer() {
        presences.deleteAll();
        sessions.delete(session);
        etudiants.deleteAll(classe);
        promotions.delete(promotion);
    }

    /** Lance toutes les tâches au même instant et renvoie leurs résultats (ou l'exception levée). */
    private List<Object> enMemeTemps(List<Callable<Object>> taches) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(taches.size());
        CountDownLatch depart = new CountDownLatch(1);
        List<Future<Object>> futurs = new ArrayList<>();
        for (Callable<Object> tache : taches) {
            futurs.add(pool.submit(() -> {
                depart.await();
                try {
                    return tache.call();
                } catch (Exception e) {
                    return e;
                }
            }));
        }
        depart.countDown();
        List<Object> resultats = new ArrayList<>();
        for (Future<Object> f : futurs) {
            resultats.add(f.get(30, TimeUnit.SECONDS));
        }
        pool.shutdown();
        return resultats;
    }

    @Test
    void dixEtudiantsQuiTapentLeCodeEnMemeTempsSontTousEnregistres() throws Exception {
        List<Callable<Object>> taches = new ArrayList<>();
        for (Etudiant e : classe) {
            taches.add(() -> service.marquerPresence("CONC42", e.getId()));
        }

        List<Object> resultats = enMemeTemps(taches);

        assertThat(resultats).noneMatch(r -> r instanceof Exception);
        assertThat(presences.count()).isEqualTo(NB_ETUDIANTS);
    }

    @Test
    void unDoubleEnvoiSimultaneDonneUnSucces201EtUnDejaPresent409() throws Exception {
        Long alice = classe.get(0).getId();
        List<Callable<Object>> taches = List.of(
                () -> service.marquerPresence("CONC42", alice),
                () -> service.marquerPresence("CONC42", alice));

        List<Object> resultats = enMemeTemps(taches);

        assertThat(resultats).filteredOn(r -> !(r instanceof Exception)).hasSize(1);
        assertThat(resultats).filteredOn(r -> r instanceof Exception)
                .hasSize(1)
                .allMatch(r -> r instanceof DejaPresentException); // jamais une erreur générique
        assertThat(presences.count()).isEqualTo(1);
    }
}
