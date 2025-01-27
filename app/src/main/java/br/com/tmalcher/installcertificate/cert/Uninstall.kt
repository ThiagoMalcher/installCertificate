package br.com.tmalcher.installcertificate.cert

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.security.KeyChain
import android.util.Log
import br.com.tmalcher.installcertificate.utils.Consts.Companion.DELETE_FAILED_INTERNAL_ERROR
import br.com.tmalcher.installcertificate.utils.Consts.Companion.DELETE_SUCCEEDED
import br.com.tmalcher.installcertificate.utils.Consts.Companion.DELETE_WAIT
import br.com.tmalcher.installcertificate.utils.Consts.Companion.INSTALL_FAILED_INTERNAL_ERROR
import br.com.tmalcher.installcertificate.utils.Consts.Companion.INSTALL_SUCCEEDED
import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class Uninstall() {

    private val TAG = this.javaClass.simpleName

    /**
     * Function that removes a certificate from the Android KeyChain.
     *
     * This function receives the path and filename of a certificate file, checks if the file has the
     * `.cer` extension, and attempts to remove it from the device's KeyChain using private KeyChain
     * classes in Android. If an error occurs, the callback is invoked with an error code.
     *
     * @param context The application context. Used to perform system-related operations and interact with
     *                the KeyChain.
     * @param filePath The directory path where the certificate file is located.
     * @param fileName The name of the certificate file. The file must have a `.cer` extension.
     * @param callback A callback function that will be called after the attempt to remove the certificate.
     *                 It receives an integer code representing the status of the operation.
     *
     * @return An integer code representing the status of the operation. It may return:
     *         - `DELETE_WAIT` if the operation has started and is waiting.
     *         - `DELETE_FAILED_INTERNAL_ERROR` if an internal error occurred during validation or execution.
     *         - `DELETE_SUCCEEDED` if the removal was successful.
     */

    fun run(context: Context, filePath: String, fileName: String, callback: (Int) -> Unit): Int {
        if (!fileName.contains(".cer")) {
            Log.d(TAG, "SÓ ACEITO CERTIFICADO COM EXTENSÃO .CER!")
            return DELETE_FAILED_INTERNAL_ERROR
        }

        var realAlias: String? = null

        try {
            val keyStore = KeyStore.getInstance("AndroidCAStore").apply {
                load(null)
            }

            val fileNameWithExtension = fileName
            val fileIn = File(filePath, fileNameWithExtension)
            val certFilePath = fileIn.absolutePath
            val certFile = FileInputStream(certFilePath)
            val certFactory = CertificateFactory.getInstance("X.509")
            val cert = certFactory.generateCertificate(certFile) as X509Certificate
            realAlias = keyStore.getCertificateAlias(cert)

        } catch (e: Exception) {
            Log.d("Uninstall", e.message.orEmpty(), e)
        }

        val handler = Handler(Looper.getMainLooper())
        AsyncTask.execute {
            try {
                val keyChainConnectionClass =
                    context.classLoader.loadClass("android.security.KeyChain\$KeyChainConnection")
                val iKeyChainServiceClass =
                    context.classLoader.loadClass("android.security.IKeyChainService")

                val keyChainBindMethod = KeyChain::class.java.getMethod("bind", Context::class.java)
                val keyChainConnectionGetServiceMethod =
                    keyChainConnectionClass.getMethod("getService")

                val keyChainConnectionObject = keyChainBindMethod.invoke(null, context)
                val iKeyChainServiceObject =
                    keyChainConnectionGetServiceMethod.invoke(keyChainConnectionObject)

                @SuppressLint("SoonBlockedPrivateApi") val deleteCaCertificateMethod =
                    iKeyChainServiceClass.getDeclaredMethod(
                        "deleteCaCertificate", String::class.java
                    )
                deleteCaCertificateMethod.invoke(iKeyChainServiceObject, realAlias)
                // Sucesso
                handler.post {
                    callback(DELETE_SUCCEEDED)
                }
            } catch (e: Exception) {
                Log.e("KeyChainError", "Failed to install certificate", e)
                handler.post {
                    callback(INSTALL_FAILED_INTERNAL_ERROR)
                }
            }
        }
        return DELETE_WAIT
    }

}