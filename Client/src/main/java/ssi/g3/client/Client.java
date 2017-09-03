/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ssi.g3.client;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
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
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import ssi.g3.model.ClientBox;
import ssi.g3.model.Loader;
import ssi.g3.model.Message;
import ssi.g3.proto.CriptoMessage;
import ssi.g3.proto.LoginProtos;
import ssi.g3.proto.StationToStation;

/**
 *
 * @author isacm
 */
public class Client {

    private static final String host = "localhost";
    private static final int port = 8765;

    private static final String HEXES = "0123456789ABCDEF";

    public static String getHex(byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return ("0X" + hex.toString());
    }

    private InputStream in;
    private OutputStream out;
    private Scanner scanner;
    private Menu menu1;
    private Menu menu2;
    private Cipher aes256;
    private Cipher rsa;
    private SecretKey aesKey;
    private SecureRandom random;
    private Mac hmac256;
    private PrivateKey rsaKey;
    private ClientBox clientBox;
    private KeyGenerator keyGenerator;
    private Loader loader;

    public Client() throws Exception {
        Socket s = new Socket(host, port);
        this.in = s.getInputStream();
        this.out = s.getOutputStream();
        this.scanner = new Scanner(System.in);
        this.aes256 = Cipher.getInstance("AES/CTR/PKCS5Padding");
        this.rsa = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
        this.aesKey = null;
        this.random = SecureRandom.getInstance("NativePRNGNonBlocking");
        this.hmac256 = Mac.getInstance("HmacSHA256");
        this.keyGenerator = KeyGenerator.getInstance("AES");
        this.keyGenerator.init(256, this.random);
        this.rsaKey = null;
        this.clientBox = new ClientBox();
        this.loader = new Loader();

        String[] options1 = {"Registar", "Login"};
        this.menu1 = new Menu(this.scanner, Arrays.asList(options1));
        String[] options2 = {"Enviar Email", "Ver os meus Emails", "Refresh"};
        this.menu2 = new Menu(scanner, Arrays.asList(options2));
    }

