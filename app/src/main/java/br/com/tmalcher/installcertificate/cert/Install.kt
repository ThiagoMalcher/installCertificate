package br.com.tmalcher.installcertificate.cert

import android.content.Context
import android.nfc.Tag
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.security.KeyChain
import android.util.Log
import br.com.tmalcher.installcertificate.utils.Consts.Companion.INSTALL_FAILED_INTERNAL_ERROR
import br.com.tmalcher.installcertificate.utils.Consts.Companion.INSTALL_SUCCEEDED
import br.com.tmalcher.installcertificate.utils.Consts.Companion.INSTALL_WAIT
import java.io.File
import java.io.RandomAccessFile

class Install() {

    private val TAG = this.javaClass.simpleName

    /**
     * Installs certification authority from a given file.
     *
     * This function reads a certificate file, processes its contents, and installs it as a
     * system certification authority using Android's internal KeyChain and IKeyChainService classes.
     * It dynamically loads the required classes and methods to perform the installation.
     *
     * @param filePath The absolute path to the directory containing the certificate file.
     * @param fileName The name of the certificate file (without extension) to be installed. The function appends `.cer` as the file extension.
     * @return An integer indicating the result of the operation:
     * - [INSTALL_SUCCEEDED] if the certificate was successfully installed.
     * - [INSTALL_FAILED_INTERNAL_ERROR] if an error occurred during the installation process.
     *
     * @throws java.lang.ClassNotFoundException If the required KeyChain or IKeyChainService classes cannot be loaded.
     * @throws java.lang.NoSuchMethodException If a required method is not found in the dynamically loaded classes.
     * @throws java.lang.reflect.InvocationTargetException If there is an exception while invoking a method.
     * @throws java.lang.IllegalAccessException If the program does not have access to the method being invoked.
     * @throws java.io.IOException If an I/O error occurs while reading the certificate file.
     */

    fun run(context: Context, filePath: String, fileName: String, callback: (Int) -> Unit): Int {
        if (!fileName.contains(".cer")) {
            Log.d(TAG, "SÓ ACEITO CERTIFICADO COM EXTENSÃO .CER!")
            callback(INSTALL_FAILED_INTERNAL_ERROR)
            return INSTALL_FAILED_INTERNAL_ERROR
        }

        val fileNameWithExtension = fileName
        val fileIn = File(filePath, fileNameWithExtension)

        val handler = Handler(Looper.getMainLooper())

        AsyncTask.execute {
            try {
                val file = RandomAccessFile(fileIn, "r")
                val certificateBytes = ByteArray(file.length().toInt())
                file.read(certificateBytes)

                val keyChainConnectionClass =
                    context.classLoader.loadClass("android.security.KeyChain\$KeyChainConnection")
                val iKeyChainServiceClass =
                    context.classLoader.loadClass("android.security.IKeyChainService")

                val keyChainBindMethod = KeyChain::class.java.getMethod("bind", Context::class.java)
                val keyChainConnectionGetServiceMethod = keyChainConnectionClass.getMethod("getService")

                val keyChainConnectionObject = keyChainBindMethod.invoke(null, context)
                val iKeyChainServiceObject =
                    keyChainConnectionGetServiceMethod.invoke(keyChainConnectionObject)

                val installCaCertificateMethod = iKeyChainServiceClass.getDeclaredMethod(
                    "installCaCertificate",
                    ByteArray::class.java
                )
                installCaCertificateMethod.invoke(iKeyChainServiceObject, certificateBytes)

                // Sucesso
                handler.post {
                    callback(INSTALL_SUCCEEDED)
                }
            } catch (e: Exception) {
                Log.e("KeyChainError", "Failed to install certificate", e)
                handler.post {
                    callback(INSTALL_FAILED_INTERNAL_ERROR)
                }
            }
        }

        return INSTALL_WAIT
    }

}