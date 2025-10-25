/*
 * ApplicationControllerAdvice est une classe centralisée de gestion des exceptions 
 * pour l’ensemble des contrôleurs Spring.
 * Elle intercepte les exceptions et renvoie une réponse structurée (ErrorEntity) 
 * avec un statut HTTP adéquat. 
 * Cette approche permet d’uniformiser le format des erreurs dans toute l’application et 
 * d’améliorer la lisibilité des réponses côté client.
 */

package ld.sa_backend.controller.advice;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import ld.sa_backend.dto.ErrorResponse;
import ld.sa_backend.exception.ExternalApiException;


@ControllerAdvice
public class ApplicationControllerAdvice {

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler({EntityNotFoundException.class})
    public @ResponseBody ErrorResponse handleException(EntityNotFoundException exception) {
        return new ErrorResponse("ENTITY_NOT_FOUND", exception.getMessage());
    }

    @ResponseStatus(CONFLICT)
    @ExceptionHandler({DataIntegrityViolationException.class})
    public @ResponseBody ErrorResponse handleException(DataIntegrityViolationException exception) {
        return new ErrorResponse("CONFLICT_WITH_EXISTING_DATA", exception.getMessage());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class})
    public @ResponseBody ErrorResponse handleException(IllegalArgumentException exception) {
        return new ErrorResponse("ARGUMENTS_INVALID", exception.getMessage());
    }

    @ResponseStatus(BAD_GATEWAY)
    @ExceptionHandler({ExternalApiException.class})
    public @ResponseBody ErrorResponse handleException(ExternalApiException exception) {
        return new ErrorResponse(
            "EXTERNAL_API_ERROR",
            "Erreur lors de l’appel à un service externe : " + exception.getExternalErrorMessage()
        );
    }
}