    private void protocol() throws Exception {

        Any keyAndParams = Any.parseDelimitedFrom(this.in);

        StationToStation.Establishment establishment;

        if (keyAndParams.is(StationToStation.Establishment.class)) {
            establishment = keyAndParams.unpack(StationToStation.Establishment.class);
        } else {
            throw new Exception("Esperava o g, p, g^x");
        }

        byte[] gx = establishment.getGx().toByteArray();

        KeyFactory keyFactory = KeyFactory.getInstance("DH");

        X509EncodedKeySpec gxSpec = new X509EncodedKeySpec(gx);
        PublicKey serverPubKey = keyFactory.generatePublic(gxSpec);
        DHParameterSpec parameterSpec = ((DHPublicKey) serverPubKey).getParams();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
        keyPairGenerator.initialize(parameterSpec);

        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
        keyAgreement.init(keyPair.getPrivate());

        byte[] pubKey = keyPair.getPublic().getEncoded();
        ByteString bsPubKey = ByteString.copyFrom(pubKey);

        keyAgreement.doPhase(serverPubKey, true);
        byte[] sharedSecret = keyAgreement.generateSecret();

        //Enviar gy, cert, Ek(Sign(gy++gx))
        //Para isso preciso:
        //Carregar KeyStore
        // Carregar Chave Privada da KeyStore
        // Carregar CertPath da KeyStore
        // Assinar gy++gx com chave privada
        // Usar SHA256 na chave privada
        // Usar AES_256 para cifrar a assinatura
        // Calcular MAC
        // Criptograma++MAC
        // Enviar tudo
        KeyStore keyStore = KeyStore.getInstance(this.loader.getKeyStoreType());
        keyStore.load(new FileInputStream(this.loader.getKeyStoreName()), this.loader.getPasswordKeyStore());
        this.rsaKey = (PrivateKey) keyStore.getKey(this.loader.getAlias(), this.loader.getPasswordAlias());
        Certificate[] certChain = keyStore.getCertificateChain(this.loader.getAlias());

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        CertPath certPath = certFactory.generateCertPath(Arrays.asList(certChain));
        ByteString certificado = ByteString.copyFrom(certPath.getEncoded());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(pubKey);
        baos.write(gx);

        byte[] gygx = baos.toByteArray();

        Signature sha256WithRSA = Signature.getInstance("SHA256withRSA");
        sha256WithRSA.initSign(this.rsaKey);
        sha256WithRSA.update(gygx);
        byte[] gygxSigned = sha256WithRSA.sign();

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hashedSharedKey = sha256.digest(sharedSecret);
        this.aesKey = new SecretKeySpec(hashedSharedKey, "AES");

        byte[] iv = new byte[16];
        this.random.nextBytes(iv);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        //Cipher aes256 = Cipher.getInstance("AES/CTR/PKCS5Padding"); //Preciso de lhe passar um IV.
        this.aes256.init(Cipher.ENCRYPT_MODE, this.aesKey, ivSpec);

        byte[] criptograma = this.aes256.doFinal(gygxSigned);
        this.hmac256.init(this.aesKey);
        this.hmac256.update(iv);
        byte[] mac = this.hmac256.doFinal(criptograma);

        ByteString criptograma_ = ByteString.copyFrom(criptograma);
        ByteString mac_ = ByteString.copyFrom(mac);
        ByteString iv_ = ByteString.copyFrom(iv);
        StationToStation.KeyAndCert clientPhase1 = StationToStation.KeyAndCert.newBuilder()
                .setGy(bsPubKey)
                .setCert(certificado)
                .setSign(criptograma_)
                .setIv(iv_)
                .setMac(mac_)
                .build();

        Any phase1 = Any.pack(clientPhase1);

        phase1.writeDelimitedTo(this.out);

        //Agora vou receber CertServidor, Ek(Sign(gx++gy))
        //O que fazer:
        // Ir buscar ao socket a mensagem
        // Verificar Certificado
        // Partir Ek em criptograma e mac
        // Ver se o MAC corresponde
        // Decifrar Ek
        // Verificar assinatura
        //Protocologo Concluido!!!
        Any phase2 = Any.parseDelimitedFrom(this.in);

        StationToStation.CertAndCripto certAndCripto;
        if (phase2.is(StationToStation.CertAndCripto.class)) {
            certAndCripto = phase2.unpack(StationToStation.CertAndCripto.class);
        } else {
            throw new Exception("Esperava o Certificado do Servidor e Criptograma");
        }

        InputStream certIs = new ByteArrayInputStream(certAndCripto.getCerti().toByteArray());
        CertPath cp = certFactory.generateCertPath(certIs);
        X509Certificate ca = (X509Certificate) certFactory.generateCertificate(new FileInputStream(this.loader.getCaName()));
        CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
        TrustAnchor anchor = new TrustAnchor(ca, null);
        PKIXParameters params = new PKIXParameters(Collections.singleton(anchor));
        params.setRevocationEnabled(false);
        cpv.validate(cp, params);

        byte[] cripto = certAndCripto.getCripto().toByteArray();
        byte[] macMensagem = certAndCripto.getMac().toByteArray();
        iv = certAndCripto.getIv().toByteArray();
        this.hmac256.update(iv);
        byte[] macDoCripto = this.hmac256.doFinal(cripto);

        if (!Arrays.equals(macMensagem, macDoCripto)) {
            throw new Exception("Os MAC não são iguais");
        }

        ivSpec = new IvParameterSpec(iv);

        this.aes256.init(Cipher.DECRYPT_MODE, this.aesKey, ivSpec);
        byte[] assinatura = this.aes256.doFinal(cripto);

        baos.reset();
        baos.write(gx);
        baos.write(pubKey);
        byte[] gxgy = baos.toByteArray();
        baos.close();

        Signature verify = Signature.getInstance("SHA256withRSA");
        verify.initVerify(cp.getCertificates().get(0));
        verify.update(gxgy);
        boolean b = verify.verify(assinatura);

        if (!b) {
            throw new Exception("A assinatura do servidor foi conspurcada.");
        }

        System.out.println("Protocologo Concluído");
    }

