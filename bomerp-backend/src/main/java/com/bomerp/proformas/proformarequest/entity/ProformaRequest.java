/*
 * Ruta: bomerp-backend/src/main/java/com/bomerp/proformas/proformarequest/entity/ProformaRequest.java
 * CAMBIO SOLICITADO: aceptar la forma de pago con la que se registra la proforma.
 */
package com.bomerp.proformas.proformarequest.entity;

import com.bomerp.proformas.proforma.entity.Proforma;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ProformaRequest {

    public record CrearProformaDTO(
            @NotNull Long clienteId,
            @NotNull Proforma.Moneda moneda,
            Integer diasVigencia,
            // @Valid en cada ítem: sin él, el @Min(1) de la cantidad nunca se revisaba
            @NotEmpty(message = "La proforma debe tener al menos un producto") List<@Valid ItemDTO> items,
            java.math.BigDecimal tipoCambio,         // opcional: soles por dólar (null = SUNAT del día)
            Proforma.TipoPago tipoPago               // opcional en clientes antiguos; por defecto CONTADO
    ) {
        /** Mantiene compatibles las llamadas antiguas de servicios y pruebas. */
        public CrearProformaDTO(Long clienteId, Proforma.Moneda moneda, Integer diasVigencia,
                                List<ItemDTO> items, java.math.BigDecimal tipoCambio) {
            this(clienteId, moneda, diasVigencia, items, tipoCambio, Proforma.TipoPago.CONTADO);
        }
    }

    public record ItemDTO(
            @NotNull Long productoId,
            @NotNull @Min(value = 1, message = "La cantidad debe ser al menos 1") Integer cantidad,
            java.math.BigDecimal descuentoPorcentaje,   // opcional: % negociado (0 a tope del rol)
            // opcional: precio editado en la moneda de la proforma (null = catálogo); solo sube, con motivo y permiso
            @jakarta.validation.constraints.Positive(message = "El precio debe ser mayor que cero.") java.math.BigDecimal precioUnitario,
            String motivoPrecio
    ) {
        public ItemDTO(Long productoId, Integer cantidad, java.math.BigDecimal descuentoPorcentaje) {
            this(productoId, cantidad, descuentoPorcentaje, null, null);
        }
    }
}

/* Flujo: la nueva proforma envía tipoPago; si una integración antigua no lo incluye, se conserva contado. */
