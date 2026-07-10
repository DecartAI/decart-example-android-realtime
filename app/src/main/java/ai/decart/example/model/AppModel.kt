package ai.decart.example.model

import ai.decart.sdk.RealtimeModels
import ai.decart.sdk.RealtimeModel

enum class AppModel(val label: String, val realtimeModel: RealtimeModel) {
    RESTYLE("Restyle", RealtimeModels.LUCY_RESTYLE_2),
    EDIT("Edit", RealtimeModels.LUCY_2_1);
}
