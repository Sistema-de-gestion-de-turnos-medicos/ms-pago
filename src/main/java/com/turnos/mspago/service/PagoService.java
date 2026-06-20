package com.turnos.mspago.service;

import com.turnos.mspago.datalizer.ActualizarEstadoRequest;
import com.turnos.mspago.datalizer.PagoRequest;
import com.turnos.mspago.datalizer.PagoResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PagoService {

    PagoResponse crearPago(PagoRequest request);

    PagoResponse obtenerPagoPorId(Long id);

    PagoResponse obtenerPagoPorComprobante(String numeroComprobante);

    List<PagoResponse> listarTodosLosPagos();

    List<PagoResponse> listarPagosPorPaciente(Long pacienteId);

    List<PagoResponse> listarPagosPorTurno(Long turnoId);

    // estado y metodoPago ahora son String
    List<PagoResponse> listarPagosPorEstado(String estado);

    List<PagoResponse> listarPagosPorMetodo(String metodoPago);

    List<PagoResponse> listarPagosPorRangoFecha(LocalDateTime desde, LocalDateTime hasta);

    PagoResponse actualizarEstadoPago(Long id, ActualizarEstadoRequest request);

    PagoResponse aprobarPago(Long id);

    PagoResponse rechazarPago(Long id, String motivo);

    PagoResponse cancelarPago(Long id, String motivo);

    PagoResponse reembolsarPago(Long id, String motivo);

    void eliminarPago(Long id);

    BigDecimal calcularTotalPorEstado(String estado);

    Long contarPagosPorEstadoYPeriodo(String estado, LocalDateTime desde, LocalDateTime hasta);
}
