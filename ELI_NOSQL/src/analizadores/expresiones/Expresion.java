package analizadores.expresiones;

import java.util.Map;

public abstract class Expresion {
    public abstract Object evaluar(Map<String, Object> registro);
}
