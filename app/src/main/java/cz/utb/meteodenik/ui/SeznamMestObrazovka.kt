package cz.utb.meteodenik.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cz.utb.meteodenik.DetailMestaActivity
import cz.utb.meteodenik.R
import cz.utb.meteodenik.ZaznamUlozenReceiver

@Composable
fun SeznamMestObrazovka(modifier: Modifier = Modifier) {

    val context = LocalContext.current

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = stringResource(R.string.seznam_mest))

        Text(text = "Zlín")

        Text(text = stringResource(R.string.teplota_format, 21.4))

        Button(
            onClick = {
                val intent = Intent(
                    context,
                    DetailMestaActivity::class.java
                ).apply {
                    putExtra(DetailMestaActivity.EXTRA_MESTO, "Zlín")
                    putExtra(DetailMestaActivity.EXTRA_TEPLOTA, 21.4)
                }

                context.startActivity(intent)
            }
        ) {
            Text(text = stringResource(R.string.otevri_detail))
        }

        Button(
            onClick = {
                val intent = Intent(
                    ZaznamUlozenReceiver.ACTION_ZAZNAM_ULOZEN
                ).apply {
                    setPackage(context.packageName)
                    putExtra(ZaznamUlozenReceiver.EXTRA_MESTO, "Zlín")
                }

                context.sendBroadcast(intent)
            }
        ) {
            Text(text = stringResource(R.string.poslat_zpravu))
        }
    }
}
