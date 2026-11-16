# Filtro de productos por rango de precio

Cambio para el frontend de SGE-JD. Conserva las rutas relativas al repositorio original.

## Archivos incluidos

- `bomerp-frontend/src/app/features/catalogo/producto/producto-list.ts`
- `bomerp-frontend/src/app/features/catalogo/producto/producto-list.html`

## Qué cambia

Agrega los límites opcionales **Precio desde** y **hasta**, inclusivos, en soles y sin IGV. El filtro se combina con la categoría seleccionada. Los precios en dólares se convierten usando el tipo de cambio de venta de hoy. Si un límite es negativo o el mínimo supera al máximo, no se muestran productos.

La lógica solo filtra los productos ya cargados en la pantalla; no altera datos guardados ni requiere cambios en el backend o la base de datos.

## Aplicar al proyecto

Copia `bomerp-frontend/src/app/features/catalogo/producto` en la misma ruta de tu clon del proyecto. Los comentarios en cada archivo identifican la ubicación y explican el flujo del cambio.

La compilación de Angular no se pudo confirmar en el entorno de preparación.
