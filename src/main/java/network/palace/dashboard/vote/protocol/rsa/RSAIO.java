package network.palace.dashboard.vote.protocol.rsa;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Static utility methods for saving and loading RSA key pairs.
 */
public class RSAIO {

    /**
     * Saves the key pair to the disk.
     *
     * @param directory The directory to save to
     * @param keyPair   The key pair to save
     * @throws Exception If an error occurs
     */
    public static void save(File directory, KeyPair keyPair) throws Exception {
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Store the public and private keys.
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(
                publicKey.getEncoded());
        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(
                privateKey.getEncoded());
        try (FileOutputStream publicOut = new FileOutputStream(directory + "/public.key");
             FileOutputStream privateOut = new FileOutputStream(directory + "/private.key")) {
            publicOut.write(DatatypeConverter.printBase64Binary(publicSpec.getEncoded())
                    .getBytes(StandardCharsets.UTF_8));
            privateOut.write(DatatypeConverter.printBase64Binary(privateSpec.getEncoded())
                    .getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Loads an RSA key pair from a directory. The directory must have the files
     * "public.key" and "private.key".
     *
     * @param directory The directory to load from
     * @return The key pair
     * @throws Exception If an error occurs
     */
    public static KeyPair load(File directory) throws Exception {
        // Read the public key file.
        File publicKeyFile = new File(directory + "/public.key");
        byte[] encodedPublicKey = Files.readAllBytes(publicKeyFile.toPath());
        encodedPublicKey = DatatypeConverter.parseBase64Binary(new String(
                encodedPublicKey, StandardCharsets.UTF_8));

        // Read the private key file.
        File privateKeyFile = new File(directory + "/private.key");
        byte[] encodedPrivateKey = Files.readAllBytes(privateKeyFile.toPath());
        encodedPrivateKey = DatatypeConverter.parseBase64Binary(new String(
                encodedPrivateKey, StandardCharsets.UTF_8));

        // Instantiate and return the key pair.
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                encodedPublicKey);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                encodedPrivateKey);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
        return new KeyPair(publicKey, privateKey);
    }

}
