package com.kfokam48.presencelab.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** RG7 : seul un lien http(s) absolu avec un hôte est accepté. */
class LienExerciceTest {

    @ParameterizedTest
    @ValueSource(strings = {"https://github.com/alice/tp", "http://exemple.cm/tp?x=1", "HTTPS://GitHub.com/a"})
    void accepteLesLiensHttpEtHttps(String lien) {
        assertThat(ExerciceService.lienValide(lien)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "github.com/alice", "ftp://serveur/tp", "javascript:alert(1)", "https://", "pas un lien", "https://exa mple.com"})
    void refuseLesLiensInvalides(String lien) {
        assertThat(ExerciceService.lienValide(lien)).isFalse();
    }
}
