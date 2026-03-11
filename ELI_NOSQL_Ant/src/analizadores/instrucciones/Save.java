package analizadores.instrucciones;

import analizadores.ast.Arbol;
import analizadores.ast.Instruccion;
import analizadores.data.DatabaseMemory;
import analizadores.persistencia.JsonManager;
import analizadores.tablas.TablaSimbolos;

public class Save extends Instruccion {
    private final String rutaArchivo;

    public Save(String rutaArchivo, int linea, int col) {
        super(linea, col);
        this.rutaArchivo = rutaArchivo; // puede ser null para usar la ruta por defecto
    }

    @Override
    public void interpretar(Arbol arbol, TablaSimbolos ts) {
        DatabaseMemory db = ts.getDbActiva();
        if (db == null) {
            arbol.error("Semántico", "No hay base de datos activa.",
                        linea, columna);
            return;
        }

        String ruta = (rutaArchivo != null) ? rutaArchivo : db.getRutaArchivo();
        if (ruta == null) {
            arbol.error("Semántico", "No se ha especificado ruta para guardar la base de datos.",
                        linea, columna);
            return;
        }

        try {
            JsonManager.guardar(db, ruta);
            arbol.print("Base de datos guardada en: " + ruta);
        } catch (Exception e) {
            arbol.error("Semántico",
                        "Error al guardar: " + e.getMessage(),
                        linea, columna);
        }
    }
}
