package ai.decart.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.decart.example.model.AppModel

@Composable
fun ModelSelector(
    currentModel: AppModel,
    onModelSelected: (AppModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        AppModel.entries.forEach { model ->
            val isSelected = model == currentModel
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(17.dp))
                    .then(
                        if (isSelected) Modifier.background(Color.White.copy(alpha = 0.2f))
                        else Modifier
                    )
                    .clickable { onModelSelected(model) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = model.label,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
