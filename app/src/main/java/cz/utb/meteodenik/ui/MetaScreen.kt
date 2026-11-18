
package cz.utb.meteodenik.ui

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.utb.meteodenik.DetailMestaActivity
import cz.utb.meteodenik.R
import cz.utb.meteodenik.ZaznamUlozenReceiver
import cz.utb.meteodenik.model.Mesto
import cz.utb.meteodenik.ui.theme.MeteoDenikTheme

// Ukázková města z předchozích lekcí.
internal val ukazkovaMesta = listOf(
    Mesto(id = 1, nazev = "Zlín", teplotaC = 21.4),
    Mesto(id = 2, nazev = "Brno", teplotaC = 23.1),
    Mesto(id = 3, nazev = "Ostrava", teplotaC = 19.8)
)

// Karta města.
// Nově přijímá také callback onSmazat.
@Composable
fun MestoKarta(
    mesto: Mesto,
    onOtevrit: () -> Unit,
    onSmazat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val popisAvatara = stringResource(
        R.string.mesto_avatar_popis
    )

    val popisSmazani = stringResource(
        R.string.mesto_smazat_popis,
        mesto.nazev
    )

    Card(
        onClick = onOtevrit,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                    .semantics {
                        contentDescription =
                            "$popisAvatara: ${mesto.nazev}"
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mesto.nazev.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mesto.nazev,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val teplota = mesto.teplotaC

                Text(
                    text = if (teplota != null) {
                        stringResource(
                            R.string.mesto_teplota,
                            teplota
                        )
                    } else {
                        stringResource(
                            R.string.mesto_teplota_neznama
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Nové tlačítko – cvičení 7.2.
            // Samotná karta neřeší, jak se město smaže.
            TextButton(
                onClick = onSmazat,
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = 48.dp,
                        minHeight = 48.dp
                    )
                    .semantics {
                        contentDescription = popisSmazani
                    }
            ) {
                Text(
                    text = stringResource(R.string.mesto_smazat)
                )
            }
        }
    }
}

// Formulář pro přidání města.
// Stav si neukládá, pouze dostává hodnoty od rodiče.
@Composable
fun PridatMestoRadek(
    nazev: String,
    jeChyba: Boolean,
    onNazevChange: (String) -> Unit,
    onPridat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            value = nazev,
            onValueChange = onNazevChange,
            label = {
                Text(
                    text = stringResource(
                        R.string.mesto_pole_label
                    )
                )
            },
            isError = jeChyba,
            supportingText = if (jeChyba) {
                {
                    Text(
                        text = stringResource(
                            R.string.mesto_duplikat
                        )
                    )
                }
            } else {
                null
            },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = onPridat,
            enabled = nazev.isNotBlank()
        ) {
            Text(
                text = stringResource(R.string.mesto_pridat)
            )
        }
    }
}

// Seznam měst.
// Nově přijímá onSmazatMesto a předává ID vybrané karty.
@Composable
fun MestaSeznam(
    mesta: List<Mesto>,
    onOtevritMesto: (Mesto) -> Unit,
    onSmazatMesto: (Int) -> Unit,
    onZapsatZaznam: () -> Unit,
    modifier: Modifier = Modifier,
    polohaObsah: @Composable () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // Vizitka ze cvičení 7.1.
        item(key = "vizitka") {
            VizitkaKarta(
                jmeno = stringResource(R.string.vizitka_jmeno),
                obor = stringResource(R.string.vizitka_obor),
                email = stringResource(R.string.vizitka_email)
            )
        }

        // Poloha z lekce 06.
        item(key = "poloha") {
            polohaObsah()
        }

        // Nový plurál pro počet měst.
        item(key = "pocet") {
            Text(
                text = pluralStringResource(
                    R.plurals.mesta_pocet,
                    mesta.size,
                    mesta.size
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (mesta.isEmpty()) {
            item(key = "prazdno") {
                Text(
                    text = stringResource(R.string.mesta_prazdno),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        }

        items(
            items = mesta,
            key = { mesto -> mesto.id }
        ) { mesto ->

            MestoKarta(
                mesto = mesto,
                onOtevrit = {
                    onOtevritMesto(mesto)
                },
                onSmazat = {
                    onSmazatMesto(mesto.id)
                }
            )
        }

        // BroadcastReceiver z lekce 05.
        item(key = "broadcast") {
            Button(
                onClick = onZapsatZaznam,
                enabled = mesta.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.poslat_zpravu)
                )
            }
        }
    }
}

// Bezstavový obsah obrazovky.
// Všechny hodnoty a akce dostává přes parametry.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MestaObsah(
    mesta: List<Mesto>,
    novyNazev: String,
    jeDuplikat: Boolean,
    onNovyNazevChange: (String) -> Unit,
    onPridatMesto: () -> Unit,
    onOtevritMesto: (Mesto) -> Unit,
    onSmazatMesto: (Int) -> Unit,
    onZapsatZaznam: () -> Unit,
    modifier: Modifier = Modifier,
    polohaObsah: @Composable () -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.mesta_titulek)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor =
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {

            PridatMestoRadek(
                nazev = novyNazev,
                jeChyba = jeDuplikat,
                onNazevChange = onNovyNazevChange,
                onPridat = onPridatMesto
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp)
            )

            MestaSeznam(
                mesta = mesta,
                onOtevritMesto = onOtevritMesto,
                onSmazatMesto = onSmazatMesto,
                onZapsatZaznam = onZapsatZaznam,
                modifier = Modifier.weight(1f),
                polohaObsah = polohaObsah
            )
        }
    }
}

