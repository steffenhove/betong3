
package no.steffenhove.betongkalkulator

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.preference.PreferenceManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import no.steffenhove.betongkalkulator.ui.FirkantInput
import no.steffenhove.betongkalkulator.ui.KjerneInput
import no.steffenhove.betongkalkulator.ui.TrekantInput

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var defaultPrefs: SharedPreferences
    private var density: Double = 2400.0
    private var unitSystem: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        // Referanser til knapper for formvalg
        val buttonKjerne = findViewById<Button>(R.id.button_kjerne)
        val buttonFirkant = findViewById<Button>(R.id.button_firkant)
        val buttonTrekant = findViewById<Button>(R.id.button_trekant)

        // Referanser til layout-containere for inputfelt
        val layoutKjerne = findViewById<LinearLayout>(R.id.layout_kjerne)
        val layoutFirkant = findViewById<LinearLayout>(R.id.layout_firkant)
        val layoutTrekant = findViewById<LinearLayout>(R.id.layout_trekant)

        // Referanser til inputfelt for Kjerne
        val inputDiameter = findViewById<EditText>(R.id.input_diameter)
        val inputHeight = findViewById<EditText>(R.id.input_height)
        val radioGroupDiameterUnit = findViewById<RadioGroup>(R.id.radio_group_diameter_unit)
        val radioGroupHeightUnit = findViewById<RadioGroup>(R.id.radio_group_height_unit)

        // Referanser til inputfelt for Firkant
        val inputLength = findViewById<EditText>(R.id.input_length)
        val inputWidth = findViewById<EditText>(R.id.input_width)
        val inputThickness = findViewById<EditText>(R.id.input_thickness)
        val radioGroupLengthUnit = findViewById<RadioGroup>(R.id.radio_group_length_unit)
        val radioGroupWidthUnit = findViewById<RadioGroup>(R.id.radio_group_width_unit)
        val radioGroupThicknessUnit = findViewById<RadioGroup>(R.id.radio_group_thickness_unit)

        // Referanser til inputfelt for Trekant
        val inputSideA = findViewById<EditText>(R.id.input_side_a)
        val inputSideB = findViewById<EditText>(R.id.input_side_b)
        val inputSideC = findViewById<EditText>(R.id.input_side_c)
        val inputThicknessTriangle = findViewById<EditText>(R.id.input_thickness_triangle)
        val radioGroupSideAUnit = findViewById<RadioGroup>(R.id.radio_group_side_a_unit)
        val radioGroupSideBUnit = findViewById<RadioGroup>(R.id.radio_group_side_b_unit)
        val radioGroupSideCUnit = findViewById<RadioGroup>(R.id.radio_group_side_c_unit)
        val radioGroupThicknessTriangleUnit =
            findViewById<RadioGroup>(R.id.radio_group_thickness_triangle_unit)

        // Referanse til beregn-knappen
        val buttonCalculate = findViewById<Button>(R.id.button_calculate)

        // Referanse til resultat-TextView
        val textResult = findViewById<TextView>(R.id.text_result)

        // Spinner for tetthet
        val spinnerDensity = findViewById<Spinner>(R.id.spinner_density)
        val inputCustomDensity = findViewById<EditText>(R.id.input_custom_density)

        // Hent lagrede verdier fra SharedPreferences
        unitSystem = defaultPrefs.getString("unit_system_preference", "Metrisk")
        val savedDensity = prefs.getString("density_preference", getString(R.string.betong))
        val savedCustomDensity = prefs.getString("custom_density", "")

        // Sett standardvalg for tetthet
        val densityAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.density_options,
            android.R.layout.simple_spinner_item
        )
        densityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDensity.adapter = densityAdapter

        val densitySelectionIndex = resources.getStringArray(R.array.density_options).indexOf(savedDensity)
        spinnerDensity.setSelection(densitySelectionIndex)

        // Vis/skjul egendefinert tetthet inputfelt basert på lagret valg
        if (savedDensity == getString(R.string.custom_density)) {
            inputCustomDensity.visibility = View.VISIBLE
            inputCustomDensity.setText(savedCustomDensity)
        } else {
            inputCustomDensity.visibility = View.GONE
        }

        // Lytt til endringer i inputfeltene og oppdater resultatet
        val inputFields = listOf(
            inputDiameter, inputHeight, inputLength, inputWidth, inputThickness,
            inputSideA, inputSideB, inputSideC, inputThicknessTriangle
        )
        for (field in inputFields) {
            field.addTextChangedListener {
                updateResult(
                    textResult,
                    layoutKjerne,
                    layoutFirkant,
                    layoutTrekant,
                    spinnerDensity,
                    inputCustomDensity
                )
            }
        }

        // Lytt til endringer i radiogruppene
        val unitRadioGroups = listOf(
            radioGroupDiameterUnit, radioGroupHeightUnit, radioGroupLengthUnit, radioGroupWidthUnit,
            radioGroupThicknessUnit, radioGroupSideAUnit, radioGroupSideBUnit, radioGroupSideCUnit, radioGroupThicknessTriangleUnit
        )
        for (radioGroup in unitRadioGroups) {
            radioGroup.setOnCheckedChangeListener { _, _ ->
                updateResult(
                    textResult,
                    layoutKjerne,
                    layoutFirkant,
                    layoutTrekant,
                    spinnerDensity,
                    inputCustomDensity
                )
            }
        }

        // Spinner for valg av tetthet
        spinnerDensity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedDensity = parent?.getItemAtPosition(position).toString()
                if (selectedDensity == getString(R.string.custom_density)) {
                    inputCustomDensity.visibility = View.VISIBLE
                    val customDensity = prefs.getString("custom_density", "")
                    inputCustomDensity.setText(customDensity)
                } else {
                    inputCustomDensity.visibility = View.GONE
                }
                updateResult(textResult, layoutKjerne, layoutFirkant, layoutTrekant, spinnerDensity, inputCustomDensity)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Sett opp OnClickListener for hver formvalg-knapp
        buttonKjerne.setOnClickListener {
            layoutKjerne.visibility = View.VISIBLE
            layoutFirkant.visibility = View.GONE
            layoutTrekant.visibility = View.GONE
            updateResult(textResult, layoutKjerne, layoutFirkant, layoutTrekant, spinnerDensity, inputCustomDensity)
        }

        buttonFirkant.setOnClickListener {
            layoutKjerne.visibility = View.GONE
            layoutFirkant.visibility = View.VISIBLE
            layoutTrekant.visibility = View.GONE
            updateResult(textResult, layoutKjerne, layoutFirkant, layoutTrekant, spinnerDensity, inputCustomDensity)
        }

        buttonTrekant.setOnClickListener {
            layoutKjerne.visibility = View.GONE
            layoutFirkant.visibility = View.GONE
            layoutTrekant.visibility = View.VISIBLE
            updateResult(textResult, layoutKjerne, layoutFirkant, layoutTrekant, spinnerDensity, inputCustomDensity)
        }

        val kjerneInput = KjerneInput(inputDiameter, inputHeight, radioGroupDiameterUnit, radioGroupHeightUnit, textResult, defaultPrefs, this)
        val firkantInput = FirkantInput(inputLength, inputWidth, inputThickness, radioGroupLengthUnit, radioGroupWidthUnit, radioGroupThicknessUnit, textResult, defaultPrefs, this)
        val trekantInput = TrekantInput(inputSideA, inputSideB, inputSideC, inputThicknessTriangle, radioGroupSideAUnit, radioGroupSideBUnit, radioGroupSideCUnit, radioGroupThicknessTriangleUnit, textResult, defaultPrefs, this)

        buttonCalculate.setOnClickListener {
            // Hent tetthet fra spinner eller egendefinert felt
            this.density = when (spinnerDensity.selectedItem.toString()) {
                getString(R.string.leca) -> 1800.0
                getString(R.string.custom_density) -> inputCustomDensity.text.toString()
                    .toDoubleOrNull() ?: 2400.0
                else -> 2400.0 // Standard for betong
            }

            when {
                layoutKjerne.visibility == View.VISIBLE -> {
                    kjerneInput.handleKjerneCalculation()
                }
                layoutFirkant.visibility == View.VISIBLE -> {
                    firkantInput.handleFirkantCalculation()
                }
                layoutTrekant.visibility == View.VISIBLE -> {
                    trekantInput.handleTrekantCalculation()
                }
            }
        }
    }

    private fun updateResult(textResult: TextView, layoutKjerne: LinearLayout, layoutFirkant: LinearLayout, layoutTrekant: LinearLayout) {
        // Hent tetthet fra spinner eller egendefinert felt
        val density = when (findViewById<Spinner>(R.id.spinner_density).selectedItem.toString()) {
            getString(R.string.leca) -> 1800.0
            getString(R.string.custom_density) -> findViewById<EditText>(R.id.input_custom_density).text.toString().toDoubleOrNull() ?: 2400.0
            else -> 2400.0 // Standard for betong
        }

        val unitSystem = defaultPrefs.getString("unit_system_preference", "Metrisk")

        val resultText = when {
            layoutKjerne.visibility == View.VISIBLE -> {
                val diameter = findViewById<EditText>(R.id.input_diameter).text.toString().toDoubleOrNull()
                val height = findViewById<EditText>(R.id.input_height).text.toString().toDoubleOrNull()
                if (diameter != null && height != null) {
                    val diameterInMeters = convertToMeters(diameter, R.id.radio_group_diameter_unit)
                    val heightInMeters = convertToMeters(height, R.id.radio_group_height_unit)
                    val volume = calculateCylinderVolume(diameterInMeters, heightInMeters)
                    val weight = calculateWeight(volume)
                    val unit = if (unitSystem == "Imperialsk") "tommer" else "cm"
                    String.format(
                        Locale.ROOT,
                        "Volum: %.2f m³\nVekt: %.0f kg\nDiameter: %.0f %s\nHøyde: %.0f %s",
                        volume,
                        weight,
                        diameter,
                        unit,
                        height,
                        unit
                    ) + if (weight >= 1000) String.format(
                        Locale.ROOT,
                        " (%.1f tonn)",
                        weight / 1000
                    ) else ""
                } else {
                    ""
                }
            }

            layoutFirkant.visibility == View.VISIBLE -> {
                val length = findViewById<EditText>(R.id.input_length).text.toString().toDoubleOrNull()
                val width = findViewById<EditText>(R.id.input_width).text.toString().toDoubleOrNull()
                val thickness =
                    findViewById<EditText>(R.id.input_thickness).text.toString().toDoubleOrNull()
                if (length != null && width != null && thickness != null) {
                    val lengthInMeters = convertToMeters(length, R.id.radio_group_length_unit)
                    val widthInMeters = convertToMeters(width, R.id.radio_group_width_unit)
                    val thicknessInMeters =
                        convertToMeters(thickness, R.id.radio_group_thickness_unit)
                    val volume =
                        calculateBoxVolume(lengthInMeters, widthInMeters, thicknessInMeters)
                    val weight = calculateWeight(volume)
                    val unit = if (unitSystem == "Imperialsk") "tommer" else "cm"
                    String.format(
                        Locale.ROOT,
                        "Volum: %.2f m³\nVekt: %.0f kg\nLengde: %.0f %s\nBredde: %.0f %s\nTykkelse: %.0f %s",
                        volume,
                        weight,
                        length,
                        unit,
                        width,
                        unit,
                        thickness,
                        unit
                    ) + if (weight >= 1000) String.format(
                        Locale.ROOT,
                        " (%.1f tonn)",
                        weight / 1000
                    ) else ""
                } else {
                    ""
                }
            }

            layoutTrekant.visibility == View.VISIBLE -> {
                val sideA = findViewById<EditText>(R.id.input_side_a).text.toString().toDoubleOrNull()
                val sideB = findViewById<EditText>(R.id.input_side_b).text.toString().toDoubleOrNull()
                val sideC = findViewById<EditText>(R.id.input_side_c).text.toString().toDoubleOrNull()
                val thickness =
                    findViewById<EditText>(R.id.input_thickness_triangle).text.toString()
                        .toDoubleOrNull()
                if (sideA != null && sideB != null && sideC != null && thickness != null) {
                    val sideAInMeters = convertToMeters(sideA, R.id.radio_group_side_a_unit)
                   val sideBInMeters = convertToMeters(sideB, radioGroupSideBUnit.checkedRadioButtonId)
                val sideCInMeters = convertToMeters(sideC, radioGroupSideCUnit.checkedRadioButtonId)
                val thicknessInMeters = convertToMeters(thickness, radioGroupThicknessTriangleUnit.checkedRadioButtonId)

                val volume = calculateTriangleVolume(
                    sideAInMeters,
                    sideBInMeters,
                    sideCInMeters,
                    thicknessInMeters
                )
                val weight = calculateWeight(volume)

                val resultText =
                    String.format(Locale.ROOT, "Volum: %.2f m³\nVekt: %.0f kg", volume, weight)
                textResult.text = resultText
                if (weight >= 1000) {
                    val weightInTons = weight / 1000
                    textResult.append(String.format(Locale.ROOT, " (%.1f tonn)", weightInTons))
                }

                // Lagre beregningen til historikk
                val unitSystem = defaultPrefs.getString("unit_system_preference", "Metrisk")
                val dimensions = if (unitSystem == "Imperialsk") {
                    String.format(
                        "Side A: %.1f tommer, Side B: %.1f tommer, Side C: %.1f tommer, Tykkelse: %.1f tommer",
                        sideA,
                        sideB,
                        sideC,
                        thickness
                    )
                } else {
                    val sideAUnit = findViewById<RadioButton>(radioGroupSideAUnit.checkedRadioButtonId).text.toString()
                    val sideBUnit = findViewById<RadioButton>(radioGroupSideBUnit.checkedRadioButtonId).text.toString()
                    val sideCUnit = findViewById<RadioButton>(radioGroupSideCUnit.checkedRadioButtonId).text.toString()
                    val thicknessUnit = findViewById<RadioButton>(radioGroupThicknessTriangleUnit.checkedRadioButtonId).text.toString()
                    String.format(
                        "Side A: %.0f %s, Side B: %.0f %s, Side C: %.0f %s, Tykkelse: %.0f %s",
                        sideA,
                        sideAUnit,
                        sideB,
                        sideBUnit,
                        sideC,
                        sideCUnit,
                        thickness,
                        thicknessUnit
                    )
                }
                saveCalculationToHistory(volume, weight, "Trekant", dimensions)
            }
        }
    }

    // Funksjon for å konvertere mål til meter
    private fun convertToMeters(value: Double, unitGroupId: Int): Double {
        val unitSystem = defaultPrefs.getString("unit_system_preference", "Metrisk")
        val unitRadioButtonId = findViewById<RadioGroup>(unitGroupId).checkedRadioButtonId
        val unit = when (findViewById<RadioButton>(unitRadioButtonId).text.toString()) {
            "mm" -> "mm"
            "cm" -> "cm"
            "m" -> "m"
            "tommer" -> "tommer"
            else -> "cm" // Standard enhet
        }
        Log.d("ConvertToMeters", "Valgt enhetssystem: $unitSystem")
        val convertedValue = if (unitSystem == "Imperialsk") {
            // Konverter fra tommer til meter
            value * 0.0254
        } else {
            // Konverter fra mm, cm, eller m til meter
            when (unit) {
                "mm" -> value / 1000
                "cm" -> value / 100
                "m" -> value
                else -> {
                    Log.e("ConvertToMeters", "Ukjent enhet: $unit")
                    value // Returner samme verdi i tilfelle ukjent enhet
                }
            }
        }
        Log.d("ConvertToMeters", "Konvertert verdi: $convertedValue")
        return convertedValue
    }

    // Funksjon for å beregne volum av en sylinder (kjerne)
    private fun calculateCylinderVolume(diameter: Double, height: Double): Double {
        val radius = diameter / 2
        return Math.PI * radius * radius * height
    }

    // Funksjon for å beregne volum av en firkantet boks
    private fun calculateBoxVolume(length: Double, width: Double, thickness: Double): Double {
        return length * width * thickness
    }

    // Funksjon for å beregne volum av en trekant (Herons formel)
    private fun calculateTriangleVolume(sideA: Double, sideB: Double, sideC: Double, thickness: Double): Double {
        // Sjekk om trekanten er gyldig (trekantulikheten)
        if (sideA + sideB <= sideC || sideA + sideC <= sideB || sideB + sideC <= sideA) {
            Log.d("calculateTriangleVolume", "Ugyldig trekant: Side A + Side B <= Side C, eller lignende")
            return 0.0 // Returner 0 hvis trekanten er ugyldig
        }
        val s = (sideA + sideB + sideC) / 2.0
        val sMinusA = s - sideA
        val sMinusB = s - sideB
        val sMinusC = s - sideC

        val area = kotlin.math.sqrt(s * sMinusA * sMinusB * sMinusC)
        return if (area.isNaN()) {
            Log.d("calculateTriangleVolume", "Arealet er NaN (Not a Number). Sidelengder: A=$sideA, B=$sideB, C=$sideC, s=$s")
            0.0
        } else {
            area * thickness
        }
    }

    // Funksjon for å beregne vekt basert på volum og tetthet
    private fun calculateWeight(volume: Double): Double {
        val density = when (findViewById<Spinner>(R.id.spinner_density).selectedItem.toString()) {
            getString(R.string.leca) -> 1800.0
            getString(R.string.custom_density) -> findViewById<EditText>(R.id.input_custom_density).text.toString().toDoubleOrNull() ?: 2400.0
            else -> 2400.0 // Standard for betong
        }
        return volume * density
    }
    private fun saveCalculationToHistory(volume: Double, weight: Double, shape: String, dimensions: String) {
        val prefs = getSharedPreferences("history", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val currentHistory = prefs.getString("calculations", "[]") // Endret til å hente en JSON-array som en streng
        val jsonArray = try {
            JSONArray(currentHistory)
        } catch (e: Exception) {
            JSONArray()
        }

        // Lag et JSON objekt for den nye beregningen
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDateTime = currentDateTime.format(formatter)
        val newCalculation = JSONObject().apply {
            put("volume", String.format(Locale.ROOT, "%.2f", volume))
            put("weight", String.format(Locale.ROOT, "%.0f", weight))
            put("shape", shape)
            put("dimensions", dimensions)
            put("datetime", formattedDateTime)
            put("unit_system", defaultPrefs.getString("unit_system_preference", "Metrisk"))
            put("density", density)
            val customDensity = findViewById<EditText>(R.id.input_custom_density).text.toString()
            if (findViewById<Spinner>(R.id.spinner_density).selectedItem.toString() == getString(R.string.custom_density) && customDensity.isNotEmpty()) {
                put("custom_density", customDensity)
            }
        }

        // Legg til det nye JSON objektet til JSONArray
        jsonArray.put(newCalculation)

        // Begrens historikken til 20 beregninger
        while (jsonArray.length() > 20) {
            jsonArray.remove(0) // Fjern det eldste elementet
        }

        // Lagre den oppdaterte historikken
        editor.putString("calculations", jsonArray.toString())
        editor.apply()

        Log.d("History", "Saved calculation: $newCalculation")
    }
    private fun updateResult(textResult: TextView, layoutKjerne: LinearLayout, layoutFirkant: LinearLayout, layoutTrekant: LinearLayout) {
        // Hent tetthet fra spinner eller egendefinert felt
        val density = when (findViewById<Spinner>(R.id.spinner_density).selectedItem.toString()) {
            getString(R.string.leca) -> 1800.0
            getString(R.string.custom_density) -> findViewById<EditText>(R.id.input_custom_density).text.toString().toDoubleOrNull() ?: 2400.0
            else -> 2400.0 // Standard for betong
        }

        val unitSystem = defaultPrefs.getString("unit_system_preference", "Metrisk")

        val resultText = when {
            layoutKjerne.visibility == View.VISIBLE -> {
                val diameter = findViewById<EditText>(R.id.input_diameter).text.toString().toDoubleOrNull()
                val height = findViewById<EditText>(R.id.input_height).text.toString().toDoubleOrNull()
                if (diameter != null && height != null) {
                    val diameterInMeters = convertToMeters(diameter, R.id.radio_group_diameter_unit)
                    val heightInMeters = convertToMeters(height, R.id.radio_group_height_unit)
                    val volume = calculateCylinderVolume(diameterInMeters, heightInMeters)
                    val weight = calculateWeight(volume)
                    val unit = if (unitSystem == "Imperialsk") "tommer" else "cm"
                    String.format(
                        Locale.ROOT,
                        "Volum: %.2f m³\nVekt: %.0f kg\nDiameter: %.0f %s\nHøyde: %.0f %s",
                        volume,
                        weight,
                        diameter,
                        unit,
                        height,
                        unit
                    ) + if (weight >= 1000) String.format(
                        Locale.ROOT,
                        " (%.1f tonn)",
                        weight / 1000
                    ) else ""
                } else {
                    ""
                }
            }

            layoutFirkant.visibility == View.VISIBLE -> {
                val length = findViewById<EditText>(R.id.input_length).text.toString().toDoubleOrNull()
                val width = findViewById<EditText>(R.id.input_width).text.toString().toDoubleOrNull()
                val thickness =
                    findViewById<EditText>(R.id.input_thickness).text.toString().toDoubleOrNull()
                if (length != null && width != null && thickness != null) {
                    val lengthInMeters = convertToMeters(length, R.id.radio_group_length_unit)
                    val widthInMeters = convertToMeters(width, R.id.radio_group_width_unit)
                    val thicknessInMeters =
                        convertToMeters(thickness, R.id.radio_group_thickness_unit)
                    val volume =
                        calculateBoxVolume(lengthInMeters, widthInMeters, thicknessInMeters)
                    val weight = calculateWeight(volume)
                    val unit = if (unitSystem == "Imperialsk") "tommer" else "cm"
                    String.format(
                        Locale.ROOT,
                        "Volum: %.2f m³\nVekt: %.0f kg\nLengde: %.0f %s\nBredde: %.0f %s\nTykkelse: %.0f %s",
                        volume,
                        weight,
                        length,
                        unit,
                        width,
                        unit,
                        thickness,
                        unit
                    ) + if (weight >= 1000) String.format(
                        Locale.ROOT,
                        " (%.1f tonn)",
                        weight / 1000
                    ) else ""
                } else {
                    ""
                }
            }

            layoutTrekant.visibility == View.VISIBLE -> {
                val sideA = findViewById<EditText>(R.id.input_side_a).text.toString().toDoubleOrNull()
                val sideB = findViewById<EditText>(R.id.input_side_b).text.toString().toDoubleOrNull()
                val sideC = findViewById<EditText>(R.id.input_side_c).text.toString().toDoubleOrNull()
                val thickness =
                    findViewById<EditText>(R.id.input_thickness_triangle).text.toString()
                        .toDoubleOrNull()
                if (sideA != null && sideB != null && sideC != null && thickness != null) {
                    val sideAInMeters = convertToMeters(sideA, R.id.radio_group_side_a_unit)
                    val sideBInMeters = convertToMeters(sideB, R.id.radio_group_side_b_unit)
                    val sideCInMeters = convertToMeters(sideC, R.id.radio_group_side_c_unit)
                    val thicknessInMeters =
                        convertToMeters(thickness, R.id.radio_group_thickness_triangle_unit)
                    val volume = calculateTriangleVolume(
                        sideAInMeters,
                        sideBInMeters,
                        sideCInMeters,
                        thicknessInMeters
                    )
                    val unit = if (unitSystem == "Imperialsk") "tommer" else "cm"
                    if (volume == 0.0) "" else {
                        val weight = calculateWeight(volume)
                        String.format(
                            Locale.ROOT,
                            "Volum: %.2f m³\nVekt: %.0f kg\nSide A: %.0f %s\nSide B: %.0f %s\nSide C: %.0f %s\nTykkelse: %.0f %s",
                            volume,
                            weight,
                            sideA,
                            unit,
                            sideB,
                            unit,
                            sideC,
                            unit,
                            thickness,
                            unit
                        ) + if (weight >= 1000) String.format(
                            Locale.ROOT,
                            " (%.1f tonn)",
                            weight / 1000
                        ) else ""
                    }
                } else {
                    ""
                }
            }

            else -> ""
        }
        textResult.text = resultText
    }
}
