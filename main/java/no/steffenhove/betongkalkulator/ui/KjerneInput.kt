package no.steffenhove.betongkalkulator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.steffenhove.betongkalkulator.Unit as CustomUnit

@Composable
fun KjerneInput(onCalculateClick: (String, String, CustomUnit, CustomUnit) -> Unit) {
    var diameter by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var selectedDiameterUnit by remember { mutableStateOf(CustomUnit.METER) }
    var selectedHeightUnit by remember { mutableStateOf(CustomUnit.METER) }

    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = diameter,
            onValueChange = { diameter = it },
            label = { Text("Diameter") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Høyde") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            UnitDropdown(selectedUnit = selectedDiameterUnit) { unit -> selectedDiameterUnit = unit }
            UnitDropdown(selectedUnit = selectedHeightUnit) { unit -> selectedHeightUnit = unit }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            onCalculateClick(diameter, height, selectedDiameterUnit, selectedHeightUnit)
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Beregn")
        }
    }
}