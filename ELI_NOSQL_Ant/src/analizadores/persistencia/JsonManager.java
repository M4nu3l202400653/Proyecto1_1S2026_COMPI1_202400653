package analizadores.persistencia;

import analizadores.data.DatabaseMemory;
import analizadores.data.Tabla;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonManager {

    // ===== GUARDAR: DatabaseMemory -> archivo JSON =====
    public static void guardar(DatabaseMemory db, String ruta) throws IOException {
        String json = serializarDB(db);
        Path path = Paths.get(ruta);
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    // ===== CARGAR: archivo JSON -> poblar DatabaseMemory =====
    public static void cargar(DatabaseMemory db, String ruta) throws IOException {
        String json = Files.readString(Paths.get(ruta), StandardCharsets.UTF_8);
        deserializarDB(db, json);
    }

    // ===== EXPORTAR: último resultado read -> archivo JSON =====
    public static void exportarResultado(List<Map<String, Object>> registros,
                                         List<String> campos,
                                         String ruta) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < registros.size(); i++) {
            sb.append("  ").append(serializarRegistro(registros.get(i), campos));
            if (i < registros.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");
        Path path = Paths.get(ruta);
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }

    // ===== SERIALIZACIÓN =====

    private static String serializarDB(DatabaseMemory db) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"nombre\": ").append(jsonStr(db.getNombre())).append(",\n");
        sb.append("  \"ruta\": ").append(jsonStr(db.getRutaArchivo())).append(",\n");
        sb.append("  \"tablas\": {\n");
        var tablas = db.getTablas();
        int ti = 0;
        for (Map.Entry<String, Tabla> entry : tablas.entrySet()) {
            sb.append("    ").append(jsonStr(entry.getKey())).append(": ");
            sb.append(serializarTabla(entry.getValue()));
            if (ti < tablas.size() - 1) sb.append(",");
            sb.append("\n");
            ti++;
        }
        sb.append("  }\n");
        sb.append("}");
        return sb.toString();
    }

    private static String serializarTabla(Tabla tabla) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("      \"esquema\": {");
        var esq = tabla.getEsquema();
        int ei = 0;
        for (Map.Entry<String, String> e : esq.entrySet()) {
            sb.append(jsonStr(e.getKey())).append(": ").append(jsonStr(e.getValue()));
            if (ei < esq.size() - 1) sb.append(", ");
            ei++;
        }
        sb.append("},\n");
        sb.append("      \"registros\": [\n");
        var regs = tabla.getRegistros();
        for (int i = 0; i < regs.size(); i++) {
            sb.append("        ").append(serializarRegistro(
                    regs.get(i), new ArrayList<>(esq.keySet())));
            if (i < regs.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("      ]\n");
        sb.append("    }");
        return sb.toString();
    }

    private static String serializarRegistro(Map<String, Object> reg,
                                              List<String> campos) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < campos.size(); i++) {
            String campo = campos.get(i);
            Object val   = reg.get(campo);
            sb.append(jsonStr(campo)).append(": ").append(jsonVal(val));
            if (i < campos.size() - 1) sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    static String jsonVal(Object val) {
        if (val == null)             return "null";
        if (val instanceof Boolean)  return val.toString();
        if (val instanceof Integer)  return val.toString();
        if (val instanceof Double)   return val.toString();
        if (val instanceof String s) return jsonStr(s);
        if (val instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(jsonVal(list.get(i)));
                if (i < list.size() - 1) sb.append(", ");
            }
            return sb.append("]").toString();
        }
        if (val instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            var entries = map.entrySet().toArray();
            for (int i = 0; i < entries.length; i++) {
                var e = (Map.Entry<?, ?>) entries[i];
                sb.append(jsonStr(e.getKey().toString()))
                  .append(": ").append(jsonVal(e.getValue()));
                if (i < entries.length - 1) sb.append(", ");
            }
            return sb.append("}").toString();
        }
        return jsonStr(val.toString());
    }

    static String jsonStr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r")
                       .replace("\t", "\\t")
               + "\"";
    }

    // ===== DESERIALIZACIÓN (parser recursivo manual) =====

    private static int pos;

    public static synchronized void deserializarDB(DatabaseMemory db,
                                                    String json) {
        pos = 0;
        char[] ch = json.toCharArray();
        skipWS(ch);
        expectChar(ch, '{');
        while (pos < ch.length && ch[pos] != '}') {
            skipWS(ch);
            if (ch[pos] == '}') break;
            String key = parseString(ch);
            skipWS(ch);
            expectChar(ch, ':');
            skipWS(ch);
            switch (key) {
                case "nombre", "ruta" -> parseString(ch);
                case "tablas"         -> parseTablas(db, ch);
                default               -> skipValue(ch);
            }
            skipWS(ch);
            if (pos < ch.length && ch[pos] == ',') pos++;
            skipWS(ch);
        }
        if (pos < ch.length) pos++;
    }

    private static void parseTablas(DatabaseMemory db, char[] ch) {
        expectChar(ch, '{');
        skipWS(ch);
        while (pos < ch.length && ch[pos] != '}') {
            String nombreTabla = parseString(ch);
            skipWS(ch);
            expectChar(ch, ':');
            skipWS(ch);
            Tabla tabla = parseTabla(ch);
            db.agregarTabla(nombreTabla, tabla);
            skipWS(ch);
            if (pos < ch.length && ch[pos] == ',') pos++;
            skipWS(ch);
        }
        if (pos < ch.length) pos++;
    }

    private static Tabla parseTabla(char[] ch) {
        expectChar(ch, '{');
        LinkedHashMap<String, String> esquema = new LinkedHashMap<>();
        List<Map<String, Object>>     registros = new ArrayList<>();
        skipWS(ch);
        while (pos < ch.length && ch[pos] != '}') {
            String key = parseString(ch);
            skipWS(ch);
            expectChar(ch, ':');
            skipWS(ch);
            switch (key) {
                case "esquema"   -> esquema   = parseEsquema(ch);
                case "registros" -> registros = parseRegistros(ch);
                default          -> skipValue(ch);
            }
            skipWS(ch);
            if (pos < ch.length && ch[pos] == ',') pos++;
            skipWS(ch);
        }
        if (pos < ch.length) pos++;
        Tabla t = new Tabla(esquema);
        for (var r : registros) t.agregarRegistro(r);
        return t;
    }

    private static LinkedHashMap<String, String> parseEsquema(char[] ch) {
        LinkedHashMap<String, String> esq = new LinkedHashMap<>();
        expectChar(ch, '{');
        skipWS(ch);
        while (pos < ch.length && ch[pos] != '}') {
            String field = parseString(ch);
            skipWS(ch);
            expectChar(ch, ':');
            skipWS(ch);
            String tipo = parseString(ch);
            esq.put(field, tipo);
            skipWS(ch);
            if (pos < ch.length && ch[pos] == ',') pos++;
            skipWS(ch);
        }
        if (pos < ch.length) pos++;
        return esq;
    }

    private static List<Map<String, Object>> parseRegistros(char[] ch) {
        List<Map<String, Object>> list = new ArrayList<>();
        expectChar(ch, '[');
        skipWS(ch);
        while (pos < ch.length && ch[pos] != ']') {
            list.add(parseObject(ch));
            skipWS(ch);
            if (pos < ch.length && ch[pos] == ',') pos++;
            skipWS(ch);
        }
        if (pos < ch.length) pos++;
        return list;
    }

    private static Map<String, Object> parseObject(char[] ch) {
        Map<String, Object> map = new LinkedHashMap<>();
        expectChar(ch, '{');
        skipWS(ch);
        while (pos < ch.length && ch[pos] != '}') {
            String key = parseString(ch);
            skipWS(ch);
            expectChar(ch, ':');
            skipWS(ch);
            Object val = parseValue(ch);
            map.put(key, val);
            skipWS(ch);
            if (pos < ch.length && ch[pos] == ',') pos++;
            skipWS(ch);
        }
        if (pos < ch.length) pos++;
        return map;
    }

    private static Object parseValue(char[] ch) {
        skipWS(ch);
        if (pos >= ch.length) return null;
        char c = ch[pos];
        if (c == '"') return parseString(ch);
        if (c == '[') return parseArray(ch);
        if (c == '{') return parseObject(ch);
        if (c == 'n' && pos + 3 < ch.length) { pos += 4; return null; }
        if (c == 't' && pos + 3 < ch.length) { pos += 4; return true; }
        if (c == 'f' && pos + 4 < ch.length) { pos += 5; return false; }
        return parseNumber(ch);
    }

    private static List<Object> parseArray(char[] ch) {
        List<Object> list = new ArrayList<>();
        expectChar(ch, '[');
        skipWS(ch);
        while (pos < ch.length && ch[pos] != ']') {
            list.add(parseValue(ch));
            skipWS(ch);
            if (pos < ch.length && ch[pos] == ',') pos++;
            skipWS(ch);
        }
        if (pos < ch.length) pos++;
        return list;
    }

    private static Number parseNumber(char[] ch) {
        StringBuilder sb = new StringBuilder();
        while (pos < ch.length
               && (Character.isDigit(ch[pos]) || ch[pos] == '-'
                   || ch[pos] == '.' || ch[pos] == 'e'
                   || ch[pos] == 'E' || ch[pos] == '+')) {
            sb.append(ch[pos++]);
        }
        String s = sb.toString();
        try {
            if (s.contains(".") || s.contains("e") || s.contains("E")) {
                return Double.parseDouble(s);
            }
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String parseString(char[] ch) {
        expectChar(ch, '"');
        StringBuilder sb = new StringBuilder();
        while (pos < ch.length && ch[pos] != '"') {
            if (ch[pos] == '\\') {
                pos++;
                if (pos < ch.length) {
                    sb.append(switch (ch[pos]) {
                        case '"'  -> '"';
                        case '\\' -> '\\';
                        case 'n'  -> '\n';
                        case 'r'  -> '\r';
                        case 't'  -> '\t';
                        default   -> ch[pos];
                    });
                }
            } else {
                sb.append(ch[pos]);
            }
            pos++;
        }
        if (pos < ch.length) pos++;
        return sb.toString();
    }

    private static void skipValue(char[] ch) { parseValue(ch); }

    private static void skipWS(char[] ch) {
        while (pos < ch.length && Character.isWhitespace(ch[pos])) pos++;
    }

    private static void expectChar(char[] ch, char expected) {
        skipWS(ch);
        if (pos < ch.length && ch[pos] == expected) pos++;
    }
}
