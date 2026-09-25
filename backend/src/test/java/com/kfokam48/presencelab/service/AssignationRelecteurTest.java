package com.kfokam48.presencelab.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.kfokam48.presencelab.entite.Etudiant;
import com.kfokam48.presencelab.entite.Promotion;

/**
 * RG10 v2 (deux relecteurs différents), RG11 (tirage parmi les présents, moins chargés d'abord),
 * RG12 (jamais l'auteur), H2 (aucun candidat), H11 (un seul candidat).
 */
class AssignationRelecteurTest {

    private final Promotion promotion = new Promotion("Batch 2");
    private final Etudiant auteur = etudiant(1L, "Alice");
    private final Etudiant bruno = etudiant(2L, "Bruno");
    private final Etudiant carine = etudiant(3L, "Carine");
    private final Etudiant david = etudiant(4L, "David");

    private Etudiant etudiant(long id, String nom) {
        Etudiant e = new Etudiant(nom, promotion);
        ReflectionTestUtils.setField(e, "id", id);
        return e;
    }

    @Test
    void RG10_RG12_deuxRelecteursDifferentsJamaisLAuteurSurMilleTirages() {
        Random aleatoire = new Random(42);
        Set<Long> vus = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            List<Etudiant> choisis = AssignationRelecteur.choisir(
                    List.of(auteur, bruno, carine, david), auteur, e -> 0L, aleatoire, 2);
            assertThat(choisis).hasSize(2);
            assertThat(choisis.get(0).getId()).isNotEqualTo(choisis.get(1).getId()); // deux pairs différents
            assertThat(choisis).noneMatch(e -> e.getId().equals(1L));               // jamais l'auteur
            choisis.forEach(e -> vus.add(e.getId()));
        }
        assertThat(vus).containsExactlyInAnyOrder(2L, 3L, 4L); // le hasard couvre tous les candidats
    }

    @Test
    void H2_siLAuteurEstSeulPresentIlNYAPasDeRelecteur() {
        assertThat(AssignationRelecteur.choisir(List.of(auteur), auteur, e -> 0L, new Random(), 2)).isEmpty();
    }

    @Test
    void H11_unSeulCandidatDonneUneSeuleRelecture() {
        assertThat(AssignationRelecteur.choisir(List.of(auteur, bruno), auteur, e -> 0L, new Random(), 2))
                .containsExactly(bruno);
    }

    @Test
    void RG11_lesMoinsChargesSontPrivilegies() {
        Map<Long, Long> charge = Map.of(2L, 5L, 3L, 0L, 4L, 1L);
        List<Etudiant> choisis = AssignationRelecteur.choisir(List.of(auteur, bruno, carine, david), auteur,
                e -> charge.get(e.getId()), new Random(), 2);
        assertThat(choisis).containsExactly(carine, david);
    }
}
