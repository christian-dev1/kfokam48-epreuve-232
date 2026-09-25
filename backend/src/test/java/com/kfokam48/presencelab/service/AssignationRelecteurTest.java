package com.kfokam48.presencelab.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.kfokam48.presencelab.entite.Etudiant;
import com.kfokam48.presencelab.entite.Promotion;

/** RG11 (tirage parmi les présents, moins chargés d'abord), RG12 (jamais l'auteur), H2 (aucun candidat). */
class AssignationRelecteurTest {

    private final Promotion promotion = new Promotion("Batch 2");
    private final Etudiant auteur = etudiant(1L, "Alice");
    private final Etudiant bruno = etudiant(2L, "Bruno");
    private final Etudiant carine = etudiant(3L, "Carine");

    private Etudiant etudiant(long id, String nom) {
        Etudiant e = new Etudiant(nom, promotion);
        ReflectionTestUtils.setField(e, "id", id);
        return e;
    }

    @Test
    void RG12_lAuteurNEstJamaisTireMemeSurMilleTirages() {
        Random aleatoire = new Random(42);
        Set<Long> tires = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            AssignationRelecteur.choisir(List.of(auteur, bruno, carine), auteur, e -> 0L, aleatoire)
                    .ifPresent(e -> tires.add(e.getId()));
        }
        assertThat(tires).containsExactlyInAnyOrder(2L, 3L); // hasard réel entre Bruno et Carine, jamais Alice
    }

    @Test
    void H2_siLAuteurEstSeulPresentIlNYAPasDeRelecteur() {
        Optional<Etudiant> choisi = AssignationRelecteur.choisir(List.of(auteur), auteur, e -> 0L, new Random());
        assertThat(choisi).isEmpty();
    }

    @Test
    void RG11_leMoinsChargeEstPrivilegie() {
        Map<Long, Long> charge = Map.of(2L, 3L, 3L, 0L);
        Optional<Etudiant> choisi = AssignationRelecteur.choisir(List.of(auteur, bruno, carine), auteur,
                e -> charge.get(e.getId()), new Random());
        assertThat(choisi).contains(carine);
    }
}
