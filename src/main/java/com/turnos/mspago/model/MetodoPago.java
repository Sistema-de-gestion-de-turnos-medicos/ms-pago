package com.turnos.mspago.model;


public class MetodoPago {

    public static final String EFECTIVO               = "EFECTIVO";
    public static final String TARJETA_DEBITO         = "TARJETA_DEBITO";
    public static final String TARJETA_CREDITO        = "TARJETA_CREDITO";
    public static final String TRANSFERENCIA_BANCARIA = "TRANSFERENCIA_BANCARIA";
    public static final String CHEQUE                 = "CHEQUE";
    public static final String OBRA_SOCIAL            = "OBRA_SOCIAL";

    private final String valor;

    public MetodoPago(String valor) {
        validar(valor);
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static boolean esValido(String valor) {
        return EFECTIVO.equals(valor)
                || TARJETA_DEBITO.equals(valor)
                || TARJETA_CREDITO.equals(valor)
                || TRANSFERENCIA_BANCARIA.equals(valor)
                || CHEQUE.equals(valor)
                || OBRA_SOCIAL.equals(valor);
    }

    private static void validar(String valor) {
        if (!esValido(valor)) {
            throw new IllegalArgumentException(
                    "Método de pago inválido: '" + valor + "'. Valores permitidos: "
                    + EFECTIVO + ", " + TARJETA_DEBITO + ", " + TARJETA_CREDITO + ", "
                    + TRANSFERENCIA_BANCARIA + ", " + CHEQUE + ", " + OBRA_SOCIAL);
        }
    }

    @Override
    public String toString() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetodoPago other)) return false;
        return valor.equals(other.valor);
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }
}
