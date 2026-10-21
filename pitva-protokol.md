# Dekompilační protokol

Porovnal jsem původní Kotlin s dekompilovanou Javou. Podpisy uvádím bez anotací a počty řádků v Javě jsou jen odhady.

# Tabulka konstrukcí

| Konstrukce | Co jsem napsal v Kotlinu | Generované členy – přesné podpisy | Odhad řádků v Javě |
|---|---|---|---|
| Data class | data class Zapis(val mesto: String, val teplota: Double, val poznamka: String = "") | Podpisy jsou vypsané pod tabulkou. | 60–80 |
| Object | object Prevody s konstantou a funkcí | Podpisy jsou vypsané pod tabulkou. | 10–15 |
| Extension funkce | fun Double.zaokrouhlenoNaDesetiny(): Double | public static final double zaokrouhlenoNaDesetiny(double $this$zaokrouhlenoNaDesetiny) | 3 |
| Funkce jako parametr | fun prumer(zapisy: List<Zapis>, vyber: (Zapis) -> Double): Double | public static final double prumer(List zapisy, Function1 vyber) | 10–15 |
| Suspend funkce | suspend fun nactiPoZdrzeni(zapisy: List<Zapis>): List<Zapis> | public static final Object nactiPoZdrzeni(List zapisy, Continuation $completion) | 50–70 pro podobný stavový automat |

# Metody třídy Zapis:

public final String getMesto()

public final double getTeplota()

public final String getPoznamka()

public final String component1()

public final double component2()

public final String component3()

public final Zapis copy(String mesto, double teplota, String poznamka)

public static Zapis copy$default(Zapis var0, String var1, double var2, String var4, int var5, Object var6)

public String toString()

public int hashCode()

public boolean equals(Object other)

# Členy třídy Prevody:

public static final Prevody INSTANCE

private Prevody()

public static final double ABSOLUTNI_NULA

public final double naFahrenheita(double celsia)

Poslední tři metody z tabulky jsou ve vygenerované třídě PitvaKt.

# 1. Kolik metod má třída Zapis?

Třída má 11 metod a zvlášť dva konstruktory. Jsou to tři gettery, component1 až component3, copy, copy$default, toString, hashCode a equals. Gettery vycházejí z vlastností val, ostatní metody kompilátor doplnil díky data class, aniž bych je sám napsal.

# 2. Proč má Zapis konstruktor navíc?

Je tam kvůli výchozí hodnotě poznamka = "". Parametr int je bitová maska, podle které se pozná, jestli se má použít výchozí hodnota. DefaultConstructorMarker je pomocný typ kompilátoru pro tento konstruktor.

# 3. Jaký je první parametr extension funkce a je polymorfní?

První parametr je double $this$zaokrouhlenoNaDesetiny, tedy číslo, na kterém funkci volám. V Javě jde o statickou metodu, takže se extension vybírá podle typu známého při překladu, ne podle skutečného typu objektu za běhu. Její výběr tedy není polymorfní.

# 4. Jaký typ má druhý parametr funkce prumer?

Parametr vyber má typ Function1, což je rozhraní pro funkci s jedním argumentem. Funkce je tedy předaná jako objekt a v dekompilátu se volá přes vyber.invoke(...).

# 5. Proč má suspend funkce návratový typ Object a switch?

Návratový typ se změnil na Object, protože metoda může vrátit seznam nebo značku COROUTINE_SUSPENDED při pozastavení. Přibyl parametr Continuation, který umožňuje pokračovat po pozastavení. Switch podle hodnoty label rozlišuje první spuštění a pokračování po delay, takže funkce funguje jako stavový automat.