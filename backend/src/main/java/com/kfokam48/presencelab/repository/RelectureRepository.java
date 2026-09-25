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
}
