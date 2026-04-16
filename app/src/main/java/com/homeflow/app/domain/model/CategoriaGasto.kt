package com.homeflow.app.domain.model

enum class CategoriaGasto(val emoji: String, val label: String) {
    FOOD("🍕", "Food"),
    SUPERMARKET("🛒", "Supermarket"),
    SERVICES("💡", "Services"),
    CLEANING("🧹", "Cleaning"),
    TRANSPORT("🚗", "Transport"),
    ENTERTAINMENT("🎬", "Entertain."),
    HEALTH("💊", "Health"),
    OTHER("📦", "Other")
}
