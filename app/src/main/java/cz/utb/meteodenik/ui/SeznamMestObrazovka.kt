package cz.utb.meteodenik.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cz.utb.meteodenik.DetailMestaActivity
import cz.utb.meteodenik.R
import cz.utb.meteodenik.ZaznamUlozenReceiver
import cz.utb.meteodenik.model.UKAZKOVA_MESTA

@Composable
fun SeznamMestObrazovka(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val pocet = UKAZKOVA_MESTA.size

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(
                R.string.seznam_titulek_pocet,
                pluralStringResource(
                    R.plurals.pocet_zaznamu,
                    pocet,
                    pocet
                )
            ),
            style = MaterialTheme.typography.headlineSmall
        )

        PolohaKarta(onPolohaPovolena = {})

        UKAZKOVA_MESTA.forEach { mesto ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        colorResource(R.color.pozadi_karty)
                    )
                    .clickable {
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
                                mesto.teplota
                            )
                        }

                        context.startActivity(intent)
                    }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = mesto.nazev,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(
                        R.string.teplota_format,
                        mesto.teplota
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Button(
            onClick = {
                val intent = Intent(
                    ZaznamUlozenReceiver.ACTION_ZAZNAM_ULOZEN
                ).apply {
                    setPackage(context.packageName)

                    putExtra(
                        ZaznamUlozenReceiver.EXTRA_MESTO,
                        UKAZKOVA_MESTA.first().nazev
                    )
                }

                context.sendBroadcast(intent)
            }
        ) {
            Text(
                text = stringResource(R.string.poslat_zpravu)
            )
        }
    }
}