    private CriptoMessage.GenericMessage getCriptoMessage(byte[] criptograma, byte[] iv, byte[] mac) {
        return CriptoMessage.GenericMessage.newBuilder()
                .setCriptograma(ByteString.copyFrom(criptograma))
                .setIv(ByteString.copyFrom(iv))
                .setMac(ByteString.copyFrom(mac))
                .build();
    }

    private byte[] encrypt(byte[] clearText, byte[] iv) throws Exception {
        this.random.nextBytes(iv);
        IvParameterSpec iv_ = new IvParameterSpec(iv);
        this.aes256.init(Cipher.ENCRYPT_MODE, this.aesKey, iv_);

        return this.aes256.doFinal(clearText);
    }

    private byte[] decrypt(byte[] criptograma, byte[] iv) throws Exception {
        IvParameterSpec iv_ = new IvParameterSpec(iv);
        this.aes256.init(Cipher.DECRYPT_MODE, this.aesKey, iv_);

        return this.aes256.doFinal(criptograma);
    }

    private void macUpdate(byte[] bytes) {
        this.hmac256.update(bytes);
    }

    private byte[] getMac(byte[] bytes) {
        return this.hmac256.doFinal(bytes);

    }

    private byte[] unpack(CriptoMessage.GenericMessage message) throws Exception {
        byte[] criptograma = message.getCriptograma().toByteArray();
        byte[] iv = message.getIv().toByteArray();
        byte[] mac = message.getMac().toByteArray();

        this.macUpdate(iv);
        byte[] macGerado = this.getMac(criptograma);

        if (!Arrays.equals(mac, macGerado)) {
            throw new Exception("Conspurcação da mensagem!");
        }

        return this.decrypt(criptograma, iv);
    }
    
    private SecretKey genKey() throws Exception{
        return this.keyGenerator.generateKey();
    }
    
    private byte[] wrapKey(Certificate cert, SecretKey key) throws Exception{
        this.rsa.init(Cipher.ENCRYPT_MODE, cert);
        return this.rsa.doFinal(key.getEncoded());
    }
    
    private byte[] envelopeContent(byte[] message, byte[] iv, SecretKey key) throws Exception{
        this.random.nextBytes(iv);
        IvParameterSpec iv_ = new IvParameterSpec(iv);
        this.aes256.init(Cipher.ENCRYPT_MODE, key, iv_);

        return this.aes256.doFinal(message);
    }
    
    private LoginProtos.Email abrirEnvelope(LoginProtos.Envelope envelope) throws Exception {
        byte[] criptograma = envelope.getCriptograma().toByteArray();
        byte[] iv = envelope.getIv().toByteArray();
        byte[] keyCiphered = envelope.getKey().toByteArray();
        
        this.rsa.init(Cipher.DECRYPT_MODE, this.rsaKey);
        byte[] key = this.rsa.doFinal(keyCiphered);
        
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        IvParameterSpec iv_ = new IvParameterSpec(iv);
        this.aes256.init(Cipher.DECRYPT_MODE, secretKey, iv_);
        return LoginProtos.Email.parseFrom(this.aes256.doFinal(criptograma));
    }

