/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ssi.g3.server;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import ssi.g3.proto.CriptoMessage;
import ssi.g3.proto.LoginProtos;

/**
 *
 * @author isacm
 */
public class Writer extends BasicActor<Wrap, Void> {

    private OutputStream out;
    private Cipher aes256;
    private SecretKey aesKey;
    private SecureRandom random;
    private Mac hmac256;

    public Writer(OutputStream out, byte[] key) throws Exception {
        this.out = out;
        this.aes256 = Cipher.getInstance("AES/CTR/PKCS5Padding");
        this.aesKey = new SecretKeySpec(key, "AES");
        this.random = SecureRandom.getInstance("NativePRNGNonBlocking");
        this.hmac256 = Mac.getInstance("HmacSHA256");
        this.hmac256.init(aesKey);
    }

    private byte[] encrypt(byte[] clearText, byte[] iv) throws Exception {
        this.random.nextBytes(iv);
        IvParameterSpec iv_ = new IvParameterSpec(iv);
        this.aes256.init(Cipher.ENCRYPT_MODE, this.aesKey, iv_);

        return this.aes256.doFinal(clearText);
    }

    private void macUpdate(byte[] bytes) {
        this.hmac256.update(bytes);
    }

    private byte[] getMac(byte[] bytes) {
        return this.hmac256.doFinal(bytes);

    }
    
    private CriptoMessage.GenericMessage getCriptoMessage(byte[] criptograma, byte[] iv, byte[] mac){
        return CriptoMessage.GenericMessage.newBuilder()
                .setCriptograma(ByteString.copyFrom(criptograma))
                .setIv(ByteString.copyFrom(iv))
                .setMac(ByteString.copyFrom(mac))
                .build();
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {
        Any packed;
        while (true) {
            try {
                Wrap w = this.receive();

                switch(w.type){
                    
                    case Erro:
                        String err = (String) w.obj;
                        LoginProtos.Erro erro = LoginProtos.Erro.newBuilder()
                                .setErro(err)
                                .build();
                        
                        packed = Any.pack(erro);
                        byte[] iv = new byte[16];
                        byte[] criptograma = this.encrypt(packed.toByteArray(), iv);
                        this.macUpdate(iv);
                        byte[] mac = this.getMac(criptograma);
                        
                        CriptoMessage.GenericMessage gm = this.getCriptoMessage(criptograma, iv, mac);
                        gm.writeDelimitedTo(this.out);
                        break;
                        
                    case Sucesso:
                        String suc = (String) w.obj;
                        LoginProtos.Sucesso sucesso = LoginProtos.Sucesso.newBuilder()
                                .setSucesso(suc)
                                .build();
                        
                        packed = Any.pack(sucesso);
                        
                        byte[] iv2 = new byte[16];
                        byte[] criptograma2 = this.encrypt(packed.toByteArray(), iv2);
                        this.macUpdate(iv2);
                        byte[] mac2 = this.getMac(criptograma2);
                        
                        CriptoMessage.GenericMessage gm2 = this.getCriptoMessage(criptograma2, iv2, mac2);
                        gm2.writeDelimitedTo(this.out);
                        
                        break;
                    case SucessoComMensagens:
                        List<LoginProtos.Entrega> entregas = (List<LoginProtos.Entrega>) w.obj;
                        
                        LoginProtos.Entregas.Builder builder = LoginProtos.Entregas.newBuilder();
                        
                        for(LoginProtos.Entrega e : entregas){
                            builder = builder.addEntregas(e);
                        }
                        LoginProtos.Entregas mensagens = builder.build();
                        packed = Any.pack(mensagens);
                        byte[] iv3 = new byte[16];
                        byte[] criptograma3 = this.encrypt(packed.toByteArray(), iv3);
                        this.macUpdate(iv3);
                        byte[] mac3 = this.getMac(criptograma3);
                        
                        CriptoMessage.GenericMessage gm3 = this.getCriptoMessage(criptograma3, iv3, mac3);
                        gm3.writeDelimitedTo(this.out);
                        break;
                        
                    case Certificado:
                        LoginProtos.RespostaPedido rp = (LoginProtos.RespostaPedido) w.obj;
                        
                        packed = Any.pack(rp);
                        
                        byte[] iv4 = new byte[16];
                        byte[] criptograma4 = this.encrypt(packed.toByteArray(), iv4);
                        this.macUpdate(iv4);
                        byte[] mac4 = this.getMac(criptograma4);
                        
                        CriptoMessage.GenericMessage gm4 = this.getCriptoMessage(criptograma4, iv4, mac4);
                        gm4.writeDelimitedTo(this.out);
                        
                        break;
                        
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                return null;
            }

        }

    }

}
