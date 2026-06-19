package com.example.popmart

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale

class CollectionRepository(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("popmart_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveItems(items: List<PopmartItem>) {
        val json = gson.toJson(items)
        sharedPreferences.edit().putString("items_key", json).apply()
    }

    fun loadItems(): List<PopmartItem> {
        val json = sharedPreferences.getString("items_key", null)
        if (json == null) {
            val initialData = getDummyData()
            saveItems(initialData)
            return initialData
        }
        val type = object : TypeToken<List<PopmartItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getDummyData(): List<PopmartItem> {
        val characters = listOf(
            "Molly", "Skullpanda", "Dimoo", "Labubu", "Pucky", 
            "Hirono", "Satyr Rory", "Bunny", "Sweet Bean", "Crybaby",
            "Zsiga", "Agan", "Hacipupu", "Nyota", "Kasing Lung"
        )
        val themes = listOf(
            "Space Journey", "City Pop", "Fantasy Parade", "Ancient Castle",
            "Winter Wonderland", "Fruit Party", "Neon Lights", "Forest Whispers",
            "Ocean Adventure", "Retro Game", "Steampunk", "Garden Picnic",
            "Midnight Circus", "School Days", "Starry Sky"
        )
        val seriesTypes = listOf("Series 1", "Series 2", "Limited Edition", "Special Collab")
        val rarities = listOf("Common", "Uncommon", "Rare", "Secret", "Super Secret")

        return List(100) { index ->
            val charIdx = index % characters.size
            val themeIdx = (index / 2) % themes.size
            val rarityIdx = if (index % 20 == 0) 3 else (index % 3) // Make some secrets
            
            val year = 2023 + (index / 50)
            val month = (index % 12) + 1
            val day = (index % 28) + 1
            
            PopmartItem(
                id = (index + 1).toLong(),
                name = "${characters[charIdx]} - ${themes[themeIdx]}",
                series = "${characters[charIdx]} ${seriesTypes[index % seriesTypes.size]}",
                rarity = rarities[rarityIdx],
                dateAcquired = String.format(Locale.US, "%04d-%02d-%02d", year, month, day),
                quantity = (index % 3) + 1,
                unitPrice = 15.0 + (index % 10) * 2.5,
                notes = if (rarities[rarityIdx].contains("Secret")) "Found this hidden gem!" else "A great addition to the collection."
            )
        }
    }

    fun nextId(items: List<PopmartItem>): Long {
        return (items.maxOfOrNull { it.id } ?: 0L) + 1L
    }
}
