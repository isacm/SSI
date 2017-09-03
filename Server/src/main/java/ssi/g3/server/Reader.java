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
import java.io.InputStream;
import java.util.Arrays;
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
public class Reader extends BasicActor<Void, Void>{
    private InputStream in;
    private Cipher aes256;
    private SecretKey aesKey;
    private Mac hmac256;
    private ActorRef<Wrap> manager;
    
    public Reader(InputStream in, byte[] key, ActorRef<Wrap> manager) throws Exception {
        this.in = in;
        this.manager = manager;
        
        this.aes256 = Cipher.getInstance("AES/CTR/PKCS5Padding");
        this.aesKey = new SecretKeySpec(key, "AES");
        this.hmac256 = Mac.getInstance("HmacSHA256");
        this.hmac256.init(aesKey);
    }
    
    private byte[] decrypt(byte[] criptograma, byte[] iv) throws Exception{
        IvParameterSpec iv_ = new IvParameterSpec(iv);
        this.aes256.init(Cipher.DECRYPT_MODE, this.aesKey, iv_);
        
        return this.aes256.doFinal(criptograma);
    }
    
    private void macUpdate(byte[] bytes){
        this.hmac256.update(bytes);
    }
    
    private byte[] getMac(byte[] bytes){
        return this.hmac256.doFinal(bytes);
    
    }
    
    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {
        try{
            while(true){
                CriptoMessage.GenericMessage message = CriptoMessage.GenericMessage.parseDelimitedFrom(this.in);

                byte[] criptograma = message.getCriptograma().toByteArray();
                byte[] iv = message.getIv().toByteArray();
                byte[] mac = message.getMac().toByteArray();
                
                this.macUpdate(iv);
                byte[] macGerado = this.getMac(criptograma);
                
                if(! Arrays.equals(mac, macGerado)){
                    return null;
                }
                
                byte[] clearText = this.decrypt(criptograma, iv);
                
                Any packed = Any.parseFrom(clearText);
                
                if(packed.is(LoginProtos.Login.class)){
                    LoginProtos.Login login = packed.unpack(LoginProtos.Login.class);
                    
                    Wrap wrap = new Wrap(Type.Login, login);
                    this.manager.send(wrap);
                }
                else if(packed.is(LoginProtos.Registo.class)){
                    LoginProtos.Registo registo = packed.unpack(LoginProtos.Registo.class);
                    
                    Wrap wrap = new Wrap(Type.Registo, registo);
                    this.manager.send(wrap);
                }
                else if(packed.is(LoginProtos.PedidoCliente.class)){
                    LoginProtos.PedidoCliente pedido = packed.unpack(LoginProtos.PedidoCliente.class);
                    
                    Wrap wrap = new Wrap(Type.Pedido, pedido);
                    this.manager.send(wrap);
                }
                else if(packed.is(LoginProtos.Envelope.class)){
                    LoginProtos.Envelope envelope = packed.unpack(LoginProtos.Envelope.class);
                    
                    Wrap wrap = new Wrap(Type.Envio, envelope);
                    this.manager.send(wrap);
                }
                else if(packed.is(LoginProtos.MensagensRecebidas.class)){
                    LoginProtos.MensagensRecebidas mensagens = packed.unpack(LoginProtos.MensagensRecebidas.class);
                    
                    Wrap wrap = new Wrap(Type.Apagar, mensagens);
                    this.manager.send(wrap);
                }
                else if(packed.is(LoginProtos.Refresh.class)){
                    Wrap wrap = new Wrap(Type.Refresh, null);
                    this.manager.send(wrap);
                }
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        //return null;
    }
}
