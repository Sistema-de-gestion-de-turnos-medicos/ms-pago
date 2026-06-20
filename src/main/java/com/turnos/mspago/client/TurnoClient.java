package com.turnos.mspago.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "ms-turnos", url = "${feign.client.ms-turnos.url}")
public interface TurnoClient {

    @GetMapping("/api/v1/turnos/{id}")
    TurnoResponse obtenerPorId(@PathVariable("id") Long id);
}
