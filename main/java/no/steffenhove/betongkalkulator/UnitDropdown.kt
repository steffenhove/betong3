package no.steffenhove.betongkalkulator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.steffenhove.betongkalkulator.Unit as CustomUnit

@Composable
fun UnitDropdown(selectedUnit: CustomUnit, onUnitSelected: (CustomUnit) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val units = CustomUnit.values().toList()
    Box(modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.TopStart)) {
        TextButton(onClick = { expanded = true }) {
            Text(selectedUnit.display)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(onClick = {
                    onUnitSelected(unit)
                    expanded = false
                }) {
                    Text(text = unit.display)
                }
            }
        }
    }
}