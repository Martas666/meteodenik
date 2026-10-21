package cz.utb.meteodenik

import kotlin.time.Duration.Companion.milliseconds

data class Mesto(val nazev: String, val teplota: Double)

object Konfigurace {
    const val ZAKLADNI_URL = "https://api.open-meteo.com/v1/forecast"
}

fun Double.naStupneCelsia(): String = "%.1f °C".format(this)

fun vypisVse(mesta: List<Mesto>, akce: (Mesto) -> Unit) {
    mesta.forEach { akce(it) }
}

suspend fun pockejAVrat(): Int {
    kotlinx.coroutines.delay(100.milliseconds)
    return 42
}