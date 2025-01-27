package br.com.tmalcher.certinstall.ui.main

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import br.com.tmalcher.installcertificate.R;

@Composable
fun CertificateManagerScreen(
    onInstallCertificate: () -> Unit,
    onUninstallCertificate: () -> Unit,
    logMessages: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.certificates_management),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = onInstallCertificate) {
                Text(text = stringResource(id = R.string.btn_install))
            }
            Button(onClick = onUninstallCertificate) {
                Text(text = stringResource(id = R.string.btn_uninstall))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.log_process),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.Gray)
                .padding(8.dp)
        ) {
            if (logMessages.isEmpty()) {
                Text(text = stringResource(id = R.string.log_not), color = Color.Gray)
            } else {
                LazyColumn {
                    items(logMessages) { log ->
                        Text(text = log, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCertificateManagerScreen() {
    MaterialTheme {
        CertificateManagerScreen(
            onInstallCertificate = { /* No-op */ },
            onUninstallCertificate = { /* No-op */ },
            logMessages = listOf(
                stringResource(id = R.string.log_msg_install),
                stringResource(id = R.string.log_msg_install_success),
                stringResource(id = R.string.log_msg_uninstall),
                stringResource(id = R.string.log_msg_uninstall_success)
            )
        )
    }
}