    private void menu2Handler(String option) throws Exception {
        if (option.equals("Enviar Email")) {
            System.out.println("Destinatário: ");
            this.scanner.nextLine();
            String destinatario = this.scanner.nextLine();
            System.out.println();
            System.out.println("Título: ");
            String titulo = this.scanner.nextLine();
            System.out.println();
            System.out.println("Conteúdo: ");
            String conteudo = this.scanner.nextLine();
            System.out.println();

            LoginProtos.PedidoCliente pedido = LoginProtos.PedidoCliente.newBuilder()
                    .setNome(destinatario)
                    .build();

            Any packed = Any.pack(pedido);
            byte[] iv = new byte[16];
            byte[] criptograma = this.encrypt(packed.toByteArray(), iv);
            this.macUpdate(iv);
            byte[] mac = this.getMac(criptograma);

            CriptoMessage.GenericMessage gm = this.getCriptoMessage(criptograma, iv, mac);
            gm.writeDelimitedTo(out);

            CriptoMessage.GenericMessage resposta = CriptoMessage.GenericMessage.parseDelimitedFrom(this.in);
            byte[] clearText = this.unpack(resposta);

            Any any = Any.parseFrom(clearText);

            if (any.is(LoginProtos.RespostaPedido.class)) {
                LoginProtos.RespostaPedido rp = any.unpack(LoginProtos.RespostaPedido.class);

                byte[] cert = rp.getCertificado().toByteArray();
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                ByteArrayInputStream bai = new ByteArrayInputStream(cert);
                X509Certificate certificate = (X509Certificate) factory.generateCertificate(bai);
                bai.close();

                LoginProtos.Email email = LoginProtos.Email.newBuilder()
                        .setTitulo(titulo)
                        .setConteudo(conteudo)
                        .build();
                        
                byte[] iv_ = new byte[16];
                SecretKey key = this.genKey();
                byte[] content = this.envelopeContent(email.toByteArray(), iv_, key);
                byte[] keyCiphered = this.wrapKey(certificate, key);
                
                LoginProtos.Envelope env = LoginProtos.Envelope.newBuilder()
                        .setDestinatario(destinatario)
                        .setCriptograma(ByteString.copyFrom(content))
                        .setKey(ByteString.copyFrom(keyCiphered))
                        .setIv(ByteString.copyFrom(iv_))
                        .build();

                Any pack = Any.pack(env);
                byte[] iv2 = new byte[16];
                byte[] criptograma2 = this.encrypt(pack.toByteArray(), iv2);
                this.macUpdate(iv2);
                byte[] mac2 = this.getMac(criptograma2);

                CriptoMessage.GenericMessage gm2 = this.getCriptoMessage(criptograma2, iv2, mac2);
                gm2.writeDelimitedTo(this.out);

                CriptoMessage.GenericMessage finalResp = CriptoMessage.GenericMessage.parseDelimitedFrom(this.in);
                byte[] clearText2 = this.unpack(finalResp);

                Any any2 = Any.parseFrom(clearText2);

                if (any2.is(LoginProtos.Sucesso.class)) {
                    System.out.println("Mensagem Enviada com sucesso!");
                } else {
                    System.out.println("Alguma coisa má aconteceu!");
                }
            } else if (any.is(LoginProtos.Erro.class)) {
                LoginProtos.Erro erro = any.unpack(LoginProtos.Erro.class);

                String tipoErro = erro.getErro();

                if (tipoErro.equals("Invalid")) {
                    System.out.println("O utilizador que inseriu não existe!");

                } else {
                    System.out.println("Erro no servidor. Tente de novo mais tarde!");
                }
            }

            this.menu2Handler(this.menu2.chooseOption());
        } else if (option.equals("Ver os meus Emails")) {
            if (this.clientBox.isEmpty()) {
                System.out.println("Não tem mensagens!");
            } else {
                System.out.println(this.clientBox);
            }

            this.menu2Handler(this.menu2.chooseOption());
        } else if (option.equals("Refresh")) {
            LoginProtos.Refresh refresh = LoginProtos.Refresh.newBuilder()
                    .build();

            Any pack = Any.pack(refresh);

            byte[] iv = new byte[16];
            byte[] criptograma = this.encrypt(pack.toByteArray(), iv);
            this.macUpdate(iv);
            byte[] mac = this.getMac(criptograma);

            CriptoMessage.GenericMessage gm = this.getCriptoMessage(criptograma, iv, mac);
            gm.writeDelimitedTo(out);

            CriptoMessage.GenericMessage resposta = CriptoMessage.GenericMessage.parseDelimitedFrom(this.in);
            byte[] clearText = this.unpack(resposta);

            Any any = Any.parseFrom(clearText);

            if (any.is(LoginProtos.Entregas.class)) {
                LoginProtos.Entregas entregas = any.unpack(LoginProtos.Entregas.class);
                List<LoginProtos.Entrega> mensagensNoEnvelope = entregas.getEntregasList();

                LoginProtos.MensagensRecebidas.Builder builder = LoginProtos.MensagensRecebidas.newBuilder();

                for (LoginProtos.Entrega e : mensagensNoEnvelope) {

                    String remetente = e.getRemetente();
                    int id = e.getId();
                    builder = builder.addIds(id);
                    LoginProtos.Envelope envelope = LoginProtos.Envelope.parseFrom(e.getCriptograma());
                    
                    //byte[] envelope = e.getCriptograma().toByteArray();
                    LoginProtos.Email email = this.abrirEnvelope(envelope);

                    String titulo = email.getTitulo();
                    String conteudo = email.getConteudo();

                    Message mensagem = new Message((long) id, remetente, titulo, conteudo);
                    this.clientBox.addInBox(mensagem);
                }

                LoginProtos.MensagensRecebidas mensagens = builder.build();
                Any packed = Any.pack(mensagens);

                byte[] iv2 = new byte[16];
                byte[] criptograma2 = this.encrypt(packed.toByteArray(), iv2);
                this.macUpdate(iv2);
                byte[] mac2 = this.getMac(criptograma2);

                CriptoMessage.GenericMessage gm2 = this.getCriptoMessage(criptograma2, iv2, mac2);
                gm2.writeDelimitedTo(this.out);

                System.out.println("Tem mensagens novas!");            
            }
            else if(any.is(LoginProtos.Sucesso.class)){
                 System.out.println("Não tem mensagens novas mensagens!");
            }
            else{
                System.out.println("Erro no servidor. Tente de novo mais tarde!");
            }
            this.menu2Handler(this.menu2.chooseOption());
        } else {
            this.menu1Handler(this.menu1.chooseOption());
        }
    }

