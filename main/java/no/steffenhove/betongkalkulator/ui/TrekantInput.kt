package no.steffenhove.betongkalkulator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.steffenhove.betongkalkulator.Unit as CustomUnit

@Composable
fun TrekantInput(onCalculateClick: (String, String, String, String, CustomUnit, CustomUnit, CustomUnit, CustomUnit) -> Unit) {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    var c by remember { mutableStateOf("") }
    var thickness by remember { mutableStateOf("") }
    var selectedAUnit by remember { mutableStateOf(CustomUnit.METER) }
    var selectedBUnit by remember { mutableStateOf(CustomUnit.METER) }
    var selectedCUnit by remember { mutableStateOf(CustomUnit.METER) }
    var selectedThicknessUnit by remember { mutableStateOf(CustomUnit.METER) }

    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = a,
            onValueChange = { a = it },
            label = { Text("Side A") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = b,
            onValueChange = { b = it },
            label = { Text("Side B") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = c,
            onValueChange = { c = it },
            label = { Text("Side C") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = thickness,
            onValueChange = { thickness = it },
            label = { Text("Tykkelse") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            UnitDropdown(selectedUnit = selectedAUnit) { unit -> selectedAUnit = unit }
            UnitDropdown(selectedUnit = selectedBUnit) { unit -> selectedBUnit = unit }
            UnitDropdown(selectedUnit = selectedCUnit) { unit -> selectedCUnit = unit }
            UnitDropdown(selectedUnit = selectedThicknessUnit) { unit -> selectedThicknessUnit = unit }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            onCalculateClick(a, b, c, thickness, selectedAUnit, selectedBUnit, selectedCUnit, selectedThicknessUnit)
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Beregn")
        }
    }
}