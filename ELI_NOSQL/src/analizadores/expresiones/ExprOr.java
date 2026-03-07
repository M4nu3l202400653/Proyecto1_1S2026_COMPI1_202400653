package analizadores.expresiones;

import java.util.Map;

public class ExprOr extends Expresion {
    private final Expresion izq;
    private final Expresion der;

    public ExprOr(Expresion izq, Expresion der) {
        this.izq = izq;
        this.der = der;
    }

    @Override
    public Object evaluar(Map<String, Object> registro) {
        Object vi = izq.evaluar(registro);
        Object vd = der.evaluar(registro);
        boolean bi = (vi instanceof Boolean b) ? b : vi != null;
        boolean bd = (vd instanceof Boolean b) ? b : vd != null;
        return bi || bd;
    }
}
