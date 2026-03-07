package analizadores.data;

import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseMemory {
    private final String nombre;
    private String rutaArchivo;
    private final Map<String, Tabla> tablas;

    public DatabaseMemory(String nombre, String rutaArchivo) {
        this.nombre = nombre;
        this.rutaArchivo = (rutaArchivo != null)
                ? rutaArchivo
                : "databases/" + nombre + ".json";
        this.tablas = new LinkedHashMap<>();
    }

    public String getNombre()               { return nombre; }
    public String getRutaArchivo()          { return rutaArchivo; }
    public void   setRutaArchivo(String r)  { rutaArchivo = r; }
    public Map<String, Tabla> getTablas()   { return tablas; }

    public boolean existeTabla(String nombre) {
        return tablas.containsKey(nombre.toLowerCase());
    }

    public void agregarTabla(String nombre, Tabla tabla) {
        tablas.put(nombre.toLowerCase(), tabla);
    }

    public Tabla getTabla(String nombre) {
        return tablas.get(nombre.toLowerCase());
    }
}
