package com.kfokam48.presencelab.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.ToLongFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kfokam48.presencelab.entite.Etudiant;
import com.kfokam48.presencelab.entite.Exercice;
import com.kfokam48.presencelab.entite.Relecture;
import com.kfokam48.presencelab.repository.PresenceRepository;
import com.kfokam48.presencelab.repository.RelectureRepository;

/**
 * EF5 — Désignation du relecteur au dépôt d'un exercice.
 * RG11 : tirage au hasard parmi les étudiants présents à la session, en privilégiant les moins chargés.
 * RG12 : l'auteur n'est jamais candidat. H2 : sans candidat, l'exercice reste DEPOSE.
 */
@Component
public class AssignationRelecteur {

    private final PresenceRepository presences;
    private final RelectureRepository relectures;
    private final Clock horloge;
    private final Random aleatoire;

    @Autowired
    public AssignationRelecteur(PresenceRepository presences, RelectureRepository relectures, Clock horloge) {
        this(presences, relectures, horloge, new Random());
    }

    AssignationRelecteur(PresenceRepository presences, RelectureRepository relectures, Clock horloge, Random aleatoire) {
        this.presences = presences;
        this.relectures = relectures;
        this.horloge = horloge;
        this.aleatoire = aleatoire;
    }

    /** Assigne un relecteur si possible et met à jour le statut de l'exercice (D4). */
    public Optional<Relecture> assigner(Exercice exercice) {
        List<Etudiant> presents = presences.etudiantsPresents(exercice.getSession().getId());
        Optional<Etudiant> choisi = choisir(presents, exercice.getEtudiant(),
                e -> relectures.countByRelecteurIdAndRendueAtIsNull(e.getId()), aleatoire);
        return choisi.map(relecteur -> {
            exercice.mettreEnAttenteDeRelecture();
            return relectures.save(new Relecture(exercice, relecteur, Instant.now(horloge)));
        });
    }

    /** Règle de choix, pure et testable : auteur exclu, charge minimale, puis hasard. */
    static Optional<Etudiant> choisir(List<Etudiant> presents, Etudiant auteur,
                                      ToLongFunction<Etudiant> charge, Random aleatoire) {
        List<Etudiant> candidats = presents.stream()
                .filter(e -> !Objects.equals(e.getId(), auteur.getId())) // RG12
                .toList();
        if (candidats.isEmpty()) {
            return Optional.empty(); // H2
        }
        long chargeMin = candidats.stream().mapToLong(charge).min().orElse(0);
        List<Etudiant> moinsCharges = candidats.stream()
                .filter(e -> charge.applyAsLong(e) == chargeMin)
                .sorted(Comparator.comparing(Etudiant::getId))
                .toList();
        return Optional.of(moinsCharges.get(aleatoire.nextInt(moinsCharges.size())));
    }
}
