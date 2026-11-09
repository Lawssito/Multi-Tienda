package com.bexai.multitienda.ui.navigation

import com.bexai.multitienda.model.TipoMovimiento

/**
 * Rutas de la aplicación.
 *
 * Se usa una "sealed class" para tener todas las rutas en un solo lugar y
 * evitar escribir textos sueltos por todo el código. Así, si una ruta cambia,
 * solo se modifica aquí.
 *
 * Cada ruta tiene una propiedad [ruta] que es el texto que entiende
 * Navigation Compose.
 */
sealed class Ruta(val ruta: String) {

    // Pantalla inicial (login).
    data object Login : Ruta("login")

    // Pantallas del grafo de operador (las completa la Persona A).
    data object OperadorSucursal : Ruta("operador/sucursal")
    data object OperadorInicio : Ruta("operador/inicio")
    data object OperadorEscaner : Ruta("operador/escaner") {
        // Patrón que registra el grafo (ambos argumentos son opcionales)
        val patron = "operador/escaner?tipo={tipo}&conteo={conteo}&recepcion={recepcion}"

        // Ruta concreta para navegar con un tipo
        fun conTipo(tipo: TipoMovimiento, conteo: Boolean = false) =
            "operador/escaner?tipo=${tipo.name}&conteo=$conteo"

        // Escáner para recibir los productos de una caja (recepcion = id de la recepción en curso)
        fun paraRecepcion(recepcionId: Int) =
            "operador/escaner?tipo=${TipoMovimiento.ENTRADA.name}&conteo=false&recepcion=$recepcionId"
    }
    data object OperadorCuenta : Ruta("operador/cuenta")
    data object OperadorConteo : Ruta("operador/conteo")
    data object OperadorResumenConteo : Ruta("operador/conteo/resumen")
    data object OperadorRecepcion : Ruta("operador/recepcion") {
        val patron = "operador/recepcion/{despachoId}"
        fun conDespacho(despachoId: Int) = "operador/recepcion/$despachoId"
    }
    data object OperadorResumenRecepcion : Ruta("operador/recepcion/resumen") {
        val patron = "operador/recepcion/{despachoId}/resumen"
        fun conDespacho(despachoId: Int) = "operador/recepcion/$despachoId/resumen"
    }

    // Pantalla del grafo de encargado
    data object EncargadoInicio : Ruta("encargado/inicio")
    data object EncargadoStock : Ruta("encargado/stock")
    data object EncargadoDiferencias : Ruta("encargado/diferencias")
    data object EncargadoSucursal : Ruta("encargado/sucursal")
    data object EncargadoCuenta : Ruta("encargado/cuenta")
    data object EncargadoAlertas : Ruta("encargado/alertas")
    data object EncargadoRevisarRecepcion : Ruta("encargado/recepcion") {
        val patron = "encargado/recepcion/{recepcionId}"
        fun conRecepcion(recepcionId: Int) = "encargado/recepcion/$recepcionId"
    }

    // Pantallas del grafo de gerencia.
    data object GerenciaInicio : Ruta("gerencia/inicio")
    data object GerenciaGestionTiendas : Ruta("gerencia/gestion-tiendas")
    data object GerenciaStock : Ruta("gerencia/stock")
    data object GerenciaAlertas : Ruta("gerencia/alertas")
    data object GerenciaHistorial : Ruta("gerencia/historial")
    data object GerenciaCuenta : Ruta("gerencia/cuenta")

    companion object {
        // Nombre interno de cada grafo (se usa en AppNavHost)
        const val GRAFO_OPERADOR = "grafo_operador"
        const val GRAFO_ENCARGADO = "grafo_encargado"
        const val GRAFO_GERENCIA = "grafo_gerencia"
    }
}
