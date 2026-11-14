package cz.utb.meteodenik.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import cz.utb.meteodenik.R

private const val OPRAVNENI_POLOHA =
    Manifest.permission.ACCESS_COARSE_LOCATION

enum class StavOpravneni {
    NEZNAMY,
    ODMITNUTO,
    ODMITNUTO_NATRVALO,
    UDELENO
}

private fun nactiPosledniPolohu(context: Context): Location? {

    // Oprávnění kontrolujeme před každým čtením polohy.
    if (
        ContextCompat.checkSelfPermission(
            context,
            OPRAVNENI_POLOHA
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return null
    }

    return try {
        val locationManager =
            context.getSystemService(LocationManager::class.java)
                ?: return null

        locationManager.getLastKnownLocation(
            LocationManager.NETWORK_PROVIDER
        )

    } catch (e: SecurityException) {
        Log.w("Poloha", "Chybí oprávnění k poloze", e)
        null

    } catch (e: IllegalArgumentException) {
        Log.w("Poloha", "Poskytovatel polohy není dostupný", e)
        null
    }
}

@Composable
fun PolohaKarta(onPolohaPovolena: () -> Unit) {

    val context = LocalContext.current
    val activity = LocalActivity.current

    var stav by remember {
        mutableStateOf(
            if (
                ContextCompat.checkSelfPermission(
                    context,
                    OPRAVNENI_POLOHA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                StavOpravneni.UDELENO
            } else {
                StavOpravneni.NEZNAMY
            }
        )
    }

    var zobrazitVysvetleni by remember {
        mutableStateOf(false)
    }

    var posledniPoloha by remember {
        mutableStateOf<Location?>(null)
    }

    // Spouštěč systémového dialogu.
    val spoustec = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { povoleno ->

        stav = when {
            povoleno -> StavOpravneni.UDELENO

            activity != null &&
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        OPRAVNENI_POLOHA
                    ) -> StavOpravneni.ODMITNUTO

            else -> StavOpravneni.ODMITNUTO_NATRVALO
        }

        if (povoleno) {
            onPolohaPovolena()
        } else {
            posledniPoloha = null
        }
    }

    // Načtení souřadnic, pokud máme oprávnění.
    LaunchedEffect(stav) {
        if (stav == StavOpravneni.UDELENO) {
            posledniPoloha = nactiPosledniPolohu(context)
        }
    }

    // Zachováme aktuální callback i při rekompozici.
    val aktualniOnPolohaPovolena by rememberUpdatedState(
        onPolohaPovolena
    )

    val lifecycle = (activity as? LifecycleOwner)?.lifecycle

    // Při návratu z Nastavení znovu zkontrolujeme oprávnění.
    DisposableEffect(lifecycle) {

        val pozorovatel = LifecycleEventObserver { _, udalost ->

            if (udalost == Lifecycle.Event.ON_RESUME) {

                val povoleno =
                    ContextCompat.checkSelfPermission(
                        context,
                        OPRAVNENI_POLOHA
                    ) == PackageManager.PERMISSION_GRANTED

                if (povoleno) {
                    if (stav != StavOpravneni.UDELENO) {
                        stav = StavOpravneni.UDELENO
                        aktualniOnPolohaPovolena()
                    }

                    posledniPoloha = nactiPosledniPolohu(context)

                } else if (stav == StavOpravneni.UDELENO) {
                    stav = StavOpravneni.NEZNAMY
                    posledniPoloha = null
                }
            }
        }

        lifecycle?.addObserver(pozorovatel)

        onDispose {
            lifecycle?.removeObserver(pozorovatel)
        }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        when (stav) {

            StavOpravneni.UDELENO -> {

                Text(
                    text = stringResource(
                        R.string.poloha_povolena
                    )
                )

                val poloha = posledniPoloha

                if (poloha != null) {

                    Text(
                        text = stringResource(
                            R.string.poloha_souradnice,
                            poloha.latitude,
                            poloha.longitude
                        )
                    )

                } else {

                    Text(
                        text = stringResource(
                            R.string.poloha_nedostupna
                        )
                    )
                }
            }

            StavOpravneni.ODMITNUTO_NATRVALO -> {

                Text(
                    text = stringResource(
                        R.string.poloha_bez_opravneni
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts(
                                    "package",
                                    context.packageName,
                                    null
                                )
                            )
                        )
                    }
                ) {
                    Text(
                        text = stringResource(
                            R.string.otevrit_nastaveni
                        )
                    )
                }
            }

            StavOpravneni.ODMITNUTO,
            StavOpravneni.NEZNAMY -> {

                if (zobrazitVysvetleni) {

                    Text(
                        text = stringResource(
                            R.string.poloha_vysvetleni
                        )
                    )

                    Button(
                        onClick = {
                            zobrazitVysvetleni = false
                            spoustec.launch(OPRAVNENI_POLOHA)
                        }
                    ) {
                        Text(
                            text = stringResource(
                                R.string.poloha_pokracovat
                            )
                        )
                    }

                } else {

                    if (stav == StavOpravneni.ODMITNUTO) {
                        Text(
                            text = stringResource(
                                R.string.poloha_odmitnuta
                            )
                        )
                    }

                    Button(
                        onClick = {

                            val potrebaVysvetlit =
                                activity != null &&
                                        ActivityCompat
                                            .shouldShowRequestPermissionRationale(
                                                activity,
                                                OPRAVNENI_POLOHA
                                            )

                            if (potrebaVysvetlit) {
                                zobrazitVysvetleni = true
                            } else {
                                spoustec.launch(OPRAVNENI_POLOHA)
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(
                                R.string.poloha_zjistit
                            )
                        )
                    }
                }
            }
        }
    }
}
