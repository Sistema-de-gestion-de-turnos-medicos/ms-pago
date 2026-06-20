package com.turnos.mspago.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "ms-pacientes", url = "${feign.client.ms-pacientes.url}")
public interface PacienteClient {

    @GetMapping("/api/v1/pacientes/{id}")
    PacienteResponse obtenerPorId(@PathVariable("id") Long id);
}
