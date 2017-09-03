/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ssi.g3.server;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import ssi.g3.proto.StationToStation;

/**
 *
 * @author isacm
 */
public class Protocol extends BasicActor<Void, Void> {
    private InputStream in;
    private OutputStream out;
    
    public Protocol(InputStream in, OutputStream out){
        this.in = in;
        this.out = out;
    }
    
    private static final String    HEXES    = "0123456789ABCDEF";
    
    public static String getHex(byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return ("0X" + hex.toString());
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {
        try{
            /* @@@@@@@@@@@@@@@@@@@@@@@@ Gerar Parâmetros @@@@@@@@@@@@@@@@@@@@@@@@@@ */
            SecureRandom rand = SecureRandom.getInstance("NativePRNGNonBlocking");
            BigInteger p = BigInteger.probablePrime(2048, rand);
            DHParameterSpec parameterSpec = new DHParameterSpec(p, BigInteger.valueOf(2), 2048);
            /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
            
            /* @@@@@@@@@@@@@@ Geração de Chaves para o Key Agreement @@@@@@@@@@@@@@ */
            KeyPairGenerator keyPair = KeyPairGenerator.getInstance("DH");
            keyPair.initialize(parameterSpec);
            KeyPair serverPair = keyPair.genKeyPair();
            KeyAgreement serverAgreement = KeyAgreement.getInstance("DH");
            serverAgreement.init(serverPair.getPrivate());
            /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
            
            /* @@@@@@@@@@@ Envio da Chave Pública g^x (A chave leva g p) @@@@@@@@@@ */
            byte[] pubKey = serverPair.getPublic().getEncoded();
            ByteString pubServerKey = ByteString.copyFrom(pubKey);
            StationToStation.Establishment establishment = StationToStation.Establishment.newBuilder()
                    .setGx(pubServerKey)
                    .build();
            
            Any any = Any.pack(establishment);
            any.writeDelimitedTo(this.out);
            /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
            
            /* @@@@@@ Recepção do gy, CertPath do Cliente e Ek(Sign(<gy,gx>))@@@@@@ */
            Any client = Any.parseDelimitedFrom(this.in);
            
            StationToStation.KeyAndCert keyAndCert;
            if(client.is(StationToStation.KeyAndCert.class)){
                keyAndCert = client.unpack(StationToStation.KeyAndCert.class);
            }
            else{
                return null;
            }
            /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
            
            //Agora pegamos no gy e criamos a sharedKey
            // Verificamos o certificado
            // com a sharedKey damos decript ao criptograma
            // damos verify com a chave publica do Cert
            
            /* @@@@@@@@@@ Criar a chave partilhada g^x^y a partir de g^y @@@@@@@@@@ */
            byte[] clientPubKey = keyAndCert.getGy().toByteArray();
            KeyFactory serverFactory = KeyFactory.getInstance("DH");
            X509EncodedKeySpec clientKeySpec = new X509EncodedKeySpec(clientPubKey);
            PublicKey clientPubKeyDH = serverFactory.generatePublic(clientKeySpec);
            serverAgreement.doPhase(clientPubKeyDH, true);
            byte[] sharedSecret = serverAgreement.generateSecret();
            /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
            
            /* @@@@@@@@@@@@@@@@ Verificação do CertPath do Cliente @@@@@@@@@@@@@@@@ */
            InputStream certIs = new ByteArrayInputStream(keyAndCert.getCert().toByteArray());
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            CertPath cp = certFactory.generateCertPath(certIs);
            X509Certificate ca = (X509Certificate) certFactory.generateCertificate(new FileInputStream("CA.cer"));
            CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
            TrustAnchor anchor = new TrustAnchor(ca, null);
            PKIXParameters params = new PKIXParameters(Collections.singleton(anchor));
            params.setRevocationEnabled(false);
            cpv.validate(cp, params);
            /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
            
            /* @@@@@@@@@ Decifra do Criptograma e Verificação do seu Mac @@@@@@@@@@ */
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] aesBytes = sha256.digest(sharedSecret);
            SecretKey aesKey = new SecretKeySpec(aesBytes, "AES");
            Cipher aes256 = Cipher.getInstance("AES/CTR/PKCS5Padding");
            byte[] iv = keyAndCert.getIv().toByteArray();
            IvParameterSpec iv_ = new IvParameterSpec(iv);
            aes256.init(Cipher.DECRYPT_MODE, aesKey, iv_);
            byte[] criptograma = keyAndCert.getSign().toByteArray();
            byte[] mac = keyAndCert.getMac().toByteArray();
            Mac hmac256 = Mac.getInstance("HmacSHA256");
            hmac256.init(aesKey);
            hmac256.update(iv);
            byte[] macGerado = hmac256.doFinal(criptograma); // É ENCRYPT-THEN-MAC
            byte[] assinatura = aes256.doFinal(criptograma);
            
            if(! Arrays.equals(mac, macGerado))
                return null;
            /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
            
            /* @@@@@@@ Verificação que a mensagem está devidamente assinada @@@@@@@ */
            Signature signature = Signature.getInstance("SHA256withRSA"); //OracleMode Hash-then-Sign
            signature.initVerify(cp.getCertificates().get(0));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(clientPubKey);
            baos.write(pubKey);
            byte[] gygx = baos.toByteArray();
            signature.update(gygx);
            boolean b = signature.verify(assinatura);
            
            if (b == false){
                return null;
            }
            /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
            
            //Carregar PrivKey e CertPath em memória.
            //Servidor Envia Certificado dele
            //Envia também g^x ++ g^y e assina
            //Depois cifra
            //Envia para o socket
            
            //FAZER O Q DIZ ACIMA AMANHA E VER O MODULO DO RSA
            
            /* @@@@@@@@@@@ Carrega a KeyStore, Chave Privada e CertPath @@@@@@@@@@@ */
            KeyStore keyStore = KeyStore.getInstance("pkcs12"); //pkcs12?
            InputStream is = new FileInputStream("Servidor.p12");
            keyStore.load(is, "1234".toCharArray());
            RSAPrivateKey privKey = (RSAPrivateKey) keyStore.getKey("Servidor", "1234".toCharArray());
            Certificate[] cert = keyStore.getCertificateChain("Servidor");
            CertPath certPath = certFactory.generateCertPath(Arrays.asList(cert));
            /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
            
            /* @@@@ Concata gx++gy, assina, cifra e concata com o mac da cifra @@@@ */
            //será q leva junto mais q 2048 bits por causa das outras merdas?
            baos.reset();
            baos.write(pubKey);
            baos.write(clientPubKey);
            byte[] gxgy = baos.toByteArray();
            baos.close();
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(privKey);
            sign.update(gxgy);
            byte[] gxgyAssinado = sign.sign();
            rand.nextBytes(iv);
            iv_ = new IvParameterSpec(iv);
            aes256.init(Cipher.ENCRYPT_MODE, aesKey, iv_);
            byte[] criptoServer = aes256.doFinal(gxgyAssinado);
            hmac256.update(iv);
            byte[] criptoMac = hmac256.doFinal(criptoServer);
           
            ByteString bsCriptograma = ByteString.copyFrom(criptoServer);
            ByteString bsMac = ByteString.copyFrom(criptoMac);
            ByteString bsIv = ByteString.copyFrom(iv);
            ByteString bsCertPath = ByteString.copyFrom(certPath.getEncoded());
            /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
            
            /* @@@@@@@@@@@@@@@@@@@@@@@ Envia para o Socket @@@@@@@@@@@@@@@@@@@@@@@@ */
            StationToStation.CertAndCripto certAndCripto = StationToStation.CertAndCripto.newBuilder()
                    .setCerti(bsCertPath)
                    .setCripto(bsCriptograma)
                    .setMac(bsMac)
                    .setIv(bsIv)
                    .build();
            
            Any packCripto = Any.pack(certAndCripto);
            packCripto.writeDelimitedTo(this.out);
            /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
            
            System.out.println("Protocologo Station-to-Station concluído com sucesso!!");
            
            Writer escritor = new Writer(out, aesBytes);
            ActorRef<Wrap> writer = escritor.spawn();
            Manager gestor = new Manager(writer, cp.getCertificates().get(0).getEncoded());
            ActorRef<Wrap> manager = gestor.spawn();
            Reader leitor = new Reader(in, aesBytes, manager);
            ActorRef<Void> reader = leitor.spawn();
            
            leitor.join();
            manager.close();
            writer.close();
            
        }
   
        catch(Exception e){
            
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }     
        return null;
    }
}
