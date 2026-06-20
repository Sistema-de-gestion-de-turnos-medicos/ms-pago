package com.turnos.mspago.service;

import com.turnos.mspago.client.PacienteClient;
import com.turnos.mspago.client.PacienteResponse;
import com.turnos.mspago.client.TurnoClient;
import com.turnos.mspago.client.TurnoResponse;
import com.turnos.mspago.datalizer.ActualizarEstadoRequest;
import com.turnos.mspago.datalizer.PagoRequest;
import com.turnos.mspago.datalizer.PagoResponse;
import com.turnos.mspago.model.EstadoPago;
import com.turnos.mspago.model.MetodoPago;
import com.turnos.mspago.model.Pago;
import com.turnos.mspago.repository.PagoRepository;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PacienteClient pacienteClient;

    @Mock
    private TurnoClient turnoClient;

    @InjectMocks
    private PagoServiceImpl pagoService;

    private Pago pago;
    private PagoRequest pagoRequest;
    private PacienteResponse pacienteResponse;
    private TurnoResponse turnoResponse;

    @BeforeEach
    void setUp() {
        pago = Pago.builder()
                .id(1L)
                .turnoId(10L)
                .pacienteId(20L)
                .numeroComprobante("PAG-20260620-ABCD1234")
                .monto(new BigDecimal("15000.00"))
                .metodoPago(MetodoPago.EFECTIVO)
                .estado(EstadoPago.PENDIENTE)
                .fechaPago(LocalDateTime.now())
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .descripcion("Consulta general")
                .build();

        pagoRequest = new PagoRequest();
        pagoRequest.setTurnoId(10L);
        pagoRequest.setPacienteId(20L);
        pagoRequest.setMonto(new BigDecimal("15000.00"));
        pagoRequest.setMetodoPago(MetodoPago.EFECTIVO);
        pagoRequest.setDescripcion("Consulta general");

        pacienteResponse = new PacienteResponse();
        pacienteResponse.setId(20L);
        pacienteResponse.setNombre("Juan");
        pacienteResponse.setApellido("Pérez");

        turnoResponse = new TurnoResponse();
        turnoResponse.setId(10L);
        turnoResponse.setPacienteId(20L);
        turnoResponse.setEstado("CONFIRMADO");
    }



    @Test
    void testCrearPago_exitoso() {

        when(pacienteClient.obtenerPorId(20L)).thenReturn(pacienteResponse);
        when(turnoClient.obtenerPorId(10L)).thenReturn(turnoResponse);
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);


        PagoResponse resultado = pagoService.crearPago(pagoRequest);


        assertNotNull(resultado);
        assertEquals(EstadoPago.PENDIENTE, resultado.getEstado());
        assertEquals(new BigDecimal("15000.00"), resultado.getMonto());
        verify(pagoRepository, times(1)).save(any(Pago.class));
    }

    @Test
    void testCrearPago_metodoPagoInvalido_lanzaExcepcion() {
        pagoRequest.setMetodoPago("BITCOIN");


        assertThrows(IllegalArgumentException.class, () -> pagoService.crearPago(pagoRequest));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void testCrearPago_pacienteNoExiste_lanzaEntityNotFoundException() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/v1/pacientes/20",
                java.util.Collections.emptyMap(), null, new RequestTemplate());
        when(pacienteClient.obtenerPorId(20L))
                .thenThrow(new FeignException.NotFound("Not Found", request, null, null));


        assertThrows(EntityNotFoundException.class, () -> pagoService.crearPago(pagoRequest));
        verify(pagoRepository, never()).save(any(Pago.class));
    }



    @Test
    void testObtenerPagoPorId_exitoso() {
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));

        PagoResponse resultado = pagoService.obtenerPagoPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("PAG-20260620-ABCD1234", resultado.getNumeroComprobante());
    }

    @Test
    void testObtenerPagoPorId_noExiste_lanzaExcepcion() {
        when(pagoRepository.findById(99L)).thenReturn(Optional.empty());

        // Verifica que se lance EntityNotFoundException cuando el pago no existe.
        assertThrows(EntityNotFoundException.class, () -> pagoService.obtenerPagoPorId(99L));
    }



    @Test
    void testListarTodosLosPagos() {
        when(pagoRepository.findAll()).thenReturn(List.of(pago));

        List<PagoResponse> pagos = pagoService.listarTodosLosPagos();


        assertNotNull(pagos);
        assertEquals(1, pagos.size());
    }


    @Test
    void testListarPagosPorEstado_valido() {
        when(pagoRepository.findByEstado(EstadoPago.PENDIENTE)).thenReturn(List.of(pago));

        List<PagoResponse> pagos = pagoService.listarPagosPorEstado(EstadoPago.PENDIENTE);

        assertNotNull(pagos);
        assertEquals(1, pagos.size());
        assertEquals(EstadoPago.PENDIENTE, pagos.get(0).getEstado());
    }

    @Test
    void testListarPagosPorEstado_invalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> pagoService.listarPagosPorEstado("ESTADO_INEXISTENTE"));
    }



    @Test
    void testActualizarEstadoPago_exitoso() {
        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setEstado(EstadoPago.APROBADO);

        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);

        PagoResponse resultado = pagoService.actualizarEstadoPago(1L, request);

        assertNotNull(resultado);
        verify(pagoRepository, times(1)).save(any(Pago.class));
    }

    @Test
    void testActualizarEstadoPago_estadoInvalido_lanzaExcepcion() {
        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setEstado("NO_EXISTE");

        assertThrows(IllegalArgumentException.class,
                () -> pagoService.actualizarEstadoPago(1L, request));
        verify(pagoRepository, never()).findById(any());
    }



    @Test
    void testAprobarPago_exitoso() {
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PagoResponse resultado = pagoService.aprobarPago(1L);

        assertNotNull(resultado);
        assertEquals(EstadoPago.APROBADO, resultado.getEstado());
    }

    @Test
    void testAprobarPago_pagoCancelado_lanzaIllegalStateException() {
        pago.setEstado(EstadoPago.CANCELADO);
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));

        // Verifica que no se pueda aprobar un pago cancelado (transición de estado inválida).
        assertThrows(IllegalStateException.class, () -> pagoService.aprobarPago(1L));
        verify(pagoRepository, never()).save(any(Pago.class));
    }



    @Test
    void testReembolsarPago_pagoNoAprobado_lanzaIllegalStateException() {
        pago.setEstado(EstadoPago.PENDIENTE);
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));

        assertThrows(IllegalStateException.class, () -> pagoService.reembolsarPago(1L, "Solicitud del paciente"));
    }

    @Test
    void testReembolsarPago_exitoso() {
        pago.setEstado(EstadoPago.APROBADO);
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PagoResponse resultado = pagoService.reembolsarPago(1L, "Solicitud del paciente");

        assertNotNull(resultado);
        assertEquals(EstadoPago.REEMBOLSADO, resultado.getEstado());
    }



    @Test
    void testEliminarPago_exitoso() {
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));

        pagoService.eliminarPago(1L);

        verify(pagoRepository, times(1)).delete(pago);
    }



    @Test
    void testCalcularTotalPorEstado_conValores() {
        when(pagoRepository.sumMontoByEstado(EstadoPago.APROBADO)).thenReturn(new BigDecimal("50000.00"));

        BigDecimal total = pagoService.calcularTotalPorEstado(EstadoPago.APROBADO);

        assertNotNull(total);
        assertEquals(new BigDecimal("50000.00"), total);
    }

    @Test
    void testCalcularTotalPorEstado_sinPagos_devuelveCero() {
        when(pagoRepository.sumMontoByEstado(EstadoPago.APROBADO)).thenReturn(null);

        BigDecimal total = pagoService.calcularTotalPorEstado(EstadoPago.APROBADO);


        assertNotNull(total);
        assertEquals(BigDecimal.ZERO, total);
    }
}
