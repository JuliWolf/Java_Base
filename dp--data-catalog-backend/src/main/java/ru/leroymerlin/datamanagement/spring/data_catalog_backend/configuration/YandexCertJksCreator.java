package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/**
 * @author juliwolf
 */

@Component
@Profile("!test")
public class YandexCertJksCreator {
  @Value("${spring.kafka.properties.ssl.cert.location}")
  private String certLocation;

  @Value("${spring.kafka.properties.ssl.truststore.location}")
  private String truststoreLocation;

  @Value("${spring.kafka.properties.ssl.truststore.password}")
  private String truststorePassword;

  @PostConstruct
  public void loadJksFile() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);

    KeyPair keyPair = keyPairGenerator.generateKeyPair();

    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    FileInputStream fis = new FileInputStream(certLocation);
    Certificate cert = certFactory.generateCertificate(fis);
    fis.close();

    // Create a KeyStore instance
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(null, null);

    // Add the key pair and certificate to the KeyStore
    keyStore.setKeyEntry("yandex_truststore", keyPair.getPrivate(), truststorePassword.toCharArray(), new Certificate[]{cert});

    // Save the KeyStore to a JKS file
    File file = new File(truststoreLocation);
    file.getParentFile().mkdirs(); // Create parent directories

    FileOutputStream fos = new FileOutputStream(file);
    keyStore.store(fos, truststorePassword.toCharArray());
    fos.close();
  }
}
