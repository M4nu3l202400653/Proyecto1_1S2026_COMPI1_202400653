package analizadores.expresiones;

import java.util.Map;

public class PrimLiteral extends Expresion {
    private final Object valor;

    public PrimLiteral(Object valor) {
        this.valor = valor;
    }

    @Override
    public Object evaluar(Map<String, Object> registro) {
        return valor;
    }
}
