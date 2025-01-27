package br.com.tmalcher.certinstall.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class Helpers {

    fun getCertFilesInDownloads(): List<Pair<String, String>> {
        val certFiles = mutableListOf<Pair<String, String>>()

        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        if (downloadsDirectory.exists() && downloadsDirectory.isDirectory) {
            val filesInDownloads = downloadsDirectory.listFiles() ?: arrayOf()

            for (file in filesInDownloads) {
                if (file.isFile && file.name.endsWith(".cer", ignoreCase = true)) {
                    certFiles.add(Pair(file.absolutePath.replace(file.name, ""), file.name))
                }
            }
        }

        return certFiles
    }

    fun getCertificateInfo(certificatePath: String): Array<String> {
        val certificateInfo = Array(4) { "" }
        try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val inputStream = FileInputStream(certificatePath)
            val certificate = certificateFactory.generateCertificate(inputStream) as X509Certificate

            certificateInfo[0] = certificate.subjectDN.name
            certificateInfo[1] = certificate.issuerDN.name
            certificateInfo[2] = certificate.notBefore.toString()
            certificateInfo[3] = certificate.notAfter.toString()

            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return certificateInfo
    }

    fun copyAllCerAssetsToDownloads(context: Context) {
        val assetManager = context.assets
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            val certFolderPath = "certs"
            val filesInAssets = assetManager.list(certFolderPath) ?: emptyArray()

            val downloadsDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            for (fileName in filesInAssets) {
                if (fileName.endsWith(".cer", ignoreCase = true)) {
                    val assetPath = "$certFolderPath/$fileName"
                    inputStream = assetManager.open(assetPath)

                    val outFile = File(downloadsDirectory, fileName)

                    outputStream = FileOutputStream(outFile)

                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }

                    println("Arquivo copiado para: ${outFile.absolutePath}")

                    inputStream.close()
                    outputStream.flush()
                    outputStream.close()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}