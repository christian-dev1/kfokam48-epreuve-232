package com.kfokam48.presencelab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kfokam48.presencelab.entite.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findAllByOrderByNomAsc();
}
