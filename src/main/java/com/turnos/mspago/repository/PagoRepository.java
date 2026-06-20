package com.turnos.mspago.repository;

import com.turnos.mspago.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByPacienteId(Long pacienteId);

    List<Pago> findByTurnoId(Long turnoId);

    Optional<Pago> findByNumeroComprobante(String numeroComprobante);


    List<Pago> findByEstado(String estado);

    List<Pago> findByMetodoPago(String metodoPago);

    List<Pago> findByPacienteIdAndEstado(Long pacienteId, String estado);

    List<Pago> findByFechaPagoBetween(LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT p FROM Pago p WHERE p.pacienteId = :pacienteId ORDER BY p.fechaCreacion DESC")
    List<Pago> findByPacienteIdOrderByFechaDesc(@Param("pacienteId") Long pacienteId);

    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.estado = :estado")
    BigDecimal sumMontoByEstado(@Param("estado") String estado);

    @Query("SELECT COUNT(p) FROM Pago p WHERE p.estado = :estado AND p.fechaPago BETWEEN :desde AND :hasta")
    Long countByEstadoAndFechaPagoBetween(
            @Param("estado") String estado,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

    boolean existsByNumeroComprobante(String numeroComprobante);
}
