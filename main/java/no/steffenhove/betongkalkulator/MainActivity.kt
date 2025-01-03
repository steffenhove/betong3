package no.steffenhove.betongkalkulator

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import no.steffenhove.betongkalkulator.ui.FirkantInput
import no.steffenhove.betongkalkulator.ui.KjerneInput
import no.steffenhove.betongkalkulator.ui.TrekantInput
import no.steffenhove.betongkalkulator.Unit as CustomUnit

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        setContent {
            MainActivityContent(context = this, prefs = prefs)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainActivityContent(context: Context, prefs: SharedPreferences) {
    val unitSystem = prefs.getString("unit_system", "metric") ?: "metric"
    val weightUnit = prefs.getString("weight_unit", "default") ?: "default"

    var selectedShape by remember { mutableStateOf("Kjerne") }
    var resultText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.padding(16.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                readOnly = true,
                value = selectedShape,
                onValueChange = { },
                label = { Text("Shape") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(onClick = { selectedShape = "Kjerne"; expanded = false }) {
                    Text("Kjerne")
                }
                DropdownMenuItem(onClick = { selectedShape = "Firkant"; expanded = false }) {
                    Text("Firkant")
                }
                DropdownMenuItem(onClick = { selectedShape = "Trekant"; expanded = false }) {
                    Text("Trekant")
                }
            }
        }

        when (selectedShape) {
            "Kjerne" -> KjerneInput { diameter, height, diameterUnit, heightUnit ->
                Log.d("MainActivityContent", "KjerneInput - diameter: $diameter, height: $height")
                resultText = calculateAndSave(
                    context,
                    "Kjerne",
                    coreDimensions = Dimensions(diameter.toDouble(), diameterUnit),
                    heightDimensions = Dimensions(height.toDouble(), heightUnit),
                    unitSystem = unitSystem,
                    weightUnit = weightUnit
                )
                Log.d("MainActivityContent", "Resultat for Kjerne: $resultText")
            }
            "Firkant" -> FirkantInput { length, width, thickness, lengthUnit, widthUnit, thicknessUnit ->
                Log.d("MainActivityContent", "FirkantInput - length: $length, width: $width, thickness: $thickness")
                resultText = calculateAndSave(
                    context,
                    "Firkant",
                    lengthDimensions = Dimensions(length.toDouble(), lengthUnit),
                    widthDimensions = Dimensions(width.toDouble(), widthUnit),
                    thicknessDimensions = Dimensions(thickness.toDouble(), thicknessUnit),
                    unitSystem = unitSystem,
                    weightUnit = weightUnit
                )
                Log.d("MainActivityContent", "Resultat for Firkant: $resultText")
            }
            "Trekant" -> TrekantInput { a, b, c, thickness, aUnit, bUnit, cUnit, thicknessUnit ->
                Log.d("MainActivityContent", "TrekantInput - a: $a, b: $b, c: $c, thickness: $thickness")
                resultText = calculateAndSave(
                    context,
                    "Trekant",
                    sideADimensions = Dimensions(a.toDouble(), aUnit),
                    sideBDimensions = Dimensions(b.toDouble(), bUnit),
                    sideCDimensions = Dimensions(c.toDouble(), cUnit),
                    thicknessDimensions = Dimensions(thickness.toDouble(), thicknessUnit),
                    unitSystem = unitSystem,
                    weightUnit = weightUnit
                )
                Log.d("MainActivityContent", "Resultat for Trekant: $resultText")
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(text = resultText)
    }
}

fun calculateAndSave(
    context: Context,
    shapeType: String,
    lengthDimensions: Dimensions? = null,
    widthDimensions: Dimensions? = null,
    thicknessDimensions: Dimensions? = null,
    sideADimensions: Dimensions? = null,
    sideBDimensions: Dimensions? = null,
    sideCDimensions: Dimensions? = null,
    coreDimensions: Dimensions? = null,
    heightDimensions: Dimensions? = null,
    unitSystem: String,
    weightUnit: String
): String {
    Log.d("calculateAndSave", "Shape type: $shapeType")
    Log.d("calculateAndSave", "coreDimensions: $coreDimensions, heightDimensions: $heightDimensions")

    // Implementer beregnings- og lagringslogikk her
    return "Resultat av beregningen"
}