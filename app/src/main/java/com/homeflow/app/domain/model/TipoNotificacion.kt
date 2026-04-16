package com.homeflow.app.domain.model

enum class TipoNotificacion(val color: String) {
    TASK_ASSIGNED("#444444"),
    DUE_SOON("#666666"),
    OVERDUE("#333333"),
    COMMENT("#777777"),
    EXPENSE("#555555"),
    MEMBER_JOINED("#888888"),
    DIGEST("#999999")
}