@Composable
fun MestaObrazovka(modifier: Modifier = Modifier) {

    val context = LocalContext.current

    var novyNazev by rememberSaveable {
        mutableStateOf("")
    }

    var mesta by remember {
        mutableStateOf(ukazkovaMesta)
    }

    // Nový stav – zda už dané město existuje.
    var jeDuplikat by remember {
        mutableStateOf(false)
    }

    MestaObsah(
        mesta = mesta,
        novyNazev = novyNazev,
        jeDuplikat = jeDuplikat,

        // Každá změna vstupu zruší původní chybu.
        onNovyNazevChange = { novyText ->
            novyNazev = novyText
            jeDuplikat = false
        },

        // Přidání města a kontrola duplicit.
        onPridatMesto = {

            val nazev = novyNazev.trim()

            if (nazev.isNotEmpty()) {

                val existuje = mesta.any { mesto ->
                    mesto.nazev.trim().equals(
                        nazev,
                        ignoreCase = true
                    )
                }

                if (existuje) {
                    jeDuplikat = true

                } else {
                    val noveId =
                        (mesta.maxOfOrNull { it.id } ?: 0) + 1

                    mesta = mesta + Mesto(
                        id = noveId,
                        nazev = nazev,
                        teplotaC = null
                    )

                    novyNazev = ""
                    jeDuplikat = false
                }
            }
        },

        onOtevritMesto = { mesto ->

            val intent = Intent(
                context,
                DetailMestaActivity::class.java
            ).apply {
                putExtra(
                    DetailMestaActivity.EXTRA_MESTO,
                    mesto.nazev
                )

                putExtra(
                    DetailMestaActivity.EXTRA_TEPLOTA,
                    mesto.teplotaC ?: Double.NaN
                )
            }

            context.startActivity(intent)
        },

        // Mazání města podle jeho ID.
        onSmazatMesto = { id ->

            mesta = mesta.filterNot { mesto ->
                mesto.id == id
            }

            jeDuplikat = false
        },

        onZapsatZaznam = {

            val prvniMesto = mesta.firstOrNull()

            if (prvniMesto != null) {

                val intent = Intent(
                    ZaznamUlozenReceiver.ACTION_ZAZNAM_ULOZEN
                ).apply {
                    setPackage(context.packageName)

                    putExtra(
                        ZaznamUlozenReceiver.EXTRA_MESTO,
                        prvniMesto.nazev
                    )
                }

                context.sendBroadcast(intent)
            }
        },

        modifier = modifier,

        polohaObsah = {
            PolohaKarta(onPolohaPovolena = {})
        }
    )
}

// Náhled karty – světlý a tmavý.
@Preview(
    name = "Karta – světlá",
    showBackground = true
)
@Preview(
    name = "Karta – tmavá",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun MestoKartaPreview() {
    MeteoDenikTheme {
        MestoKarta(
            mesto = ukazkovaMesta.first(),
            onOtevrit = {},
            onSmazat = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

// Náhled karty bez známé teploty.
@Preview(
    name = "Neznámá teplota",
    showBackground = true
)
@Composable
private fun MestoBezTeplotyPreview() {
    MeteoDenikTheme {
        MestoKarta(
            mesto = Mesto(
                id = 4,
                nazev = "Uherské Hradiště",
                teplotaC = null
            ),
            onOtevrit = {},
            onSmazat = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

// Náhled celé obrazovky – světlý a tmavý.
@Preview(
    name = "Obrazovka – světlá",
    showBackground = true
)
@Preview(
    name = "Obrazovka – tmavá",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun MestaObsahPreview() {
    MeteoDenikTheme {
        MestaObsah(
            mesta = ukazkovaMesta,
            novyNazev = "Otrokovice",
            jeDuplikat = false,
            onNovyNazevChange = {},
            onPridatMesto = {},
            onOtevritMesto = {},
            onSmazatMesto = {},
            onZapsatZaznam = {}
        )
    }
}

// Náhled prázdného seznamu.
@Preview(
    name = "Prázdný seznam",
    showBackground = true
)
@Composable
private fun MestaObsahPrazdnaPreview() {
    MeteoDenikTheme {
        MestaObsah(
            mesta = emptyList(),
            novyNazev = "",
            jeDuplikat = false,
            onNovyNazevChange = {},
            onPridatMesto = {},
            onOtevritMesto = {},
            onSmazatMesto = {},
            onZapsatZaznam = {}
        )
    }
}

// NOVÉ – náhled duplicitního města.
@Preview(
    name = "Chyba – duplicitní město",
    showBackground = true
)
@Composable
private fun MestaObsahChybaPreview() {
    MeteoDenikTheme {
        MestaObsah(
            mesta = ukazkovaMesta,
            novyNazev = "Brno",
            jeDuplikat = true,
            onNovyNazevChange = {},
            onPridatMesto = {},
            onOtevritMesto = {},
            onSmazatMesto = {},
            onZapsatZaznam = {}
        )
    }
}
