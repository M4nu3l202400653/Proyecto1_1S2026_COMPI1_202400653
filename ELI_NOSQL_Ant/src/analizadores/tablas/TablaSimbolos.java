package analizadores.tablas;

import analizadores.data.DatabaseMemory;
import java.util.LinkedHashMap;
import java.util.Map;

public class TablaSimbolos {
    private final Map<String, DatabaseMemory> databases;
    private DatabaseMemory dbActiva;

    public TablaSimbolos() {
        this.databases = new LinkedHashMap<>();
        this.dbActiva  = null;
    }

    public void addDB(DatabaseMemory db) {
        databases.put(db.getNombre().toLowerCase(), db);
    }

    public DatabaseMemory getDB(String nombre) {
        return databases.get(nombre.toLowerCase());
    }

    public boolean existeDB(String nombre) {
        return databases.containsKey(nombre.toLowerCase());
    }

    public DatabaseMemory getDbActiva()            { return dbActiva; }
    public void           setDbActiva(DatabaseMemory db) { this.dbActiva = db; }
    public Map<String, DatabaseMemory> getDatabases() { return databases; }
}
