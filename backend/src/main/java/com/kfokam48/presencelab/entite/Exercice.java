package com.kfokam48.presencelab.entite;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** RG6 : un exercice par étudiant et par session (contrainte uk_exercice_session_etudiant). */
@Entity
@Table(name = "exercice")
public class Exercice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SessionCours session;

    /** L'auteur de l'exercice. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Column(nullable = false, length = 500)
    private String lien;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private StatutExercice statut;

    @Column(name = "depose_at", nullable = false)
    private Instant deposeAt;

    protected Exercice() {
    }

    public Exercice(SessionCours session, Etudiant etudiant, String lien, Instant deposeAt) {
        this.session = session;
        this.etudiant = etudiant;
        this.lien = lien;
        this.deposeAt = deposeAt;
        this.statut = StatutExercice.DEPOSE;
    }

    /** D4 : DEPOSE → EN_ATTENTE_RELECTURE quand un relecteur est tiré au sort. */
    public void mettreEnAttenteDeRelecture() {
        this.statut = StatutExercice.EN_ATTENTE_RELECTURE;
    }

    /** Nombre de relecteurs par exercice (RG10 v2, changement de l'étape 3). */
    public static final int NB_RELECTEURS = 2;

    /** D4 v2 : 1 note rendue → PARTIELLEMENT_RELU (provisoire, RG20) ; 2 notes → RELU (RG19). */
    public void enregistrerNotesRendues(long nombreDeNotesRendues) {
        if (nombreDeNotesRendues >= NB_RELECTEURS) {
            this.statut = StatutExercice.RELU;
        } else if (nombreDeNotesRendues > 0) {
            this.statut = StatutExercice.PARTIELLEMENT_RELU;
        }
    }

    public Long getId() {
        return id;
    }

    public SessionCours getSession() {
        return session;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public String getLien() {
        return lien;
    }

    public StatutExercice getStatut() {
        return statut;
    }

    public Instant getDeposeAt() {
        return deposeAt;
    }
}
