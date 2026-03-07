package analizadores.instrucciones;

import analizadores.ast.Arbol;
import analizadores.ast.Instruccion;
import analizadores.data.DatabaseMemory;
import analizadores.data.Tabla;
import analizadores.tablas.TablaSimbolos;
import java.util.LinkedHashMap;

public class CreateTable extends Instruccion {
    private final String                        nombre;
    private final LinkedHashMap<String, String> esquema;

    public CreateTable(String nombre,
                       LinkedHashMap<String, String> esquema,
                       int linea, int col) {
        super(linea, col);
        this.nombre  = nombre;
        this.esquema = esquema;
    }

    @Override
    public void interpretar(Arbol arbol, TablaSimbolos ts) {
        DatabaseMemory db = ts.getDbActiva();
        if (db == null) {
            arbol.error("Semántico",
                "No hay base de datos activa. Use 'use <db>;' primero.",
                linea, columna);
            return;
        }
        if (db.existeTabla(nombre)) {
            arbol.error("Semántico",
                "La tabla '" + nombre + "' ya existe en '"
                + db.getNombre() + "'.", linea, columna);
            return;
        }
        Tabla tabla = new Tabla(esquema);
        db.agregarTabla(nombre, tabla);
        arbol.print("Tabla '" + nombre + "' creada en '"
                + db.getNombre() + "'.");
    }
}
