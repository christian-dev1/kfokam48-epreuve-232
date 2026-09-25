package com.kfokam48.presencelab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kfokam48.presencelab.entite.Presence;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
}
