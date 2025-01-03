package no.steffenhove.betongkalkulator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat

class HistoryActivity : AppCompatActivity() {

    private lateinit var historyListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var history: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        historyListView = findViewById(R.id.historyListView)
        history = getHistory().toMutableList()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, history)
        historyListView.adapter = adapter
        historyListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL

        historyListView.setMultiChoiceModeListener(object : AbsListView.MultiChoiceModeListener {
            override fun onItemCheckedStateChanged(mode: ActionMode?, position: Int, id: Long, checked: Boolean) {
                // Oppdater tittelen på contextual action bar med antall valgte elementer
                mode?.title = "${historyListView.checkedItemCount} valgt"
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                // Inflater menyen for contextual action bar
                menuInflater.inflate(R.menu.history_context_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false // Return false hvis ingenting er gjort
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                // Håndterer klikk på menyvalg
                return when (item?.itemId) {
                    R.id.action_delete -> {
                        deleteSelectedItems()
                        mode?.finish() // Avslutt contextual action bar
                        true
                    }
                    R.id.action_sum -> {
                        sumSelectedItems()
                        mode?.finish()
                        true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                // Her kan du gjøre nødvendige oppdateringer når contextual action bar avsluttes
            }
        })

        val buttonClearHistory = findViewById<Button>(R.id.button_clear_history)
        buttonClearHistory.setOnClickListener {
            clearHistory()
            // Oppdater listen etter sletting
            historyListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, getHistory())
        }
    }

    private fun getHistory(): List<String> {
        val prefs = getSharedPreferences("history", MODE_PRIVATE)
        val calculationsString = prefs.getString("calculations", "[]") // Henter som JSON-streng
        val jsonArray = try {
            JSONArray(calculationsString)
        } catch (e: JSONException) {
            Log.e("HistoryActivity", "Error parsing history JSON", e)
            JSONArray()
        }

        val historyList = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val volume = jsonObject.getString("volume")
            val weight = jsonObject.getString("weight")
            val shape = jsonObject.getString("shape")
            val dimensions = jsonObject.getString("dimensions")
            val datetime = jsonObject.optString("datetime", "Ukjent tidspunkt") // Håndterer manglende datetime
            historyList.add("Volum: $volume, Vekt: $weight, Form: $shape, Dimensjoner: $dimensions, Tid: $datetime")
        }

        return historyList
    }

    private fun deleteSelectedItems() {
        val selectedItems = historyListView.checkedItemPositions
        val prefs = getSharedPreferences("history", MODE_PRIVATE)
        val editor = prefs.edit()
        val calculationsString = prefs.getString("calculations", "[]")
        val jsonArray = try {
            JSONArray(calculationsString)
        } catch (e: JSONException) {
            Log.e("HistoryActivity", "Error parsing history JSON", e)
            JSONArray()
        }

        for (i in selectedItems.size() - 1 downTo 0) {
            if (selectedItems.valueAt(i)) {
                jsonArray.remove(selectedItems.keyAt(i))
            }
        }

        editor.putString("calculations", jsonArray.toString())
        editor.apply()

        history = getHistory().toMutableList()
        adapter.clear()
        adapter.addAll(history)
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Valgte elementer er slettet", Toast.LENGTH_SHORT).show()
    }

    private fun sumSelectedItems() {
        val selectedItems = historyListView.checkedItemPositions
        var totalVolume = 0.0
        var totalWeight = 0.0

        for (i in 0 until selectedItems.size()) {
            if (selectedItems.valueAt(i)) {
                val item = adapter.getItem(selectedItems.keyAt(i)) ?: continue
                val parts = item.split(", ")
                val volume = parts[0].split(": ")[1].toDoubleOrNull() ?: 0.0
                val weight = parts[1].split(": ")[1].toDoubleOrNull() ?: 0.0
                totalVolume += volume
                totalWeight += weight
            }
        }

        val message = "Total volum: ${DecimalFormat("#.##").format(totalVolume)} m³\nTotal vekt: ${DecimalFormat("#.##").format(totalWeight)} kg"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun clearHistory() {
        val prefs = getSharedPreferences("history", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("calculations", "[]")
        editor.apply()
        history.clear()
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Historikk er tømt", Toast.LENGTH_SHORT).show()
    }
}