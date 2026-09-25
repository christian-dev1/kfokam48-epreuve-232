package com.kfokam48.presencelab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kfokam48.presencelab.entite.Relecture;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    /** Charge d'un relecteur : ses relectures pas encore rendues (RG11, RG17). */
    long countByRelecteurIdAndRendueAtIsNull(Long relecteurId);
}
