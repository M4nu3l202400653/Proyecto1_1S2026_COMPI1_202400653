package analizadores.instrucciones;

import analizadores.ast.Arbol;
import analizadores.ast.Instruccion;
import analizadores.data.DatabaseMemory;
import analizadores.data.Tabla;
import analizadores.persistencia.JsonManager;
import analizadores.tablas.TablaSimbolos;

public class Clear extends Instruccion {
    private final String nombreTabla;

    public Clear(String nombreTabla, int linea, int col) {
        super(linea, col);
        this.nombreTabla = nombreTabla;
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
        int eliminados = tabla.getRegistros().size();
        tabla.limpiar();
        arbol.print(eliminados + " registro(s) eliminado(s) de '"
                + nombreTabla + "'.");

        // persistir si es posible
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
