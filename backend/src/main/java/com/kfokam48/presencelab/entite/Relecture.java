package com.kfokam48.presencelab.entite;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Relecture d'un exercice par un pair. Le relecteur est un étudiant (CDC §2).
 * RG10 : un seul relecteur par exercice (contrainte uk_relecture_exercice).
 * RG14 : une fois rendue (rendueAt renseigné), la relecture est définitive.
 */
@Entity
@Table(name = "relecture")
public class Relecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercice_id", nullable = false)
    private Exercice exercice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relecteur_id", nullable = false)
    private Etudiant relecteur;

    private Integer note;

    @Column(length = 1000)
    private String commentaire;

    @Column(name = "assignee_at", nullable = false)
    private Instant assigneeAt;

    @Column(name = "rendue_at")
    private Instant rendueAt;

    protected Relecture() {
    }

    public Relecture(Exercice exercice, Etudiant relecteur, Instant assigneeAt) {
        this.exercice = exercice;
        this.relecteur = relecteur;
        this.assigneeAt = assigneeAt;
    }

    public boolean estRendue() {
        return rendueAt != null;
    }

    /** Enregistre la note et le commentaire ; l'appelant a vérifié RG12, RG13 et RG14. */
    public void rendre(int note, String commentaire, Instant maintenant) {
        this.note = note;
        this.commentaire = commentaire;
        this.rendueAt = maintenant;
    }

    public Long getId() {
        return id;
    }

    public Exercice getExercice() {
        return exercice;
    }

    public Etudiant getRelecteur() {
        return relecteur;
    }

    public Integer getNote() {
        return note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public Instant getAssigneeAt() {
        return assigneeAt;
    }

    public Instant getRendueAt() {
        return rendueAt;
    }
}
