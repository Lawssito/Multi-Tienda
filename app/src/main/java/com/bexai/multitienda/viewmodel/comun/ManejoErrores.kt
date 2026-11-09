package com.bexai.multitienda.viewmodel.comun

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler

/** Mensaje que ve el usuario cuando una operación falla (el detalle técnico queda en Logcat). */
const val MENSAJE_ERROR_GENERICO = "No se pudo completar la operación. Intenta de nuevo."

/**
 * Manejador de errores para las corrutinas de los ViewModel.
 *
 * Se pasa a `viewModelScope.launch(errores) { ... }`. Si una operación de Room falla
 * (por ejemplo una restricción de la base o un disco lleno), la app no se cierra: el error se
 * registra en Logcat con [etiqueta] y se llama a [alFallar] con un mensaje para mostrar.
 * Las cancelaciones normales de corrutinas no pasan por aquí.
 */
fun manejadorErrores(
    etiqueta: String,
    alFallar: (String) -> Unit = {}
): CoroutineExceptionHandler = CoroutineExceptionHandler { _, error ->
    Log.e(etiqueta, "Operación fallida", error)
    alFallar(MENSAJE_ERROR_GENERICO)
}
