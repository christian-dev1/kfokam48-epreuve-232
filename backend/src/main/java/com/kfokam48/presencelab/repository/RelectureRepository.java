package com.kfokam48.presencelab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kfokam48.presencelab.entite.Relecture;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    /** Charge d'un relecteur : ses relectures pas encore rendues (RG11, RG17). */
    long countByRelecteurIdAndRendueAtIsNull(Long relecteurId);

    /** Relectures assignées à un étudiant, avec l'exercice et la session (évite le N+1). */
    @Query("select r from Relecture r join fetch r.exercice x join fetch x.session where r.relecteur.id = :relecteurId")
    List<Relecture> assigneesA(@Param("relecteurId") Long relecteurId);

    /** Nombre de notes rendues sur un exercice (D4 v2 : PARTIELLEMENT_RELU ou RELU). */
    long countByExerciceIdAndRendueAtIsNotNull(Long exerciceId);

    /**
     * Tableau v2 (RG19) : par exercice d'un auteur, moyenne et nombre des notes rendues
     * — [etudiantId, exerciceId, moyenne, nombreDeNotes].
     */
    @Query("select x.etudiant.id, x.id, avg(r.note), count(r) from Relecture r join r.exercice x"
            + " where x.etudiant.promotion.id = :promotionId and r.rendueAt is not null"
            + " group by x.etudiant.id, x.id")
    List<Object[]> notesParExercice(@Param("promotionId") Long promotionId);

    /** Tableau (RG17) : relectures que chaque étudiant doit encore rendre — [relecteurId, nombre]. */
    @Query("select r.relecteur.id, count(r) from Relecture r"
            + " where r.relecteur.promotion.id = :promotionId and r.rendueAt is null group by r.relecteur.id")
    List<Object[]> enAttenteParRelecteur(@Param("promotionId") Long promotionId);
}
