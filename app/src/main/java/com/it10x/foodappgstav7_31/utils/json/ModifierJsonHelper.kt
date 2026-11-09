import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.it10x.foodappgstav7_31.data.pos.models.CartModifier

object ModifierJsonHelper {

    private val gson = Gson()

    private val type = object : TypeToken<List<CartModifier>>() {}.type

    fun fromJson(json: String?): List<CartModifier> {
        if (json.isNullOrBlank()) return emptyList()

        return try {
            gson.fromJson<List<CartModifier>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun toJson(list: List<CartModifier>): String {
        return gson.toJson(list)
    }
}