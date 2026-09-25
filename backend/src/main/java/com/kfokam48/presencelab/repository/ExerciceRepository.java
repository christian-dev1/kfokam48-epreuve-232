package com.kfokam48.presencelab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kfokam48.presencelab.entite.Exercice;
import com.kfokam48.presencelab.entite.StatutExercice;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    /** Tableau (EF8) : exercices déposés par étudiant — [etudiantId, nombre]. */
    @Query("select x.etudiant.id, count(x) from Exercice x where x.etudiant.promotion.id = :promotionId group by x.etudiant.id")
    List<Object[]> compterParEtudiant(@Param("promotionId") Long promotionId);

    /** Tableau (H9, Q11) : exercices pas encore relus par étudiant — [etudiantId, nombre]. */
    @Query("select x.etudiant.id, count(x) from Exercice x where x.etudiant.promotion.id = :promotionId"
            + " and x.statut <> :relu group by x.etudiant.id")
    List<Object[]> compterNonRelusParEtudiant(@Param("promotionId") Long promotionId, @Param("relu") StatutExercice relu);
}
