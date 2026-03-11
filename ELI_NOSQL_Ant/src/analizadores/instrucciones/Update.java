package analizadores.instrucciones;

import analizadores.ast.Arbol;
import analizadores.ast.Instruccion;
import analizadores.data.DatabaseMemory;
import analizadores.data.Tabla;
import analizadores.persistencia.JsonManager;
import analizadores.expresiones.Expresion;
import analizadores.tablas.TablaSimbolos;
import java.util.LinkedHashMap;
import java.util.Map;

public class Update extends Instruccion {
    private final String                        nombreTabla;
    private final LinkedHashMap<String, Object> asignaciones;
    private final Expresion                     filtro;

    public Update(String nombreTabla,
                  LinkedHashMap<String, Object> asignaciones,
                  Expresion filtro,
                  int linea, int col) {
        super(linea, col);
        this.nombreTabla  = nombreTabla;
        this.asignaciones = asignaciones;
        this.filtro       = filtro;
    }

    @Override
    public void interpretar(Arbol arbol, TablaSimbolos ts) {
        DatabaseMemory db = ts.getDbActiva();
        if (db == null) {
            arbol.error("Semántico", "No hay base de datos activa.",
                linea, columna);
            return;
        }
        Tabla tabla = db.getTabla(nombreTabla);
        if (tabla == null) {
            arbol.error("Semántico",
                "La tabla '" + nombreTabla + "' no existe.",
                linea, columna);
            return;
        }

        for (String campo : asignaciones.keySet()) {
            if (!tabla.getEsquema().containsKey(campo.toLowerCase())) {
                arbol.error("Semántico",
                    "El campo '" + campo + "' no existe en '"
                    + nombreTabla + "'.", linea, columna);
                return;
            }
        }

        int actualizados = 0;
        for (Map<String, Object> registro : tabla.getRegistros()) {
            boolean aplicar = (filtro == null)
                    || Boolean.TRUE.equals(filtro.evaluar(registro));
            if (aplicar) {
                for (Map.Entry<String, Object> asig : asignaciones.entrySet()) {
                    registro.put(asig.getKey().toLowerCase(), asig.getValue());
                }
                actualizados++;
            }
        }

        arbol.print(actualizados + " registro(s) actualizado(s) en '"
                + nombreTabla + "'.");

        // persistir cambios si hay ruta
        // el objeto "db" ya se obtuvo al principio del método
        if (db != null && db.getRutaArchivo() != null) {
            try {
                JsonManager.guardar(db, db.getRutaArchivo());
                arbol.print("(auto) guardado en " + db.getRutaArchivo());
            } catch (Exception e) {
                arbol.error("Semántico", "No se pudo auto-guardar: " + e.getMessage(),
                            linea, columna);
            }
        }
    }
}
