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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final PacienteClient pacienteClient;
    private final TurnoClient turnoClient;

    @Override
    public PagoResponse crearPago(PagoRequest request) {
        log.info("Creando pago para turno ID: {}, paciente ID: {}",
                request.getTurnoId(), request.getPacienteId());


        if (!MetodoPago.esValido(request.getMetodoPago())) {
            throw new IllegalArgumentException(
                    "Método de pago inválido: " + request.getMetodoPago());
        }


        validarPaciente(request.getPacienteId());


        validarTurno(request.getTurnoId());

        String comprobante = generarNumeroComprobante();

        Pago pago = Pago.builder()
                .turnoId(request.getTurnoId())
                .pacienteId(request.getPacienteId())
                .numeroComprobante(comprobante)
                .monto(request.getMonto())
                .metodoPago(request.getMetodoPago())
                .estado(EstadoPago.PENDIENTE)
                .descripcion(request.getDescripcion())
                .referenciaTransaccion(request.getReferenciaTransaccion())
                .fechaPago(request.getFechaPago() != null ? request.getFechaPago() : LocalDateTime.now())
                .build();

        Pago guardado = pagoRepository.save(pago);
        log.info("Pago creado con ID: {} y comprobante: {}", guardado.getId(), comprobante);
        return PagoResponse.fromEntity(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtenerPagoPorId(Long id) {
        log.debug("Buscando pago con ID: {}", id);
        return PagoResponse.fromEntity(findPagoById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtenerPagoPorComprobante(String numeroComprobante) {
        log.debug("Buscando pago con comprobante: {}", numeroComprobante);
        Pago pago = pagoRepository.findByNumeroComprobante(numeroComprobante)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Pago no encontrado con comprobante: " + numeroComprobante));
        return PagoResponse.fromEntity(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarTodosLosPagos() {
        return pagoRepository.findAll()
                .stream()
                .map(PagoResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPagosPorPaciente(Long pacienteId) {
        return pagoRepository.findByPacienteIdOrderByFechaDesc(pacienteId)
                .stream()
                .map(PagoResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPagosPorTurno(Long turnoId) {
        return pagoRepository.findByTurnoId(turnoId)
                .stream()
                .map(PagoResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPagosPorEstado(String estado) {
        if (!EstadoPago.esValido(estado)) {
            throw new IllegalArgumentException("Estado de pago inválido: " + estado);
        }
        return pagoRepository.findByEstado(estado)
                .stream()
                .map(PagoResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPagosPorMetodo(String metodoPago) {
        if (!MetodoPago.esValido(metodoPago)) {
            throw new IllegalArgumentException("Método de pago inválido: " + metodoPago);
        }
        return pagoRepository.findByMetodoPago(metodoPago)
                .stream()
                .map(PagoResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPagosPorRangoFecha(LocalDateTime desde, LocalDateTime hasta) {
        return pagoRepository.findByFechaPagoBetween(desde, hasta)
                .stream()
                .map(PagoResponse::fromEntity)
                .toList();
    }

    @Override
    public PagoResponse actualizarEstadoPago(Long id, ActualizarEstadoRequest request) {
        log.info("Actualizando estado del pago ID: {} a {}", id, request.getEstado());
        if (!EstadoPago.esValido(request.getEstado())) {
            throw new IllegalArgumentException("Estado de pago inválido: " + request.getEstado());
        }
        Pago pago = findPagoById(id);
        pago.setEstado(request.getEstado());
        return PagoResponse.fromEntity(pagoRepository.save(pago));
    }

    @Override
    public PagoResponse aprobarPago(Long id) {
        log.info("Aprobando pago ID: {}", id);
        Pago pago = findPagoById(id);
        validarTransicion(pago.getEstado(), EstadoPago.APROBADO);
        pago.setEstado(EstadoPago.APROBADO);
        pago.setFechaPago(LocalDateTime.now());
        return PagoResponse.fromEntity(pagoRepository.save(pago));
    }

    @Override
    public PagoResponse rechazarPago(Long id, String motivo) {
        log.info("Rechazando pago ID: {} - Motivo: {}", id, motivo);
        Pago pago = findPagoById(id);
        validarTransicion(pago.getEstado(), EstadoPago.RECHAZADO);
        pago.setEstado(EstadoPago.RECHAZADO);
        pago.setDescripcion(motivo != null ? motivo : pago.getDescripcion());
        return PagoResponse.fromEntity(pagoRepository.save(pago));
    }

    @Override
    public PagoResponse cancelarPago(Long id, String motivo) {
        log.info("Cancelando pago ID: {} - Motivo: {}", id, motivo);
        Pago pago = findPagoById(id);
        validarTransicion(pago.getEstado(), EstadoPago.CANCELADO);
        pago.setEstado(EstadoPago.CANCELADO);
        pago.setDescripcion(motivo != null ? motivo : pago.getDescripcion());
        return PagoResponse.fromEntity(pagoRepository.save(pago));
    }

    @Override
    public PagoResponse reembolsarPago(Long id, String motivo) {
        log.info("Reembolsando pago ID: {} - Motivo: {}", id, motivo);
        Pago pago = findPagoById(id);
        if (!EstadoPago.APROBADO.equals(pago.getEstado())) {
            throw new IllegalStateException("Solo se pueden reembolsar pagos en estado APROBADO");
        }
        pago.setEstado(EstadoPago.REEMBOLSADO);
        pago.setDescripcion(motivo != null ? motivo : pago.getDescripcion());
        return PagoResponse.fromEntity(pagoRepository.save(pago));
    }

    @Override
    public void eliminarPago(Long id) {
        log.warn("Eliminando pago ID: {}", id);
        Pago pago = findPagoById(id);
        pagoRepository.delete(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPorEstado(String estado) {
        if (!EstadoPago.esValido(estado)) {
            throw new IllegalArgumentException("Estado de pago inválido: " + estado);
        }
        BigDecimal total = pagoRepository.sumMontoByEstado(estado);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarPagosPorEstadoYPeriodo(String estado, LocalDateTime desde, LocalDateTime hasta) {
        if (!EstadoPago.esValido(estado)) {
            throw new IllegalArgumentException("Estado de pago inválido: " + estado);
        }
        return pagoRepository.countByEstadoAndFechaPagoBetween(estado, desde, hasta);
    }



    private Pago findPagoById(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con ID: " + id));
    }

    private String generarNumeroComprobante() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PAG-" + fecha + "-" + uuid;
    }

    private void validarTransicion(String estadoActual, String estadoNuevo) {
        if (EstadoPago.CANCELADO.equals(estadoActual) || EstadoPago.REEMBOLSADO.equals(estadoActual)) {
            throw new IllegalStateException(
                    "No se puede cambiar el estado de un pago en estado: " + estadoActual);
        }
        if (EstadoPago.APROBADO.equals(estadoActual) && EstadoPago.RECHAZADO.equals(estadoNuevo)) {
            throw new IllegalStateException("No se puede rechazar un pago ya aprobado");
        }
    }

    private void validarPaciente(Long pacienteId) {
        try {
            PacienteResponse paciente = pacienteClient.obtenerPorId(pacienteId);
            log.debug("Paciente validado: {} {}", paciente.getNombre(), paciente.getApellido());
        } catch (FeignException.NotFound e) {
            log.warn("Paciente no encontrado en ms-pacientes para ID: {}", pacienteId);
            throw new EntityNotFoundException("Paciente no encontrado con ID: " + pacienteId);
        } catch (FeignException e) {
            log.error("Error al comunicarse con ms-pacientes para ID: {}. Status: {}",
                    pacienteId, e.status());
            throw new IllegalStateException("No se pudo verificar el paciente. Intente nuevamente.");
        }
    }

    private void validarTurno(Long turnoId) {
        try {
            TurnoResponse turno = turnoClient.obtenerPorId(turnoId);
            log.debug("Turno validado con ID: {}", turno.getId());
        } catch (FeignException.NotFound e) {
            log.warn("Turno no encontrado en ms-turnos para ID: {}", turnoId);
            throw new EntityNotFoundException("Turno no encontrado con ID: " + turnoId);
        } catch (FeignException e) {
            log.error("Error al comunicarse con ms-turnos para ID: {}. Status: {}",
                    turnoId, e.status());
            throw new IllegalStateException("No se pudo verificar el turno. Intente nuevamente.");
        }
    }
}
