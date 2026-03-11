# Script para crear ejemplos de prueba

$examplesDir = "test_examples"

if (-not (Test-Path $examplesDir)) {
    New-Item -ItemType Directory -Path $examplesDir | Out-Null
}

# Ejemplo 1: Simple
$simple = @'
## Ejemplo simple

database biblioteca {
  store at "biblioteca.json";
}

use biblioteca;

table libros {
  id: int;
  titulo: string;
  autor: string;
  anio: int;
  disponible: bool;
}

add libros {
  id: 1,
  titulo: "Cien años de soledad",
  autor: "Gabriel García Márquez",
  anio: 1967,
  disponible: true
};

add libros {
  id: 2,
  titulo: "El quijote",
  autor: "Cervantes",
  anio: 1605,
  disponible: false
};

read libros {
  fields: titulo, autor, anio;
  filter: disponible == true;
};

export "libros_disponibles.json";
'@

# Ejemplo 2: Con arrays y objetos
$complejo = @'
## Ejemplo con tipos complejos

database rrhh {
  store at "rrhh.json";
}

use rrhh;

table empleados {
  id: int;
  nombre: string;
  salario: float;
  departamentos: array;
  datos: object;
}

add empleados {
  id: 1,
  nombre: "Juan",
  salario: 2500.50,
  departamentos: ["TI", "Desarrollo"],
  datos: { ciudad: "CDMX", antiguedad: 5, activo: true }
};

add empleados {
  id: 2,
  nombre: "María",
  salario: 3000.00,
  departamentos: ["Recursos Humanos"],
  datos: { ciudad: "Guadalajara", antiguedad: 3, activo: true }
};

read empleados {
  fields: nombre, salario;
  filter: salario > 2800.0;
};

export "empleados_senior.json";
'@

# Ejemplo 3: Operaciones múltiples
$operaciones = @'
## Ejemplo con múltiples operaciones

database ventas {
  store at "ventas.json";
}

use ventas;

table clientes {
  id: int;
  nombre: string;
  ciudad: string;
  activo: bool;
}

add clientes { id: 1, nombre: "Acme Corp", ciudad: "Ciudad A", activo: true };
add clientes { id: 2, nombre: "Beta Inc", ciudad: "Ciudad B", activo: true };
add clientes { id: 3, nombre: "Gamma Ltd", ciudad: "Ciudad A", activo: false };

read clientes {
  fields: nombre, ciudad;
  filter: activo == true && ciudad == "Ciudad A";
};

update clientes {
  set: activo = false;
  filter: id == 2;
};

read clientes {
  fields: *;
};

export "clientes_finales.json";
'@

# Guardar ejemplos
@{
    "simple.code" = $simple
    "complejo.code" = $complejo
    "operaciones.code" = $operaciones
}.GetEnumerator() | ForEach-Object {
    $path = "$examplesDir/$($_.Name)"
    Set-Content -Path $path -Value $_.Value -Encoding UTF8
    Write-Host "✓ Creado: $path" -ForegroundColor Green
}

Write-Host ""
Write-Host "Ejemplos creados en: $examplesDir/" -ForegroundColor Cyan
