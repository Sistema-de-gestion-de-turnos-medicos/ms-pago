package com.turnos.mspago.model;


public class EstadoPago {

    public static final String PENDIENTE    = "PENDIENTE";
    public static final String APROBADO     = "APROBADO";
    public static final String RECHAZADO    = "RECHAZADO";
    public static final String CANCELADO    = "CANCELADO";
    public static final String REEMBOLSADO  = "REEMBOLSADO";

    private final String valor;

    public EstadoPago(String valor) {
        validar(valor);
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }


    public static boolean esValido(String valor) {
        return PENDIENTE.equals(valor)
                || APROBADO.equals(valor)
                || RECHAZADO.equals(valor)
                || CANCELADO.equals(valor)
                || REEMBOLSADO.equals(valor);
    }

    private static void validar(String valor) {
        if (!esValido(valor)) {
            throw new IllegalArgumentException(
                    "Estado de pago inválido: '" + valor + "'. Valores permitidos: "
                    + PENDIENTE + ", " + APROBADO + ", " + RECHAZADO + ", "
                    + CANCELADO + ", " + REEMBOLSADO);
        }
    }

    @Override
    public String toString() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EstadoPago other)) return false;
        return valor.equals(other.valor);
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }
}
