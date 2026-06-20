package com.turnos.mspago.client;

import lombok.Data;

@Data
public class TurnoResponse {
    private Long id;
    private Long pacienteId;
    private Long medicoId;
    private String estado;
    private String fechaHora;
}
