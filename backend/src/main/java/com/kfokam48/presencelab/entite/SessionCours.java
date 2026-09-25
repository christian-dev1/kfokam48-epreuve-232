package com.kfokam48.presencelab.entite;

import java.time.Duration;
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

/** Une séance de cours ouverte par le formateur (table session_cours). */
@Entity
@Table(name = "session_cours")
public class SessionCours {

    /** RG1 : le code de présence expire 15 minutes après l'ouverture. */
    public static final Duration VALIDITE_CODE = Duration.ofMinutes(15);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(nullable = false, length = 6, unique = true)
    private String code;

    @Column(name = "ouverture_at", nullable = false)
    private Instant ouvertureAt;

    @Column(name = "expiration_at", nullable = false)
    private Instant expirationAt;

    @Column(name = "cloture_at")
    private Instant clotureAt;

    protected SessionCours() {
    }

    public SessionCours(String titre, Promotion promotion, String code, Instant ouvertureAt) {
        this.titre = titre;
        this.promotion = promotion;
        this.code = code;
        this.ouvertureAt = ouvertureAt;
        this.expirationAt = ouvertureAt.plus(VALIDITE_CODE);
    }

    /** RG1 : à l'instant exact de l'expiration, le code ne marche plus. */
    public boolean codeExpire(Instant maintenant) {
        return !maintenant.isBefore(expirationAt);
    }

    /** H3 : « fin de session » = clôture par le formateur. */
    public boolean estCloturee() {
        return clotureAt != null;
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public String getCode() {
        return code;
    }

    public Instant getOuvertureAt() {
        return ouvertureAt;
    }

    public Instant getExpirationAt() {
        return expirationAt;
    }

    public Instant getClotureAt() {
        return clotureAt;
    }
}
