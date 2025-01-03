package no.steffenhove.betongkalkulator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.steffenhove.betongkalkulator.Unit as CustomUnit

@Composable
fun FirkantInput(onCalculateClick: (String, String, String, CustomUnit, CustomUnit, CustomUnit) -> Unit) {
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var thickness by remember { mutableStateOf("") }
    var selectedLengthUnit by remember { mutableStateOf(CustomUnit.METER) }
    var selectedWidthUnit by remember { mutableStateOf(CustomUnit.METER) }
    var selectedThicknessUnit by remember { mutableStateOf(CustomUnit.METER) }

    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = length,
            onValueChange = { length = it },
            label = { Text("Lengde") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = width,
            onValueChange = { width = it },
            label = { Text("Bredde") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = thickness,
            onValueChange = { thickness = it },
            label = { Text("Tykkelse") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            UnitDropdown(selectedUnit = selectedLengthUnit) { unit -> selectedLengthUnit = unit }
            UnitDropdown(selectedUnit = selectedWidthUnit) { unit -> selectedWidthUnit = unit }
            UnitDropdown(selectedUnit = selectedThicknessUnit) { unit -> selectedThicknessUnit = unit }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            onCalculateClick(length, width, thickness, selectedLengthUnit, selectedWidthUnit, selectedThicknessUnit)
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Beregn")
        }
    }
}