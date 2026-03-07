package analizadores.instrucciones;

import analizadores.ast.Arbol;
import analizadores.ast.Instruccion;
import analizadores.data.DatabaseMemory;
import analizadores.persistencia.JsonManager;
import analizadores.tablas.TablaSimbolos;

public class Database extends Instruccion {
    private final String nombre;
    private final String rutaArchivo;

    public Database(String nombre, String rutaArchivo, int linea, int col) {
        super(linea, col);
        this.nombre      = nombre;
        this.rutaArchivo = rutaArchivo;
    }

    @Override
    public void interpretar(Arbol arbol, TablaSimbolos ts) {
        if (ts.existeDB(nombre)) {
            arbol.error("Semántico",
                "La base de datos '" + nombre + "' ya existe.",
                linea, columna);
            return;
        }

        DatabaseMemory db = new DatabaseMemory(nombre, rutaArchivo);

        if (rutaArchivo != null) {
            java.io.File f = new java.io.File(rutaArchivo);
            if (f.exists()) {
                try {
                    JsonManager.cargar(db, rutaArchivo);
                    arbol.print("Base de datos '" + nombre
                            + "' cargada desde: " + rutaArchivo);
                } catch (Exception e) {
                    arbol.error("Semántico",
                        "No se pudo cargar '" + rutaArchivo + "': "
                        + e.getMessage(), linea, columna);
                }
            }
        }

        ts.addDB(db);
        arbol.print("Base de datos '" + nombre + "' creada.");
    }
}
