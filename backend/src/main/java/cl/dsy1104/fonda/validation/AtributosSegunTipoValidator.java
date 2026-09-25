package cl.dsy1104.fonda.validation;

import cl.dsy1104.fonda.dto.BebidaRequest;
import cl.dsy1104.fonda.model.TipoBebida;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Logica de @AtributosSegunTipo. Solo revisa presencia o ausencia; los rangos
 * (0.5 a 45, mayor o igual a 0) estan como anotaciones en cada campo.
 */
public class AtributosSegunTipoValidator
        implements ConstraintValidator<AtributosSegunTipo, BebidaRequest> {

    @Override
    public boolean isValid(BebidaRequest bebida, ConstraintValidatorContext context) {
        // Sin tipo no hay regla que aplicar; @NotNull en tipo ya reporta ese error.
        if (bebida == null || bebida.tipo() == null) {
            return true;
        }

        // Se reemplaza el mensaje general por uno en cada campo que falla.
        context.disableDefaultConstraintViolation();
        boolean valido = true;

        if (bebida.tipo() == TipoBebida.ALCOHOLICA) {
            if (bebida.gradosAlcohol() == null) {
                valido = error(context, "gradosAlcohol", "es obligatorio en bebidas alcoholicas");
            }
            if (bebida.azucarPorLitro() != null) {
                valido = error(context, "azucarPorLitro", "debe quedar vacio en bebidas alcoholicas");
            }
        } else {
            if (bebida.azucarPorLitro() == null) {
                valido = error(context, "azucarPorLitro", "es obligatorio en bebidas sin alcohol");
            }
            if (bebida.gradosAlcohol() != null) {
                valido = error(context, "gradosAlcohol", "debe quedar vacio en bebidas sin alcohol");
            }
        }
        return valido;
    }

    // addPropertyNode asocia el error al campo: asi llega como "campos.<campo>" en el 400.
    private boolean error(ConstraintValidatorContext context, String campo, String mensaje) {
        context.buildConstraintViolationWithTemplate(mensaje)
                .addPropertyNode(campo)
                .addConstraintViolation();
        return false;
    }
}
