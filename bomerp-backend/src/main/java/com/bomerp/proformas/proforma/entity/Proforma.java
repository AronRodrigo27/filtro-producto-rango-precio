/*
 * Ruta: bomerp-backend/src/main/java/com/bomerp/proformas/proforma/entity/Proforma.java
 * CAMBIO SOLICITADO: guardar la forma de pago elegida al crear una proforma.
 */
package com.bomerp.proformas.proforma.entity;
import com.bomerp.proformas.itemproforma.entity.ItemProforma;
import com.bomerp.compras.compra.entity.Compra;
import com.bomerp.ventas.detalleventa.entity.DetalleVenta;
import com.bomerp.compras.detallecompra.entity.DetalleCompra;
import com.bomerp.ventas.venta.entity.Venta;

import com.bomerp.clientes.general.integration.Cliente;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera de una proforma. El detalle (productos/servicios cotizados)
 * vive en ItemProforma — mismo patrón cabecera-detalle que Venta/DetalleVenta
 * y Compra/DetalleCompra, tal como pidió el equipo docente.
 */
@Entity
@Table(name = "PROFORMAS")
@Getter
@Setter
@NoArgsConstructor
public class Proforma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    @NotNull
    private Cliente cliente;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision = LocalDate.now();

    /** RNF confirmado con Lisandro: una proforma vence si no hay respuesta */
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoProforma estado = EstadoProforma.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Moneda moneda;

    /** Forma de pago que se propone al convertir la proforma en venta (retrocompatible: CONTADO). */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", length = 10)
    private TipoPago tipoPago = TipoPago.CONTADO;

    /**
     * Tipo de cambio (soles por dólar) con que se convirtieron los precios. Se respeta al
     * convertir la proforma en venta: el cliente paga lo cotizado aunque el cambio varíe.
     */
    @Column(name = "tipo_cambio", precision = 10, scale = 4)
    private BigDecimal tipoCambio;

    @OneToMany(mappedBy = "proforma", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemProforma> items = new ArrayList<>();

    /** Canal por el que se envió: WHATSAPP, CORREO, o ambos (se registran 2 filas si aplica) */
    @Column(name = "enviado_whatsapp")
    private boolean enviadoWhatsApp;

    @Column(name = "enviado_correo")
    private boolean enviadoCorreo;

    /** Venta generada al convertirla (solo si estado = CONVERTIDA). */
    @Column(name = "venta_id")
    private Long ventaId;

    public enum EstadoProforma {
        PENDIENTE, ACEPTADA, RECHAZADA, VENCIDA, CONVERTIDA
    }

    public enum Moneda {
        SOLES, DOLARES
    }

    public enum TipoPago {
        CONTADO, CREDITO
    }

    /** Las proformas antiguas sin esta columna o sin valor se tratan como contado. */
    public TipoPago getTipoPago() {
        return tipoPago != null ? tipoPago : TipoPago.CONTADO;
    }

    /** Suma de todos los items, antes de IGV */
    public BigDecimal calcularSubtotal() {
        return items.stream()
                .map(ItemProforma::calcularSubtotalItem)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private static final BigDecimal IGV = new BigDecimal("0.18");

    public BigDecimal calcularIgv() {
        return calcularSubtotal().multiply(IGV).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotal() {
        return calcularSubtotal().add(calcularIgv()).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public void agregarItem(ItemProforma item) {
        items.add(item);
        item.setProforma(this);
    }
}

/* Flujo: el campo tipo_pago guarda la preferencia seleccionada en la proforma. La lista la muestra y la
   usa como selección inicial al convertir; el pago efectivo de la venta sigue confirmándose al convertir. */
