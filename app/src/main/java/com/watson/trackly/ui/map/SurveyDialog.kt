package com.watson.trackly.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.watson.trackly.ui.components.BodyText
import com.watson.trackly.ui.components.HeaderText
import com.watson.trackly.ui.components.LabelText
import com.watson.trackly.ui.components.PrimaryButton
import com.watson.trackly.ui.components.SecondaryButton

@Composable
fun SurveyDialog(
    locationName: String,
    options: List<SurveyOption>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    onResolveNow: () -> Unit = onSubmit,
    onResolveLater: () -> Unit = onDismiss
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                HeaderText(
                    text = locationName,
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                LabelText(text = "Please select an issue:")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Radio button options
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedOption == option.id,
                                onClick = { onOptionSelected(option.id) }
                            )
                            .padding(vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == option.id,
                            onClick = { onOptionSelected(option.id) }
                        )
                        BodyText(
                            text = option.label,
                            modifier = Modifier.padding(start = 1.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Two buttons in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    PrimaryButton(
                        text = "Resolve",
                        onClick = onResolveNow,
                        enabled = selectedOption != null,
                        modifier = Modifier.weight(1f)
                    )
                    
                    SecondaryButton(
                        text = "Later",
                        onClick = onResolveLater,
                        enabled = selectedOption != null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// Made with Bob
