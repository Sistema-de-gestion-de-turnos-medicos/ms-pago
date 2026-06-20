package com.turnos.mspago.datalizer;

import com.turnos.mspago.model.Pago;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PagoResponse {

    private Long id;
    private Long turnoId;
    private Long pacienteId;
    private String numeroComprobante;
    private BigDecimal monto;
    private String metodoPago;
    private String estado;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String descripcion;
    private String referenciaTransaccion;

    public static PagoResponse fromEntity(Pago pago) {
        return PagoResponse.builder()
                .id(pago.getId())
                .turnoId(pago.getTurnoId())
                .pacienteId(pago.getPacienteId())
                .numeroComprobante(pago.getNumeroComprobante())
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago())
                .estado(pago.getEstado())
                .fechaPago(pago.getFechaPago())
                .fechaCreacion(pago.getFechaCreacion())
                .fechaActualizacion(pago.getFechaActualizacion())
                .descripcion(pago.getDescripcion())
                .referenciaTransaccion(pago.getReferenciaTransaccion())
                .build();
    }
}
