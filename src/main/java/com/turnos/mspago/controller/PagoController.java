package com.turnos.mspago.controller;

import com.turnos.mspago.datalizer.ActualizarEstadoRequest;
import com.turnos.mspago.datalizer.ApiResponse;
import com.turnos.mspago.datalizer.PagoRequest;
import com.turnos.mspago.datalizer.PagoResponse;
import com.turnos.mspago.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST encargado de la gestión de pagos del sistema de turnos médicos.
 * <p>
 * Expone operaciones de creación, consulta, actualización de estado y
 * estadísticas sobre los pagos registrados, asociados a turnos y pacientes.
 */
@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Pagos",
        description = "Operaciones para la gestión de pagos asociados a turnos médicos: " +
                "creación, consulta, cambios de estado (aprobación, rechazo, cancelación, " +
                "reembolso) y estadísticas."
)
public class PagoController {

    private final PagoService pagoService;

    @Operation(
            summary = "Registrar un nuevo pago",
            description = "Crea un nuevo pago asociado a un turno y un paciente. " +
                    "Genera automáticamente un número de comprobante y queda en estado PENDIENTE."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Pago creado exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "PagoCreado",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Pago creado exitosamente",
                                              "data": {
                                                "id": 1,
                                                "turnoId": 10,
                                                "pacienteId": 5,
                                                "numeroComprobante": "CMP-2026-0001",
                                                "monto": 25000.00,
                                                "metodoPago": "TARJETA_CREDITO",
                                                "estado": "PENDIENTE",
                                                "fechaPago": "2026-06-17T10:30:00",
                                                "fechaCreacion": "2026-06-17T10:30:00",
                                                "fechaActualizacion": "2026-06-17T10:30:00",
                                                "descripcion": "Consulta cardiología",
                                                "referenciaTransaccion": "TXN-998877"
                                              },
                                              "timestamp": "2026-06-17T10:30:00"
                                            }"""
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Datos de la solicitud inválidos (validación fallida)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "ErrorValidacion",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "El monto debe ser mayor a 0",
                                              "data": null,
                                              "timestamp": "2026-06-17T10:30:00"
                                            }"""
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene el rol requerido (ADMIN o RECEPCIONISTA)"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<PagoResponse>> crearPago(
            @RequestBody(
                    description = "Datos necesarios para registrar un nuevo pago",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PagoRequest.class),
                            examples = @ExampleObject(
                                    name = "NuevoPago",
                                    value = """
                                            {
                                              "turnoId": 10,
                                              "pacienteId": 5,
                                              "monto": 25000.00,
                                              "metodoPago": "TARJETA_CREDITO",
                                              "descripcion": "Consulta cardiología",
                                              "referenciaTransaccion": "TXN-998877",
                                              "fechaPago": "2026-06-17T10:30:00"
                                            }"""
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody PagoRequest request) {
        log.info("POST /api/v1/pagos - Crear nuevo pago");
        PagoResponse response = pagoService.crearPago(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Pago creado exitosamente", response));
    }

    @Operation(
            summary = "Listar todos los pagos",
            description = "Obtiene el listado completo de pagos registrados en el sistema."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Listado de pagos obtenido exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PagoResponse.class))
                    )
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'MEDICO')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarTodos() {
        log.info("GET /api/v1/pagos - Listar todos los pagos");
        List<PagoResponse> pagos = pagoService.listarTodosLosPagos();
        return ResponseEntity.ok(ApiResponse.ok("Pagos obtenidos exitosamente", pagos));
    }

    @Operation(
            summary = "Obtener un pago por su ID",
            description = "Busca y retorna un pago específico a partir de su identificador único."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pago encontrado",
                    content = @Content(schema = @Schema(implementation = PagoResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No existe un pago con el ID indicado"
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'MEDICO')")
    public ResponseEntity<ApiResponse<PagoResponse>> obtenerPorId(
            @Parameter(description = "Identificador único del pago", example = "1", required = true)
            @PathVariable Long id) {
        log.info("GET /api/v1/pagos/{}", id);
        PagoResponse response = pagoService.obtenerPagoPorId(id);
        return ResponseEntity.ok(ApiResponse.ok("Pago encontrado", response));
    }

    @Operation(
            summary = "Obtener un pago por número de comprobante",
            description = "Busca un pago a partir de su número de comprobante único. " +
                    "Este endpoint es de acceso público."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pago encontrado",
                    content = @Content(schema = @Schema(implementation = PagoResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No existe un pago con ese número de comprobante"
            )
    })
    @GetMapping("/comprobante/{numeroComprobante}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'MEDICO', 'PACIENTE')")
    public ResponseEntity<ApiResponse<PagoResponse>> obtenerPorComprobante(
            @Parameter(description = "Número de comprobante del pago", example = "CMP-2026-0001", required = true)
            @PathVariable String numeroComprobante) {
        log.info("GET /api/v1/pagos/comprobante/{}", numeroComprobante);
        PagoResponse response = pagoService.obtenerPagoPorComprobante(numeroComprobante);
        return ResponseEntity.ok(ApiResponse.ok("Pago encontrado", response));
    }

    @Operation(
            summary = "Listar pagos de un paciente",
            description = "Obtiene todos los pagos asociados a un paciente específico."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pagos del paciente obtenidos",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagoResponse.class)))
            )
    })
    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'MEDICO', 'PACIENTE')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarPorPaciente(
            @Parameter(description = "Identificador del paciente", example = "5", required = true)
            @PathVariable Long pacienteId) {
        log.info("GET /api/v1/pagos/paciente/{}", pacienteId);
        List<PagoResponse> pagos = pagoService.listarPagosPorPaciente(pacienteId);
        return ResponseEntity.ok(ApiResponse.ok("Pagos del paciente obtenidos", pagos));
    }

    @Operation(
            summary = "Listar pagos de un turno",
            description = "Obtiene todos los pagos asociados a un turno médico específico."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pagos del turno obtenidos",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagoResponse.class)))
            )
    })
    @GetMapping("/turno/{turnoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'MEDICO')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarPorTurno(
            @Parameter(description = "Identificador del turno médico", example = "10", required = true)
            @PathVariable Long turnoId) {
        log.info("GET /api/v1/pagos/turno/{}", turnoId);
        List<PagoResponse> pagos = pagoService.listarPagosPorTurno(turnoId);
        return ResponseEntity.ok(ApiResponse.ok("Pagos del turno obtenidos", pagos));
    }

    @Operation(
            summary = "Listar pagos por estado",
            description = "Filtra los pagos según su estado actual " +
                    "(PENDIENTE, APROBADO, RECHAZADO, CANCELADO o REEMBOLSADO)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pagos por estado obtenidos",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagoResponse.class)))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "El estado indicado no es válido"
            )
    })
    // estado y metodoPago llegan como String desde la URL
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarPorEstado(
            @Parameter(
                    description = "Estado del pago",
                    example = "APROBADO",
                    required = true,
                    schema = @Schema(allowableValues = {
                            "PENDIENTE", "APROBADO", "RECHAZADO", "CANCELADO", "REEMBOLSADO"
                    })
            )
            @PathVariable String estado) {
        log.info("GET /api/v1/pagos/estado/{}", estado);
        List<PagoResponse> pagos = pagoService.listarPagosPorEstado(estado.toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok("Pagos por estado obtenidos", pagos));
    }

    @Operation(
            summary = "Listar pagos por método de pago",
            description = "Filtra los pagos según el método utilizado " +
                    "(EFECTIVO, TARJETA_DEBITO, TARJETA_CREDITO, TRANSFERENCIA_BANCARIA, CHEQUE u OBRA_SOCIAL)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pagos por método obtenidos",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagoResponse.class)))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "El método de pago indicado no es válido"
            )
    })
    @GetMapping("/metodo/{metodoPago}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarPorMetodo(
            @Parameter(
                    description = "Método de pago utilizado",
                    example = "TARJETA_CREDITO",
                    required = true,
                    schema = @Schema(allowableValues = {
                            "EFECTIVO", "TARJETA_DEBITO", "TARJETA_CREDITO",
                            "TRANSFERENCIA_BANCARIA", "CHEQUE", "OBRA_SOCIAL"
                    })
            )
            @PathVariable String metodoPago) {
        log.info("GET /api/v1/pagos/metodo/{}", metodoPago);
        List<PagoResponse> pagos = pagoService.listarPagosPorMetodo(metodoPago.toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok("Pagos por método obtenidos", pagos));
    }

    @Operation(
            summary = "Listar pagos por rango de fechas",
            description = "Obtiene los pagos cuya fecha de pago se encuentra dentro del " +
                    "rango indicado. Solo accesible por administradores."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pagos en rango de fecha obtenidos",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagoResponse.class)))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene rol ADMIN"
            )
    })
    @GetMapping("/rango")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarPorRangoFecha(
            @Parameter(description = "Fecha y hora desde la cual buscar", example = "2026-06-01T00:00:00", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @Parameter(description = "Fecha y hora hasta la cual buscar", example = "2026-06-30T23:59:59", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        log.info("GET /api/v1/pagos/rango - desde: {} hasta: {}", desde, hasta);
        List<PagoResponse> pagos = pagoService.listarPagosPorRangoFecha(desde, hasta);
        return ResponseEntity.ok(ApiResponse.ok("Pagos en rango de fecha obtenidos", pagos));
    }

    @Operation(
            summary = "Actualizar el estado de un pago",
            description = "Permite cambiar el estado de un pago de forma manual, indicando " +
                    "el nuevo estado y, opcionalmente, un motivo."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = PagoResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Estado inválido o transición de estado no permitida"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No existe un pago con el ID indicado"
            )
    })
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<PagoResponse>> actualizarEstado(
            @Parameter(description = "Identificador único del pago", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody(
                    description = "Nuevo estado y motivo del cambio",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ActualizarEstadoRequest.class),
                            examples = @ExampleObject(
                                    name = "ActualizarEstado",
                                    value = """
                                            {
                                              "estado": "APROBADO",
                                              "motivo": "Pago verificado correctamente"
                                            }"""
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody ActualizarEstadoRequest request) {
        log.info("PATCH /api/v1/pagos/{}/estado", id);
        PagoResponse response = pagoService.actualizarEstadoPago(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado exitosamente", response));
    }

    @Operation(
            summary = "Aprobar un pago",
            description = "Cambia el estado de un pago a APROBADO."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pago aprobado exitosamente",
                    content = @Content(schema = @Schema(implementation = PagoResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No existe un pago con el ID indicado"
            )
    })
    @PatchMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<PagoResponse>> aprobarPago(
            @Parameter(description = "Identificador único del pago", example = "1", required = true)
            @PathVariable Long id) {
        log.info("PATCH /api/v1/pagos/{}/aprobar", id);
        PagoResponse response = pagoService.aprobarPago(id);
        return ResponseEntity.ok(ApiResponse.ok("Pago aprobado exitosamente", response));
    }

    @Operation(
            summary = "Rechazar un pago",
            description = "Cambia el estado de un pago a RECHAZADO, permitiendo indicar un motivo opcional."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pago rechazado",
                    content = @Content(schema = @Schema(implementation = PagoResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No existe un pago con el ID indicado"
            )
    })
    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<PagoResponse>> rechazarPago(
            @Parameter(description = "Identificador único del pago", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Motivo del rechazo", example = "Comprobante de transferencia ilegible")
            @RequestParam(required = false) String motivo) {
        log.info("PATCH /api/v1/pagos/{}/rechazar", id);
        PagoResponse response = pagoService.rechazarPago(id, motivo);
        return ResponseEntity.ok(ApiResponse.ok("Pago rechazado", response));
    }

    @Operation(
            summary = "Cancelar un pago",
            description = "Cambia el estado de un pago a CANCELADO. Puede ser ejecutado " +
                    "por administradores, recepcionistas o el propio paciente."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pago cancelado",
                    content = @Content(schema = @Schema(implementation = PagoResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No existe un pago con el ID indicado"
            )
    })
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'PACIENTE')")
    public ResponseEntity<ApiResponse<PagoResponse>> cancelarPago(
            @Parameter(description = "Identificador único del pago", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Motivo de la cancelación", example = "El paciente canceló el turno")
            @RequestParam(required = false) String motivo) {
        log.info("PATCH /api/v1/pagos/{}/cancelar", id);
        PagoResponse response = pagoService.cancelarPago(id, motivo);
        return ResponseEntity.ok(ApiResponse.ok("Pago cancelado", response));
    }

    @Operation(
            summary = "Reembolsar un pago",
            description = "Cambia el estado de un pago a REEMBOLSADO. Operación restringida a administradores."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Reembolso procesado exitosamente",
                    content = @Content(schema = @Schema(implementation = PagoResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene rol ADMIN"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No existe un pago con el ID indicado"
            )
    })
    @PatchMapping("/{id}/reembolsar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagoResponse>> reembolsarPago(
            @Parameter(description = "Identificador único del pago", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Motivo del reembolso", example = "Turno médico cancelado por el centro de salud")
            @RequestParam(required = false) String motivo) {
        log.info("PATCH /api/v1/pagos/{}/reembolsar", id);
        PagoResponse response = pagoService.reembolsarPago(id, motivo);
        return ResponseEntity.ok(ApiResponse.ok("Reembolso procesado exitosamente", response));
    }

    @Operation(
            summary = "Eliminar un pago",
            description = "Elimina de forma permanente un pago del sistema. " +
                    "Operación irreversible, restringida a administradores."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pago eliminado exitosamente"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene rol ADMIN"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No existe un pago con el ID indicado"
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminarPago(
            @Parameter(description = "Identificador único del pago", example = "1", required = true)
            @PathVariable Long id) {
        log.warn("DELETE /api/v1/pagos/{}", id);
        pagoService.eliminarPago(id);
        return ResponseEntity.ok(ApiResponse.ok("Pago eliminado exitosamente", null));
    }

    @Operation(
            summary = "Calcular el total recaudado por estado",
            description = "Suma el monto de todos los pagos que se encuentran en un estado específico."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Total calculado",
                    content = @Content(
                            schema = @Schema(implementation = BigDecimal.class),
                            examples = @ExampleObject(name = "Total", value = "150000.00")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene rol ADMIN"
            )
    })
    @GetMapping("/estadisticas/total/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BigDecimal>> totalPorEstado(
            @Parameter(
                    description = "Estado de los pagos a sumar",
                    example = "APROBADO",
                    required = true,
                    schema = @Schema(allowableValues = {
                            "PENDIENTE", "APROBADO", "RECHAZADO", "CANCELADO", "REEMBOLSADO"
                    })
            )
            @PathVariable String estado) {
        log.info("GET /api/v1/pagos/estadisticas/total/{}", estado);
        BigDecimal total = pagoService.calcularTotalPorEstado(estado.toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok("Total calculado", total));
    }

    @Operation(
            summary = "Contar pagos por estado y periodo",
            description = "Cuenta la cantidad de pagos que se encuentran en un estado específico " +
                    "dentro de un rango de fechas determinado."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Conteo obtenido",
                    content = @Content(
                            schema = @Schema(implementation = Long.class),
                            examples = @ExampleObject(name = "Conteo", value = "12")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene rol ADMIN"
            )
    })
    @GetMapping("/estadisticas/conteo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> conteoPorEstadoYPeriodo(
            @Parameter(
                    description = "Estado de los pagos a contar",
                    example = "APROBADO",
                    required = true,
                    schema = @Schema(allowableValues = {
                            "PENDIENTE", "APROBADO", "RECHAZADO", "CANCELADO", "REEMBOLSADO"
                    })
            )
            @RequestParam String estado,
            @Parameter(description = "Fecha y hora desde la cual contar", example = "2026-06-01T00:00:00", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @Parameter(description = "Fecha y hora hasta la cual contar", example = "2026-06-30T23:59:59", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        Long conteo = pagoService.contarPagosPorEstadoYPeriodo(estado.toUpperCase(), desde, hasta);
        return ResponseEntity.ok(ApiResponse.ok("Conteo obtenido", conteo));
    }
}
