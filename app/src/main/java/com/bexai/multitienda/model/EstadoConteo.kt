package com.bexai.multitienda.model

enum class EstadoConteo {
    EN_CURSO,   // el operador sigue contando
    ENVIADO,    // esperando revisión del encargado
    APROBADO,   // el encargado aprobó: el stock ya cambió
    RECHAZADO   // el encargado lo rechazó: el stock no cambió
}
