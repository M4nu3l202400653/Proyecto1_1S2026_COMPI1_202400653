package analizadores.expresiones;

import java.util.Map;

public class ExprRelacional extends Expresion {
    private final Expresion izq;
    private final String    op;
    private final Expresion der;

    public ExprRelacional(Expresion izq, String op, Expresion der) {
        this.izq = izq;
        this.op  = op;
        this.der = der;
    }

    @Override
    public Object evaluar(Map<String, Object> registro) {
        Object vi = izq.evaluar(registro);
        Object vd = der.evaluar(registro);

        if (vi == null || vd == null) {
            return switch (op) {
                case "==" -> vi == vd;
                case "!=" -> vi != vd;
                default   -> false;
            };
        }

        if (vi instanceof Number ni && vd instanceof Number nd) {
            double l = ni.doubleValue();
            double r = nd.doubleValue();
            return switch (op) {
                case "==" -> l == r;
                case "!=" -> l != r;
                case ">"  -> l > r;
                case "<"  -> l < r;
                case ">=" -> l >= r;
                case "<=" -> l <= r;
                default   -> false;
            };
        }

        return switch (op) {
            case "==" -> vi.equals(vd);
            case "!=" -> !vi.equals(vd);
            default   -> false;
        };
    }
}
