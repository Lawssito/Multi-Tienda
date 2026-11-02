package com.bexai.multitienda.ui.navigation

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
    data object OperadorInicio : Ruta("operador/inicio")
    data object OperadorEscaner : Ruta("operador/escaner")
    data object OperadorCuenta : Ruta("operador/cuenta")

    // Pantalla del grafo de encargado
    data object EncargadoInicio : Ruta("encargado/inicio")

    // Pantallas del grafo de gerencia.
    data object GerenciaInicio : Ruta("gerencia/inicio")
    data object GerenciaReportes : Ruta("gerencia/reportes")

    companion object {
        // Nombre interno de cada grafo (se usa en AppNavHost)
        const val GRAFO_OPERADOR = "grafo_operador"
        const val GRAFO_ENCARGADO = "grafo_encargado"
        const val GRAFO_GERENCIA = "grafo_gerencia"
    }
}
