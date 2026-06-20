package com.turnos.mspago.client;

import lombok.Data;

@Data
public class PacienteResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
}
