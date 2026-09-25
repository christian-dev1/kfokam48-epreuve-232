package com.kfokam48.presencelab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kfokam48.presencelab.entite.Exercice;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
}
