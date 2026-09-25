package com.kfokam48.presencelab.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
 * EF5 — Désignation des relecteurs au dépôt d'un exercice.
 * RG10 v2 : deux relecteurs DIFFÉRENTS (un seul s'il n'y a qu'un candidat, H11).
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

    /** Assigne jusqu'à deux relecteurs et met à jour le statut de l'exercice (D4). */
    public List<Relecture> assigner(Exercice exercice) {
        List<Etudiant> presents = presences.etudiantsPresents(exercice.getSession().getId());
        List<Etudiant> choisis = choisir(presents, exercice.getEtudiant(),
                e -> relectures.countByRelecteurIdAndRendueAtIsNull(e.getId()), aleatoire, Exercice.NB_RELECTEURS);
        if (!choisis.isEmpty()) {
            exercice.mettreEnAttenteDeRelecture();
        }
        Instant maintenant = Instant.now(horloge);
        return choisis.stream()
                .map(relecteur -> relectures.save(new Relecture(exercice, relecteur, maintenant)))
                .toList();
    }

    /**
     * Règle de choix, pure et testable : auteur exclu (RG12), puis, pour chaque place,
     * les candidats les moins chargés et tirage au hasard parmi eux (RG11), sans jamais reprendre
     * un étudiant déjà choisi (RG10 v2 : relecteurs différents).
     */
    static List<Etudiant> choisir(List<Etudiant> presents, Etudiant auteur,
                                  ToLongFunction<Etudiant> charge, Random aleatoire, int nombre) {
        List<Etudiant> restants = new ArrayList<>(presents.stream()
                .filter(e -> !Objects.equals(e.getId(), auteur.getId())) // RG12
                .sorted(Comparator.comparing(Etudiant::getId))
                .toList());
        List<Etudiant> choisis = new ArrayList<>();
        while (choisis.size() < nombre && !restants.isEmpty()) {
            long chargeMin = restants.stream().mapToLong(charge).min().orElse(0);
            List<Etudiant> moinsCharges = restants.stream()
                    .filter(e -> charge.applyAsLong(e) == chargeMin)
                    .toList();
            Etudiant choisi = moinsCharges.get(aleatoire.nextInt(moinsCharges.size()));
            choisis.add(choisi);
            restants.remove(choisi);
        }
        return choisis; // vide = H2, un seul = H11
    }
}
