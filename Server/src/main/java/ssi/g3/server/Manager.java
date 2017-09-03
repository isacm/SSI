/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ssi.g3.server;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import com.google.protobuf.ByteString;
import java.sql.SQLException;
import java.util.List;
import ssi.g3.dao.ClientDAO;
import ssi.g3.dao.ClientDAOException;
import ssi.g3.dao.MessageDAO;
import ssi.g3.dao.MessageDAOException;
import ssi.g3.proto.LoginProtos;

/**
 *
 * @author isacm
 */
public class Manager extends BasicActor<Wrap, Void> {

    private ActorRef<Wrap> writer;
    private byte[] cp;
    private ClientDAO clientDao;
    private MessageDAO messageDao;
    private String cliente;

    public Manager(ActorRef<Wrap> writer, byte[] cert) {
        this.writer = writer;
        this.cp = cert;
        this.clientDao = new ClientDAO();
        this.messageDao = new MessageDAO();
        this.cliente = null;
    }

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {

        try {

            boolean log = false;

            while (true) {

                Wrap w = this.receive();

                switch (w.type) {
                    case Login:
                        LoginProtos.Login login = (LoginProtos.Login) w.obj;

                        try {
                            log = this.clientDao.hasClient(login);
                            this.cliente = login.getEmail();

                            if (log) {
                                List<LoginProtos.Entrega> mensagens = this.messageDao.get(login.getEmail());

                                if (mensagens.isEmpty()) {
                                    String sucesso = "Logado";
                                    Wrap suc = new Wrap(Type.Sucesso, sucesso);
                                    this.writer.send(suc);
                                } else {
                                    Wrap mailbox = new Wrap(Type.SucessoComMensagens, mensagens);
                                    this.writer.send(mailbox);
                                }
                            } else {
                                String erro = "Invalid";
                                Wrap err = new Wrap(Type.Erro, erro);
                                this.writer.send(err);
                            }
                        } catch (ClientDAOException e) {
                            System.out.println(e.getMessage());
                            e.printStackTrace();

                            String servError = "ServerError";
                            Wrap servErr = new Wrap(Type.Erro, servError);
                            this.writer.send(servErr);
                        }

                        break;

                    case Registo:
                        LoginProtos.Registo registo = (LoginProtos.Registo) w.obj;

                        try {
                            boolean b = this.clientDao.hasClient(registo);

                            if (!b) {
                                this.clientDao.insertClient(registo, this.cp);
                                String registoSucedido = "Registado";
                                Wrap sucReg = new Wrap(Type.Sucesso, registoSucedido);
                                this.writer.send(sucReg);
                            } else {
                                String erro = "Invalid";
                                Wrap err = new Wrap(Type.Erro, erro);
                                this.writer.send(err);
                            }
                        } catch (ClientDAOException e) {
                            System.out.println(e.getMessage());
                            e.printStackTrace();

                            String servError = "ServerError";
                            Wrap servErr = new Wrap(Type.Erro, servError);
                            this.writer.send(servErr);
                        }

                        break;

                    case Pedido:
                        if (log) {
                            LoginProtos.PedidoCliente pedido = (LoginProtos.PedidoCliente) w.obj;

                            try {
                                byte[] cert = this.clientDao.getCert(pedido.getNome());
                                LoginProtos.RespostaPedido rp = LoginProtos.RespostaPedido.newBuilder()
                                        .setCertificado(ByteString.copyFrom(cert))
                                        .build();

                                Wrap wRp = new Wrap(Type.Certificado, rp);
                                this.writer.send(wRp);
                            } catch (ClientDAOException e) {

                                if (e.getMessage().equals("O cliente não existe")) {
                                    String erro = "Invalid";
                                    Wrap err = new Wrap(Type.Erro, erro);
                                    this.writer.send(err);
                                } else {
                                    System.out.println(e.getMessage());
                                    e.printStackTrace();

                                    String servError = "ServerError";
                                    Wrap servErr = new Wrap(Type.Erro, servError);
                                    this.writer.send(servErr);
                                }
                            }
                        } else {
                            return null;
                        }
                        break;

                    case Envio:
                        if (log) {
                            LoginProtos.Envelope envelope = (LoginProtos.Envelope) w.obj;

                            try {
                                String destinatario = envelope.getDestinatario();
                                //byte[] env = envelope.getEnvelope().toByteArray();

                                //this.messageDao.put(env, this.cliente, destinatario);
                                this.messageDao.put(envelope.toByteArray(), this.cliente, destinatario);
                                String sucesso = "Enviado";
                                Wrap suc = new Wrap(Type.Sucesso, sucesso);
                                this.writer.send(suc);
                            } catch (MessageDAOException e) {
                                System.out.println(e.getMessage());
                                e.printStackTrace();

                                String servError = "ServerError";
                                Wrap servErr = new Wrap(Type.Erro, servError);
                                this.writer.send(servErr);

                            }
                        } else {
                            return null;
                        }

                        break;

                    case Apagar:
                        if (log) {
                            LoginProtos.MensagensRecebidas mensagens = (LoginProtos.MensagensRecebidas) w.obj;

                            try {
                                List<Integer> ids = mensagens.getIdsList();
                                this.messageDao.remove(ids);
                            } catch (MessageDAOException e) {
                                System.out.println(e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            return null;
                        }
                        break;
                    case Refresh:
                        if (log) {
                            try {
                                List<LoginProtos.Entrega> mensagens = this.messageDao.get(this.cliente);

                                if (mensagens.isEmpty()) {
                                    String sucesso = "NothingNew";
                                    Wrap suc = new Wrap(Type.Sucesso, sucesso);
                                    this.writer.send(suc);
                                } else {
                                    Wrap mailbox = new Wrap(Type.SucessoComMensagens, mensagens);
                                    this.writer.send(mailbox);
                                }

                            }
                            catch(MessageDAOException e){
                                System.out.println(e.getMessage());
                                e.printStackTrace();

                                String servError = "ServerError";
                                Wrap servErr = new Wrap(Type.Erro, servError);
                                this.writer.send(servErr);
                            }

                        }
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
