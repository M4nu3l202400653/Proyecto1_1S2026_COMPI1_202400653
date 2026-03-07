package analizadores.expresiones;

import java.util.Map;

public class ExprNot extends Expresion {
    private final Expresion operando;

    public ExprNot(Expresion operando) {
        this.operando = operando;
    }

    @Override
    public Object evaluar(Map<String, Object> registro) {
        Object v = operando.evaluar(registro);
        if (v instanceof Boolean b) return !b;
        return v == null;
    }
}
