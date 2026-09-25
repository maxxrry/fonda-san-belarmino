package cl.dsy1104.fonda.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restriccion propia de Bean Validation, a nivel de clase: revisa que
 * gradosAlcohol y azucarPorLitro vengan o no segun el tipo de la bebida.
 * Va sobre la clase porque necesita ver varios campos a la vez.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtributosSegunTipoValidator.class)
public @interface AtributosSegunTipo {

    // Los tres atributos que exige Bean Validation en toda restriccion.
    String message() default "los atributos no corresponden al tipo";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
