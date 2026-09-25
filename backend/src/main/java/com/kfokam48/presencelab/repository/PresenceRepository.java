package com.kfokam48.presencelab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kfokam48.presencelab.entite.Etudiant;
import com.kfokam48.presencelab.entite.Presence;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    /** Candidats relecteurs (RG11) : les étudiants présents à la session. */
    @Query("select p.etudiant from Presence p where p.session.id = :sessionId")
    List<Etudiant> etudiantsPresents(@Param("sessionId") Long sessionId);
}
