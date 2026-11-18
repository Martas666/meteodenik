package cz.utb.meteodenik.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cz.utb.meteodenik.R

private const val TAG = "Cyklus"

@Composable
fun DetailObrazovka(
    mesto: String,
    teplota: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (teplota.isNaN()) {
            Text(
                text = mesto,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = stringResource(R.string.teplota_chybi)
            )
        } else {
            Text(
                text = stringResource(
                    R.string.detail_souhrn,
                    mesto,
                    teplota
                ),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        SdiletTlacitko(
            mesto = mesto,
            teplota = teplota
        )
    }
}

@Composable
fun SdiletTlacitko(
    mesto: String,
    teplota: Double
) {
    val context = LocalContext.current

    val text = if (teplota.isNaN()) {
        stringResource(
            R.string.sdileny_text_bez_teploty,
            mesto
        )
    } else {
        stringResource(
            R.string.sdileny_text,
            mesto,
            teplota
        )
    }

    Button(
        onClick = {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }

            try {
                context.startActivity(
                    Intent.createChooser(sendIntent, null)
                )
            } catch (e: ActivityNotFoundException) {
                Log.w(
                    TAG,
                    "Není dostupná aplikace pro sdílení",
                    e
                )
            }
        }
    ) {
        Text(
            text = stringResource(R.string.sdilet_zaznam)
        )
    }
}
