# INICIO RÁPIDO - ELI NOSQL

## ⚡ 5 Pasos para empezar en 1 minuto

### Paso 1: Abre PowerShell en este directorio

### Paso 2: Ejecuta el menú principal
```powershell
.\menu_pruebas.ps1
```

### Paso 3: Selecciona opción (recomendado: 1 → 3 → 4)

### Paso 4: ¡Listo! 🎉

---

## 🎯 Opciones Rápidas

| Comando | Qué hace |
|---------|----------|
| `.\setup_test_env.ps1` | Compila todo |
| `.\create_examples.ps1` | Crea ejemplos en `test_examples/` |
| `.\run_examples.ps1` | Ejecuta ejemplos y genera JSONs |
| `.\run_app.ps1` o `ant run` | Abre la interfaz gráfica |
| `compilar.bat` | Compilación manual (Windows) |

---

## 📝 Tu primer programa

Crea un archivo `hola.code`:

```eli
database tienda {
  store at "tienda.json";
}

use tienda;

table productos {
  id: int;
  nombre: string;
  precio: float;
}

add productos { id: 1, nombre: "Laptop", precio: 1500.00 };

read productos {
  fields: nombre, precio;
};

export "resultado.json";
```

Luego en la GUI:
1. Abre `hola.code` (Ctrl+O)
2. Ejecuta (F5)
3. Guarda BD (Ctrl+B)

✓ Se genera `tienda.json`

---

## 🔧 Si algo falla

### Error: "No se reconoce ..."
```powershell
ant clean lexer parser compile
```

### Error: "No hay base de datos activa"
Asegúrate de:
1. `database nombre { ... }`
2. `use nombre;` ← **importante**

### GUI no abre
```powershell
ant jar
java -cp "build\classes;lib\*" analizadores.Analizadores
```

---

## 📚 Comandos disponibles

| Comando | Ejemplo |
|---------|---------|
| database | `database miDB { store at "db.json"; }` |
| use | `use miDB;` |
| table | `table usuarios { id: int; nombre: string; }` |
| add | `add usuarios { id: 1, nombre: "Juan" };` |
| read | `read usuarios { fields: *; filter: id > 0; };` |
| update | `update usuarios { set: nombre = "Pedro"; filter: id == 1; };` |
| clear | `clear usuarios;` |
| export | `export "resultado.json";` |

---

## ✅ Ejemplos incluidos

- **dificil.code** - Completo (E-commerce) → ✓ Genera JSON
- **medio.code** - Intermedio (Colegio) → ✓ Genera JSON
- **prueba.code** - Con errores → ✗ (falta `use`)

---

**¿Necesitas más ayuda?** Abre `README.md`
