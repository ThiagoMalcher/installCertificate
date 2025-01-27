package br.com.tmalcher.installcertificate

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.SyncStateContract.Helpers
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.tmalcher.certinstall.ui.main.CertificateManagerScreen
import br.com.tmalcher.installcertificate.cert.Install
import br.com.tmalcher.installcertificate.cert.Uninstall
import br.com.tmalcher.installcertificate.utils.Consts.Companion.DELETE_FAILED_INTERNAL_ERROR
import br.com.tmalcher.installcertificate.utils.Consts.Companion.DELETE_SUCCEEDED
import br.com.tmalcher.installcertificate.utils.Consts.Companion.INSTALL_FAILED_INTERNAL_ERROR
import br.com.tmalcher.installcertificate.utils.Consts.Companion.INSTALL_SUCCEEDED
import java.io.File


class MainActivity : ComponentActivity() {
    private val logMessages = mutableStateListOf<String>()
    private val REQUEST_CODE_STORAGE_PERMISSION = 100

    private var mHelpers: br.com.tmalcher.certinstall.utils.Helpers? = null

    private var mInstaller: Install? = null
    private var mUninstaller: Uninstall? = null
    private val TAG = this.javaClass.simpleName

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

        mHelpers = br.com.tmalcher.certinstall.utils.Helpers()
        mInstaller = Install()
        mUninstaller = Uninstall()

        // Verificar se a permissão de leitura e escrita no armazenamento foi concedida
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
            } else {
                proceedWithAppLogic()
            }
        } else {
            proceedWithAppLogic()
        }
    }

    private fun installCertificate() {
        logMessages.clear()
        logMessages.add("Iniciando instalação")
        val certs = mHelpers?.getCertFilesInDownloads()
        if (!certs.isNullOrEmpty()) {
            for (cert in certs) {
                mInstaller?.run(this, cert.first, cert.second) { result ->
                    when (result) {
                        INSTALL_SUCCEEDED -> {
                            logMessages.add("Instalação bem-sucedida!")
                            val file = File(cert.first + cert.second).absolutePath.toString()
                            val resultInfo = mHelpers?.getCertificateInfo(file)
                            if (resultInfo != null) {
                                for (lines in resultInfo) {
                                    logMessages.add(lines)
                                }
                            }
                        }

                        INSTALL_FAILED_INTERNAL_ERROR -> {
                            logMessages.add("Falha na instalação.")
                        }

                        else -> {
                            logMessages.add("Erro desconhecido.")
                        }
                    }
                }

            }
        } else {
            logMessages.add("Nenhum certificado encontrado.")
        }

    }

    private fun uninstallCertificate() {
        logMessages.clear()
        val certs = mHelpers?.getCertFilesInDownloads()
        if (!certs.isNullOrEmpty()) {
            for (cert in certs) {
                mUninstaller?.run(this, cert.first, cert.second) { result ->
                    when (result) {
                        DELETE_SUCCEEDED -> {
                            logMessages.add("Remoção do certificado bem-sucedida!")
                            val file = File(cert.first + cert.second).absolutePath.toString()
                            val resultInfo = mHelpers?.getCertificateInfo(file)
                            if (resultInfo != null) {
                                for (lines in resultInfo) {
                                    logMessages.add(lines)
                                }
                            }
                        }

                        DELETE_FAILED_INTERNAL_ERROR -> {
                            logMessages.add("Falha na desinstalação.")
                        }

                        else -> {
                            logMessages.add("Erro desconhecido.")
                        }
                    }
                }

            }
        } else {
            logMessages.add("Nenhum certificado encontrado.")
        }
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
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    proceedWithAppLogic()
                } else {
                    showPermissionDeniedMessage()
                }
            }
        }
    }

    private fun proceedWithAppLogic() {
        mHelpers?.copyAllCerAssetsToDownloads(this)
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            this,
            "As permissões foram negadas. O app não pode continuar.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