    private void menu1Handler(String option) throws Exception {
        if (option.equals("Login")) {
            System.out.println("Username:");
            this.scanner.nextLine();
            String username = this.scanner.nextLine();
            System.out.println();
            System.out.println("Password:");
            String password = this.scanner.nextLine();
            System.out.println();

            LoginProtos.Login login = LoginProtos.Login.newBuilder()
                    .setEmail(username)
                    .setPassword(password)
                    .build();

            Any packed = Any.pack(login);
            byte[] iv = new byte[16];
            byte[] criptograma = this.encrypt(packed.toByteArray(), iv);
            this.macUpdate(iv);
            byte[] mac = this.getMac(criptograma);

            CriptoMessage.GenericMessage gm = CriptoMessage.GenericMessage.newBuilder()
                    .setCriptograma(ByteString.copyFrom(criptograma))
                    .setIv(ByteString.copyFrom(iv))
                    .setMac(ByteString.copyFrom(mac))
                    .build();

            gm.writeDelimitedTo(this.out);

            CriptoMessage.GenericMessage resposta = CriptoMessage.GenericMessage.parseDelimitedFrom(this.in);
            byte[] clearText = this.unpack(resposta);

            Any any = Any.parseFrom(clearText);

            if (any.is(LoginProtos.Sucesso.class)) {
                try{
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream("mailBox.dat"));
                    this.clientBox = (ClientBox) ois.readObject();
                }
                catch(IOException | ClassNotFoundException e){
                    
                }

                System.out.println("Logado com sucesso");
                this.menu2Handler(this.menu2.chooseOption());

            } else if (any.is(LoginProtos.Entregas.class)) {
                try{
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream("mailBox.dat"));
                    this.clientBox = (ClientBox) ois.readObject();
                }
                catch(IOException | ClassNotFoundException e){
                    
                }
                

                LoginProtos.Entregas entregas = any.unpack(LoginProtos.Entregas.class);
                List<LoginProtos.Entrega> mensagensNoEnvelope = entregas.getEntregasList();

                LoginProtos.MensagensRecebidas.Builder builder = LoginProtos.MensagensRecebidas.newBuilder();

                for (LoginProtos.Entrega e : mensagensNoEnvelope) {

                    String remetente = e.getRemetente();
                    int id = e.getId();
                    builder = builder.addIds(id);
                    LoginProtos.Envelope envelope = LoginProtos.Envelope.parseFrom(e.getCriptograma());
                    //byte[] envelope = e.getCriptograma().toByteArray();
                    LoginProtos.Email email = this.abrirEnvelope(envelope);

                    String titulo = email.getTitulo();
                    String conteudo = email.getConteudo();

                    Message mensagem = new Message((long) id, remetente, titulo, conteudo);
                    this.clientBox.addInBox(mensagem);
                }

                LoginProtos.MensagensRecebidas mensagens = builder.build();
                Any pack = Any.pack(mensagens);

                byte[] iv2 = new byte[16];
                byte[] criptograma2 = this.encrypt(pack.toByteArray(), iv2);
                this.macUpdate(iv2);
                byte[] mac2 = this.getMac(criptograma2);

                CriptoMessage.GenericMessage gm2 = this.getCriptoMessage(criptograma2, iv2, mac2);
                gm2.writeDelimitedTo(this.out);

                System.out.println("Logado com sucesso");
                System.out.println("Tem mensagens novas!");

                this.menu2Handler(this.menu2.chooseOption());
            } else if (any.is(LoginProtos.Erro.class)) {
                LoginProtos.Erro erro = any.unpack(LoginProtos.Erro.class);

                String tipoErro = erro.getErro();

                if (tipoErro.equals("Invalid")) {
                    System.out.println("Credenciais Inválidas.");
                } else {
                    System.out.println("Erro no servidor. Tente de novo mais tarde!");
                }
                this.menu1Handler(this.menu1.chooseOption());
            }
        } else if (option.equals("Registar")) {

            System.out.println("Username pretendido:");
            this.scanner.nextLine();
            String username = this.scanner.nextLine();
            System.out.println();
            System.out.println("Password:");
            String password = this.scanner.nextLine();
            System.out.println();

            LoginProtos.Registo registo = LoginProtos.Registo.newBuilder()
                    .setNome(username)
                    .setPassword(password)
                    .build();

            Any packed = Any.pack(registo);
            byte[] iv = new byte[16];
            byte[] criptograma = this.encrypt(packed.toByteArray(), iv);
            this.macUpdate(iv);
            byte[] mac = this.getMac(criptograma);

            CriptoMessage.GenericMessage gm = this.getCriptoMessage(criptograma, iv, mac);
            gm.writeDelimitedTo(out);

            CriptoMessage.GenericMessage resposta = CriptoMessage.GenericMessage.parseDelimitedFrom(this.in);
            byte[] clearText = this.unpack(resposta);

            Any any = Any.parseFrom(clearText);

            if (any.is(LoginProtos.Sucesso.class)) {
                System.out.println("Registado com sucesso!");
            } else if (any.is(LoginProtos.Erro.class)) {
                LoginProtos.Erro erro = any.unpack(LoginProtos.Erro.class);

                String tipoErro = erro.getErro();

                if (tipoErro.equals("Invalid")) {
                    System.out.println("Nome de utilizador já em uso. Escolha outro.");
                } else {
                    System.out.println("Erro no servidor. Tente de novo mais tarde!");
                }
            }
            this.menu1Handler(this.menu1.chooseOption());

        } else {
            this.clientBox.gravaObj("mailBox.dat");
            System.out.println("Adeus!\n");
            System.exit(0);
        }
    }

    public void main1() {
        try {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            this.protocol();
            String option;

            option = this.menu1.chooseOption();
            this.menu1Handler(option);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static void main(String[] args) {
        try {
            new Client().main1();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

    }
}
