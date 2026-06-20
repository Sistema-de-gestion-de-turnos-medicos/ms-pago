package com.turnos.mspago.datalizer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActualizarEstadoRequest {


    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    private String motivo;
}
