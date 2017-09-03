/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ssi.g3.dao;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import ssi.g3.proto.LoginProtos;

/**
 *
 * @author isacm
 */
public class ClientDAO {

    private Connection connection;

    public ClientDAO() {
        this.connection = ConnectionFactory.getConnection();
    }

    public ClientDAO(Connection connection) {
        this.connection = connection;
    }

    public void insertClient(LoginProtos.Registo utilizador, byte[] cp) throws ClientDAOException {
        String sql = "INSERT INTO Client (username, password, cert) "
                + "VALUES (?, ?, ?);";

        try {
            PreparedStatement stmt = this.connection.prepareStatement(sql);

            stmt.setString(1, utilizador.getNome());
            stmt.setString(2, utilizador.getPassword());
            stmt.setBytes(3, cp);

            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            for (int i = 0; i < 1000; i++) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            try{
            PrintWriter pw = new PrintWriter(new FileOutputStream("log.txt", true));
            pw.println(e.getMessage());
            e.printStackTrace(pw);
            pw.flush();
            pw.close();
            }
            catch(IOException ex){
                
            }
            throw new ClientDAOException("Ups! Ocorreu um erro na inserção!");
        }
    }

    public boolean hasClient(LoginProtos.Registo registo) throws ClientDAOException {
        String sql = "SELECT username FROM Client WHERE username = ? ; ";

        try {
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1, registo.getNome());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                rs.close();
                stmt.close();
                return true;
            } else {
                rs.close();
                stmt.close();
                return false;
            }
        } catch (SQLException e) {
            try{
            PrintWriter pw = new PrintWriter(new FileOutputStream("log.txt", true));
            pw.println(e.getMessage());
            e.printStackTrace(pw);
            pw.flush();
            pw.close();
            }
            catch(IOException ex){
                
            }
            throw new ClientDAOException("Ups! Ocorreu um erro na verificação de cliente!");
        }
    }

    public boolean hasClient(LoginProtos.Login login) throws ClientDAOException {
        String sql = "SELECT username FROM Client WHERE username = ? And password = ? ; ";

        try {
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1, login.getEmail());
            stmt.setString(2, login.getPassword());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                rs.close();
                stmt.close();
                return true;
            } else {
                rs.close();
                stmt.close();
                return false;
            }
        } catch (SQLException e) {
            try{
            PrintWriter pw = new PrintWriter(new FileOutputStream("log.txt", true));
            pw.println(e.getMessage());
            e.printStackTrace(pw);
            pw.flush();
            pw.close();
            }
            catch(IOException ex){
                
            }
            throw new ClientDAOException("Ups! Ocorreu um erro na verificação de cliente!");
        }
    }
    
    public byte[] getCert(String cliente) throws ClientDAOException{
        String sql = "SELECT cert FROM Client WHERE username = ? ;";
        
        try{
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1, cliente);
            
            ResultSet rs = stmt.executeQuery();
            
            if(rs.next()){
                byte[] cert = rs.getBytes("cert");
                rs.close();
                stmt.close();
                
                return cert;
                
            }
            else{
                rs.close();
                stmt.close();
                throw new ClientDAOException("O cliente não existe");
            }
        }
        catch(SQLException e){
            try{
            PrintWriter pw = new PrintWriter(new FileOutputStream("log.txt", true));
            pw.println(e.getMessage());
            e.printStackTrace(pw);
            pw.flush();
            pw.close();
            }
            catch(IOException ex){
                
            }
            throw new ClientDAOException("Ups! Ocorreu um erro no retorno do Certificado!");
        }
    }
}
