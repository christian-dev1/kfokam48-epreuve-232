package com.kfokam48.presencelab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kfokam48.presencelab.entite.SessionCours;

public interface SessionCoursRepository extends JpaRepository<SessionCours, Long> {

    Optional<SessionCours> findByCode(String code);

    boolean existsByCode(String code);

    List<SessionCours> findByPromotionIdOrderByOuvertureAtDesc(Long promotionId);
}
