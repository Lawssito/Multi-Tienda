package com.bexai.multitienda.repository

import com.bexai.multitienda.data.UbicacionDao
import com.bexai.multitienda.model.Ubicacion
import kotlinx.coroutines.flow.Flow

/**
 * UbicacionRepository: acceso a las sucursales guardadas en Room.
 *
 * La pantalla de sesión observa [listar] para mostrar las sucursales y
 * deshabilitar las que tienen activa = false.
 */
class UbicacionRepository(
    private val ubicacionDao: UbicacionDao
) {

    fun listar(): Flow<List<Ubicacion>> = ubicacionDao.listar()

    fun listarActivas(): Flow<List<Ubicacion>> = ubicacionDao.listarActivas()

    suspend fun buscarPorId(id: Int): Ubicacion? = ubicacionDao.buscarPorId(id)

    suspend fun insertar(ubicacion: Ubicacion): Long = ubicacionDao.insertar(ubicacion)

    suspend fun actualizar(ubicacion: Ubicacion) = ubicacionDao.actualizar(ubicacion)

    suspend fun eliminar(ubicacion: Ubicacion) = ubicacionDao.eliminar(ubicacion)

    /**
     * Crea sucursales de ejemplo la primera vez que se abre la aplicación.
     * Una queda inactiva para poder probar que la interfaz la deshabilita.
     */
    suspend fun asegurarUbicacionesDemo() {
        if (ubicacionDao.contar() > 0) return

        ubicacionDao.insertar(
            Ubicacion(
                nombre = "Sucursal Centro",
                direccion = "Av. Libertador Bernardo O'Higgins 123",
                activa = true
            )
        )
        ubicacionDao.insertar(
            Ubicacion(
                nombre = "Sucursal Providencia",
                direccion = "Av. Providencia 456",
                activa = true
            )
        )
        ubicacionDao.insertar(
            Ubicacion(
                nombre = "Sucursal Ñuñoa",
                direccion = "Av. Irarrázaval 789",
                activa = false
            )
        )
    }
}
