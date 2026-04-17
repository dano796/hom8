package com.hom8.app.domain.model

enum class CategoriaGasto(val emoji: String, val label: String) {
    COMIDA("🍕", "Comida"),
    SUPERMERCADO("🛒", "Supermercado"),
    SERVICIOS("💡", "Servicios"),
    LIMPIEZA("🧹", "Limpieza"),
    TRANSPORTE("🚗", "Transporte"),
    OCIO("🎬", "Ocio"),
    SALUD("💊", "Salud"),
    OTROS("📦", "Otros")
}
