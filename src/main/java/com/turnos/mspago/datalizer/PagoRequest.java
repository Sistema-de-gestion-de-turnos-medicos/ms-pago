package com.turnos.mspago.datalizer;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PagoRequest {

    @NotNull(message = "El ID del turno es obligatorio")
    private Long turnoId;

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long pacienteId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a 0")
    private BigDecimal monto;



    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String descripcion;

    @Size(max = 100, message = "La referencia no puede superar 100 caracteres")
    private String referenciaTransaccion;

    private LocalDateTime fechaPago;
}
