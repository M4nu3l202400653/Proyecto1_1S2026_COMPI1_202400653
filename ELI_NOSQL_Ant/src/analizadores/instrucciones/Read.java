package analizadores.instrucciones;

import analizadores.ast.Arbol;
import analizadores.ast.Instruccion;
import analizadores.data.DatabaseMemory;
import analizadores.data.Tabla;
import analizadores.expresiones.Expresion;
import analizadores.tablas.TablaSimbolos;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Read extends Instruccion {
    private final String       nombreTabla;
    private final List<String> campos;
    private final boolean      todosLosCampos;
    private final Expresion    filtro;

    public Read(String nombreTabla,
                List<String> campos,
                boolean todosLosCampos,
                Expresion filtro,
                int linea, int col) {
        super(linea, col);
        this.nombreTabla    = nombreTabla;
        this.campos         = campos;
        this.todosLosCampos = todosLosCampos;
        this.filtro         = filtro;
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

        List<String> columnas;
        if (todosLosCampos) {
            columnas = new ArrayList<>(tabla.getEsquema().keySet());
        } else {
            columnas = new ArrayList<>();
            for (String c : campos) {
                String cl = c.toLowerCase();
                if (!tabla.getEsquema().containsKey(cl)) {
                    arbol.error("Semántico",
                        "El campo '" + c + "' no existe en '"
                        + nombreTabla + "'.", linea, columna);
                    return;
                }
                columnas.add(cl);
            }
        }

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> registro : tabla.getRegistros()) {
            if (filtro == null || Boolean.TRUE.equals(filtro.evaluar(registro))) {
                resultado.add(registro);
            }
        }

        arbol.setUltimoResultado(resultado, columnas);
        arbol.print(renderTabla(columnas, resultado));
    }

    private String renderTabla(List<String> columnas,
                                List<Map<String, Object>> filas) {
        int[] anchos = new int[columnas.size()];
        for (int i = 0; i < columnas.size(); i++) {
            anchos[i] = columnas.get(i).length();
        }
        for (Map<String, Object> fila : filas) {
            for (int i = 0; i < columnas.size(); i++) {
                Object v = fila.get(columnas.get(i));
                String s = v == null ? "null" : v.toString();
                if (s.length() > anchos[i]) anchos[i] = s.length();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("|");
        for (int i = 0; i < columnas.size(); i++) {
            sb.append(" ").append(pad(columnas.get(i), anchos[i])).append(" |");
        }
        sb.append("\n|");
        for (int ancho : anchos) {
            sb.append("-".repeat(ancho + 2)).append("|");
        }
        sb.append("\n");
        for (Map<String, Object> fila : filas) {
            sb.append("|");
            for (int i = 0; i < columnas.size(); i++) {
                Object v = fila.get(columnas.get(i));
                String s = v == null ? "null" : v.toString();
                sb.append(" ").append(pad(s, anchos[i])).append(" |");
            }
            sb.append("\n");
        }
        if (filas.isEmpty()) {
            sb.append("(sin resultados)\n");
        }
        return sb.toString();
    }

    private String pad(String s, int width) {
        if (s.length() >= width) return s;
        return s + " ".repeat(width - s.length());
    }
}
