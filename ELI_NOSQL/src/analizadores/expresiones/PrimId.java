package analizadores.expresiones;

import java.util.Map;

public class PrimId extends Expresion {
    private final String campo;

    public PrimId(String campo) {
        this.campo = campo.toLowerCase();
    }

    @Override
    public Object evaluar(Map<String, Object> registro) {
        return registro.get(campo);
    }
}
