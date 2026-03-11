package analizadores.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Tabla {
    private final LinkedHashMap<String, String> esquema;
    private final List<Map<String, Object>> registros;

    public Tabla(LinkedHashMap<String, String> esquema) {
        this.esquema = esquema;
        this.registros = new ArrayList<>();
    }

    public LinkedHashMap<String, String> getEsquema()   { return esquema; }
    public List<Map<String, Object>>     getRegistros() { return registros; }

    public void agregarRegistro(Map<String, Object> registro) {
        registros.add(registro);
    }

    public void limpiar() {
        registros.clear();
    }
}
