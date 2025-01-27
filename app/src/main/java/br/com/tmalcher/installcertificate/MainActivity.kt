package br.com.tmalcher.installcertificate

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.tmalcher.certinstall.ui.main.CertificateManagerScreen

class MainActivity : ComponentActivity() {
    private val logMessages = mutableStateListOf<String>()
    private val REQUEST_CODE_STORAGE_PERMISSION = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CertificateManagerScreen(
                    onInstallCertificate = { installCertificate() },
                    onUninstallCertificate = { uninstallCertificate() },
                    logMessages = logMessages
                )
            }
        }

        // Verificar se a permissão de leitura e escrita no armazenamento foi concedida
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                // Solicitar as permissões
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
            } else {
                // Permissões já concedidas, prosseguir com a lógica do app
                proceedWithAppLogic()
            }
        } else {
            // Em versões abaixo de Android 6.0, as permissões são concedidas durante a instalação
            proceedWithAppLogic()
        }
    }

    private fun installCertificate() {

    }

    private fun uninstallCertificate() {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions as Array<String>, grantResults)

        when (requestCode) {
            REQUEST_CODE_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Permissões concedidas, prosseguir com a lógica do app
                    proceedWithAppLogic()
                } else {
                    // Permissões negadas, informar ao usuário
                    showPermissionDeniedMessage()
                }
            }
        }
    }

    private fun proceedWithAppLogic() {
        // A lógica que será executada após as permissões serem concedidas
        // Por exemplo, copiar o arquivo para o diretório de Downloads
    }

    private fun showPermissionDeniedMessage() {
        // Exibir mensagem caso o usuário negue as permissões
        Toast.makeText(this, "As permissões foram negadas. O app não pode continuar.", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
