package analizadores.instrucciones;

import analizadores.ast.Arbol;
import analizadores.ast.Instruccion;
import analizadores.data.DatabaseMemory;
import analizadores.data.Tabla;
import analizadores.persistencia.JsonManager;
import analizadores.tablas.TablaSimbolos;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Add extends Instruccion {
    private final String                        nombreTabla;
    private final LinkedHashMap<String, Object> pares;

    public Add(String nombreTabla,
               LinkedHashMap<String, Object> pares,
               int linea, int col) {
        super(linea, col);
        this.nombreTabla = nombreTabla;
        this.pares       = pares;
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

        LinkedHashMap<String, Object> registro = new LinkedHashMap<>();
        for (Map.Entry<String, String> campo : tabla.getEsquema().entrySet()) {
            String fieldName = campo.getKey();
            String tipo      = campo.getValue();
            Object valor     = pares.get(fieldName);

            if (valor == null) {
                valor = defaultValue(tipo);
            } else {
                if (!typeMatch(valor, tipo)) {
                    arbol.error("Semántico",
                        "Tipo incorrecto para campo '" + fieldName
                        + "'. Esperado: " + tipo
                        + ", recibido: " + typeName(valor),
                        linea, columna);
                    return;
                }
            }
            registro.put(fieldName, valor);
        }

        tabla.agregarRegistro(registro);
        arbol.print("Registro insertado en '" + nombreTabla + "'.");

        // guardar automáticamente si la base de datos tiene una ruta definida
        // ya tenemos la variable db arriba
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

    private Object defaultValue(String tipo) {
        return switch (tipo) {
            case "int"    -> 0;
            case "float"  -> 0.0;
            case "bool"   -> false;
            case "string" -> "";
            case "array"  -> new ArrayList<>();
            case "object" -> new LinkedHashMap<>();
            default       -> null;
        };
    }

    private boolean typeMatch(Object val, String tipo) {
        return switch (tipo) {
            case "int"    -> val instanceof Integer;
            case "float"  -> val instanceof Double;
            case "bool"   -> val instanceof Boolean;
            case "string" -> val instanceof String;
            case "array"  -> val instanceof java.util.List;
            case "object" -> val instanceof java.util.Map;
            default       -> true;
        };
    }

    private String typeName(Object val) {
        if (val == null)                   return "null";
        if (val instanceof Integer)        return "int";
        if (val instanceof Double)         return "float";
        if (val instanceof Boolean)        return "bool";
        if (val instanceof String)         return "string";
        if (val instanceof java.util.List) return "array";
        if (val instanceof java.util.Map)  return "object";
        return val.getClass().getSimpleName();
    }
}
