package no.steffenhove.betongkalkulator.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun InputRow(label: String, valueState: MutableState<String>, selectedUnit: String, onUnitSelected: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = valueState.value,
            onValueChange = { valueState.value = it },
            label = { Text(label) },
            modifier = Modifier.weight(1f)
        )
        UnitDropdown(selectedUnit = selectedUnit, onUnitSelected = onUnitSelected)
    }
}

@Composable
fun UnitDropdown(selectedUnit: String, onUnitSelected: (String) -> Unit) {
    val units = listOf("mm", "cm", "m")
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(selectedUnit)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            units.forEach { unit ->
                DropdownMenuItem(onClick = {
                    onUnitSelected(unit)
                    expanded = false
                }) {
                    Text(unit)
                }
            }
        }
    }
}