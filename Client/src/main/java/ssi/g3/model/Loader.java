/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ssi.g3.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author isacm
 */
public class Loader {

    private String caName;
    private String keyStoreType;
    private String keyStoreName;
    private String alias;
    private String passwordKeyStore;
    private String passwordAlias;

    public Loader() throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader("config.cnf"));
        String line;
        
        boolean[] arr = {false, false, false, false, false, false};
        
        while ((line = reader.readLine()) != null) {
            String[] content = line.split("=");

            if (content[0].equalsIgnoreCase("CAname")) {
                this.caName = content[1];
                arr[0] = true;
            } 
            
            else if (content[0].equalsIgnoreCase("keyStoreType")) {
                this.keyStoreType = content[1];
                arr[1] = true;
            } 
            else if (content[0].equalsIgnoreCase("keyStoreName")) {
                this.keyStoreName = content[1];
                arr[2] = true;
            } 
            else if (content[0].equalsIgnoreCase("alias")) {
                this.alias = content[1];
                arr[3] = true;
            } 
            else if (content[0].equalsIgnoreCase("passwordKeyStore")) {
                this.passwordKeyStore = content[1];
                arr[4] = true;
            }
            else if (content[0].equalsIgnoreCase("passwordAlias")) {
                this.passwordAlias = content[1];
                arr[5] = true;
            } 
        }
        
        for(boolean b : arr){
            if(b == false){
                reader.close();
                throw new IOException("Faltam campos no ficheiro de conf");
            }
        }
        
        reader.close();
    }

    public String getCaName() {
        return caName;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public String getKeyStoreName() {
        return keyStoreName;
    }

    public String getAlias() {
        return alias;
    }

    public char[] getPasswordKeyStore() {
        return passwordKeyStore.toCharArray();
    }

    public char[] getPasswordAlias() {
        return passwordAlias.toCharArray();
    }
}
