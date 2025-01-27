# App para Instalar Certificados no Android

Este repositório contém um aplicativo Android desenvolvido para instalar certificados em dispositivos Android. O certificado utilizado foi gerado com os seguintes comandos:

## Gerando o Certificado

O certificado foi criado usando o comando `keytool` do Java, com os seguintes parâmetros:

### Gerar o Certificado no Keystore:

keytool -genkeypair -alias teste_certificado \
  -keyalg RSA -keysize 2048 -validity 365 \
  -keystore teste.keystore \
  -dname "CN=localhost, OU=Desenvolvimento, O=Teste, L=Manaus, ST=Amazonas, C=BR"

* Alias: teste_certificado
* Algoritmo: RSA
* Tamanho da chave: 2048 bits
* Validade: 365 dias
* Keystore: teste.keystore

Exportar o Certificado:

keytool -exportcert -alias teste_certificado \
  -file teste.cer -keystore teste.keystore \
  -rfc

* Certificado exportado: teste.cer
* O certificado foi exportado em formato RFC para ser utilizado no aplicativo.


Funcionalidade do App
    Este aplicativo tem como objetivo permitir a instalação do certificado gerado em dispositivos Android. A instalação do certificado é uma tarefa comum para configurar conexões seguras, como VPNs, servidores HTTPS, entre outros.


Como usar o app:
* Compile o aplicativo e instale no dispositivo Android.
*  O app solicitará permissão para instalar o certificado.
*  Após a instalação, o certificado estará disponível para uso em conexões seguras no dispositivo.

Pré-requisitos
* Android 7 ou superior.
* Acesso ao teste.keystore ou ao arquivo de certificado exportado teste.cer.