package ai.decart.example.model

import ai.decart.sdk.RealtimeModels
import ai.decart.sdk.RealtimeModel

enum class AppModel(val label: String, val realtimeModel: RealtimeModel) {
    RESTYLE("Restyle", RealtimeModels.MIRAGE_V2),
    EDIT("Edit", RealtimeModels.LUCY_2_RT);
}
