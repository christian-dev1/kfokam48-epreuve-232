package com.kfokam48.presencelab.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.kfokam48.presencelab.dto.PresenceDto;
import com.kfokam48.presencelab.entite.Etudiant;
import com.kfokam48.presencelab.entite.Presence;
import com.kfokam48.presencelab.entite.Promotion;
import com.kfokam48.presencelab.entite.SessionCours;
import com.kfokam48.presencelab.entite.SourcePresence;
import com.kfokam48.presencelab.erreur.CodeExpireException;
import com.kfokam48.presencelab.erreur.CodeInconnuException;
import com.kfokam48.presencelab.erreur.DejaPresentException;
import com.kfokam48.presencelab.repository.EtudiantRepository;
import com.kfokam48.presencelab.repository.PresenceRepository;
import com.kfokam48.presencelab.repository.SessionCoursRepository;

/**
 * B6 — Test unitaire des règles métier du marquage de présence, sans base ni Spring :
 * RG1 (expiration à 15 min), RG2 (clôture), RG3 (une seule présence), RG4 (source ETUDIANT).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PresenceServiceTest {

    private static final Instant OUVERTURE = Instant.parse("2026-09-25T09:00:00Z");
    private static final String CODE = "K7P2QX";

    @Mock
    private SessionCoursRepository sessions;
    @Mock
    private EtudiantRepository etudiants;
    @Mock
    private PresenceRepository presences;

    private SessionCours session;
    private Etudiant alice;

    @BeforeEach
    void preparer() {
        Promotion promotion = new Promotion("KFOKAM48 - Batch 2");
        ReflectionTestUtils.setField(promotion, "id", 1L);
        alice = new Etudiant("Alice Mbarga", promotion);
        ReflectionTestUtils.setField(alice, "id", 10L);
        session = new SessionCours("Séance 3", promotion, CODE, OUVERTURE);
        ReflectionTestUtils.setField(session, "id", 100L);

        when(etudiants.findById(10L)).thenReturn(Optional.of(alice));
        when(sessions.findByCode(CODE)).thenReturn(Optional.of(session));
        when(presences.save(any(Presence.class))).thenAnswer(appel -> appel.getArgument(0));
        when(presences.saveAndFlush(any(Presence.class))).thenAnswer(appel -> appel.getArgument(0));
    }

    private PresenceService serviceA(Instant maintenant) {
        return new PresenceService(sessions, etudiants, presences, Clock.fixed(maintenant, ZoneOffset.UTC));
    }

    @Test
    void RG1_leCodeEstAccepteJusteAvantLes15Minutes() {
        PresenceDto presence = serviceA(OUVERTURE.plus(Duration.ofMinutes(14)).plusSeconds(59))
                .marquerPresence(CODE, 10L);

        assertThat(presence.source()).isEqualTo(SourcePresence.ETUDIANT); // RG4
        assertThat(presence.sessionId()).isEqualTo(100L);
        assertThat(presence.etudiantId()).isEqualTo(10L);
    }

    @Test
    void RG1_leCodeExpireExactementA15Minutes() {
        PresenceService service = serviceA(OUVERTURE.plus(Duration.ofMinutes(15)));

        assertThatThrownBy(() -> service.marquerPresence(CODE, 10L))
                .isInstanceOf(CodeExpireException.class)
                .hasMessage("Le code de présence a expiré.");
        verify(presences, never()).save(any());
    }

    @Test
    void RG2_uneSessionClotureeRefuseLeCodeMemeAvantExpiration() {
        ReflectionTestUtils.setField(session, "clotureAt", OUVERTURE.plusSeconds(60));

        assertThatThrownBy(() -> serviceA(OUVERTURE.plusSeconds(120)).marquerPresence(CODE, 10L))
                .isInstanceOf(CodeExpireException.class);
    }

    @Test
    void RG3_unEtudiantDejaPresentEstRefuse() {
        when(presences.existsBySessionIdAndEtudiantId(100L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> serviceA(OUVERTURE.plusSeconds(30)).marquerPresence(CODE, 10L))
                .isInstanceOf(DejaPresentException.class);
        verify(presences, never()).save(any());
    }

    @Test
    void unCodeInconnuEstRefuse() {
        when(sessions.findByCode("ZZZZZZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceA(OUVERTURE).marquerPresence("ZZZZZZ", 10L))
                .isInstanceOf(CodeInconnuException.class);
    }

    @Test
    void leCodeSaisiEnMinusculesAvecEspacesEstReconnu() {
        PresenceDto presence = serviceA(OUVERTURE.plusSeconds(30)).marquerPresence("  k7p2qx ", 10L);

        assertThat(presence.source()).isEqualTo(SourcePresence.ETUDIANT);
    }

    /**
     * Bug #23 — deux requêtes arrivent « presque en même temps » : toutes deux passent le contrôle
     * existsBy… avant que l'une n'ait écrit. La contrainte uk_presence_session_etudiant rejette la seconde.
     * Attendu : un 409 DEJA_PRESENT clair, pas une erreur générique ni une présence perdue en silence.
     */
    @Test
    void BUG_uneCourseEntreDeuxRequetesDonneDejaPresentEtNonUneErreurGenerique() {
        when(presences.existsBySessionIdAndEtudiantId(100L, 10L)).thenReturn(false); // la course : le contrôle passe
        when(presences.saveAndFlush(any(Presence.class)))
                .thenThrow(new DataIntegrityViolationException("uk_presence_session_etudiant"));
        when(presences.save(any(Presence.class)))
                .thenThrow(new DataIntegrityViolationException("uk_presence_session_etudiant"));

        assertThatThrownBy(() -> serviceA(OUVERTURE.plusSeconds(30)).marquerPresence(CODE, 10L))
                .isInstanceOf(DejaPresentException.class);
    }
}
