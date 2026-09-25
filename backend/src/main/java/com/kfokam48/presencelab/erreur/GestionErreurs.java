package com.kfokam48.presencelab.erreur;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * B4 — Gestion centralisée des erreurs. Toute exception devient { code, message } (contrat),
 * jamais une stack trace ni la page d'erreur par défaut de Spring.
 */
@RestControllerAdvice
public class GestionErreurs {

    private static final Logger LOG = LoggerFactory.getLogger(GestionErreurs.class);

    @ExceptionHandler(MetierException.class)
    public ResponseEntity<ErreurDto> metier(MetierException ex) {
        return reponse(ex.getStatut(), ex.getCode(), ex.getMessage());
    }

    /** @Valid sur un DTO : champ absent → CHAMP_MANQUANT ; note ou lien hors règle → code dédié. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurDto> validation(MethodArgumentNotValidException ex) {
        FieldError erreur = ex.getBindingResult().getFieldError();
        if (erreur == null) {
            return reponse(HttpStatus.BAD_REQUEST, "REQUETE_INVALIDE", "La requête est invalide.");
        }
        String champ = erreur.getField();
        boolean manquant = "NotNull".equals(erreur.getCode()) || "NotBlank".equals(erreur.getCode());
        if (manquant) {
            return reponse(HttpStatus.BAD_REQUEST, "CHAMP_MANQUANT", "Le champ « " + champ + " » est obligatoire.");
        }
        return switch (champ) {
            case "note" -> noteInvalide();
            case "lien" -> reponse(HttpStatus.BAD_REQUEST, "LIEN_INVALIDE", "Le lien doit être une adresse http ou https valide.");
            default -> reponse(HttpStatus.BAD_REQUEST, "REQUETE_INVALIDE", "Le champ « " + champ + " » est invalide.");
        };
    }

    /** JSON illisible ou mal typé ; une note décimale (12.5) arrive ici grâce à accept-float-as-int=false. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurDto> illisible(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof JsonMappingException jme) {
            List<JsonMappingException.Reference> chemin = jme.getPath();
            boolean surLaNote = chemin.stream().anyMatch(ref -> "note".equals(ref.getFieldName()));
            if (surLaNote) {
                return noteInvalide();
            }
        }
        return reponse(HttpStatus.BAD_REQUEST, "REQUETE_INVALIDE", "Le corps de la requête est illisible ou mal typé.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErreurDto> parametreManquant(MissingServletRequestParameterException ex) {
        return reponse(HttpStatus.BAD_REQUEST, "PARAMETRE_MANQUANT",
                "Le paramètre « " + ex.getParameterName() + " » est obligatoire.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErreurDto> mauvaisType(MethodArgumentTypeMismatchException ex) {
        return reponse(HttpStatus.BAD_REQUEST, "REQUETE_INVALIDE", "Le paramètre « " + ex.getName() + " » est invalide.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErreurDto> routeInconnue(NoResourceFoundException ex) {
        return reponse(HttpStatus.NOT_FOUND, "RESSOURCE_INTROUVABLE", "Cette adresse n'existe pas.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErreurDto> methode(HttpRequestMethodNotSupportedException ex) {
        return reponse(HttpStatus.METHOD_NOT_ALLOWED, "METHODE_NON_AUTORISEE", "Cette méthode HTTP n'est pas autorisée ici.");
    }

    /** Filet de sécurité : une contrainte UNIQUE violée en base (accès concurrents) reste un 409 propre. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurDto> integrite(DataIntegrityViolationException ex) {
        LOG.warn("Contrainte d'intégrité violée : {}", ex.getMostSpecificCause().getMessage());
        return reponse(HttpStatus.CONFLICT, "CONFLIT", "L'opération entre en conflit avec des données existantes.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurDto> inattendue(Exception ex) {
        LOG.error("Erreur inattendue", ex);
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERREUR_INTERNE", "Une erreur interne est survenue.");
    }

    private ResponseEntity<ErreurDto> noteInvalide() {
        return reponse(HttpStatus.BAD_REQUEST, "NOTE_INVALIDE", "La note doit être un entier compris entre 0 et 20.");
    }

    private ResponseEntity<ErreurDto> reponse(HttpStatus statut, String code, String message) {
        return ResponseEntity.status(statut).body(new ErreurDto(code, message));
    }
}
