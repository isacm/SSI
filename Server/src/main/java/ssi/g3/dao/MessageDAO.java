/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ssi.g3.dao;

import com.google.protobuf.ByteString;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import ssi.g3.proto.LoginProtos;

/**
 *
 * @author isacm
 */
public class MessageDAO {

    private Connection connection;

    public MessageDAO() {
        this.connection = ConnectionFactory.getConnection();
    }

    public MessageDAO(Connection connection) {
        this.connection = connection;
    }

    public void put(byte[] email, String sender, String receiver) throws MessageDAOException {
        String sql = "INSERT INTO Message (sender, destinatary, criptograma) "
                + "VALUES (?, ?, ?) ;";

        try {
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1, sender);
            stmt.setString(2, receiver);

            byte[] criptograma = email;
            stmt.setBytes(3, criptograma);

            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            for (int i = 0; i < 1000; i++) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream("log.txt", true));
                pw.println(e.getMessage());
                e.printStackTrace(pw);
                pw.flush();
                pw.close();
            } catch (IOException ex) {

            }
            throw new MessageDAOException("Ups! Ocorreu um erro na gravação da mensagem!");
        }
    }

    public List<LoginProtos.Entrega> get(String destinatario) throws MessageDAOException {
        String sql = "SELECT id, sender, criptograma FROM Message "
                + " WHERE destinatary = ? ;";

        List<LoginProtos.Entrega> mensagens = new ArrayList<>();

        try {
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1, destinatario);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String sender = rs.getString("sender");
                byte[] criptograma = rs.getBytes("criptograma");

                LoginProtos.Entrega mensagem = LoginProtos.Entrega.newBuilder()
                        .setId(id)
                        .setRemetente(sender)
                        .setCriptograma(ByteString.copyFrom(criptograma))
                        .build();

                mensagens.add(mensagem);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream("log.txt", true));
                pw.println(e.getMessage());
                e.printStackTrace(pw);
                pw.flush();
                pw.close();
            } catch (IOException ex) {

            }
            throw new MessageDAOException("Ups! Ocorreu um erro na busca de mensagens!");
        }

        return mensagens;
    }

    public void remove(int id) throws MessageDAOException {
        String sql = "DELETE FROM Message WHERE id = ? ;";

        try {
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setInt(1, id);

            stmt.execute();

            stmt.close();

        } catch (SQLException e) {
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream("log.txt", true));
                pw.println(e.getMessage());
                e.printStackTrace(pw);
                pw.flush();
                pw.close();
            } catch (IOException ex) {

            }
            throw new MessageDAOException("Ups! Ocorreu um erro na eliminação da mensagem!");
        }
    }

    public void remove(List<Integer> ids) throws MessageDAOException {
        String sql = "DELETE FROM Message WHERE id = ?";

        int len = ids.size() - 1;

        for (int i = 0; i < len; i++) {
            sql += "OR id = ?";
        }
        sql += " ;";

        try {
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            
            int i = 1;
            for(Integer j : ids){
                stmt.setInt(i, j);
            }
            
            stmt.execute();
            stmt.close();

        } catch (SQLException e) {
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream("log.txt", true));
                pw.println(e.getMessage());
                e.printStackTrace(pw);
                pw.flush();
                pw.close();
            } catch (IOException ex) {

            }
            throw new MessageDAOException("Ups! Ocorreu um erro na eliminação das mensagens!");
        }
    }
}
