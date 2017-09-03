/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ssi.g3.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author isacm
 */
public class ClientBox implements Serializable, Cloneable{
    private Set<Message> inBox;
    
    public ClientBox() {
        this.inBox = new TreeSet<>(
                (Comparator<Message> & Serializable) (o1, o2) -> {
        return o1.compareTo(o2);
    }
        );
    }
    
    public ClientBox(ClientBox box) {
        this.inBox = box.getInBox();
    }
    
    public Set<Message> getInBox() {
        Set<Message> messages = new TreeSet<>(
        (Comparator<Message> & Serializable) (o1, o2) -> {
        return o1.compareTo(o2);}
        );
        
        for(Message m : this.inBox) {
            messages.add(m.clone());
        }
        
        return messages;
    }
    
    public void setInBox(Set<Message> messages) {
        this.inBox = new TreeSet<>((Comparator<Message> & Serializable) (o1, o2) -> {
        return o1.compareTo(o2);
    });
        
        for(Message m : messages) {
            this.inBox.add(m.clone());
        }
    }
    
    public void addInBox(Message m){
        this.inBox.add(m.clone());
    }
    
    public boolean isEmpty(){
        return this.inBox.isEmpty();
    }
    
    @Override
    public int hashCode(){
        int result = 7;
        
        result = 31 * result + this.inBox.hashCode();
        
        return result;
    }
    
    @Override
    public boolean equals(Object obj){
        if(obj == this)
            return true;
        
        if(obj == null || this.getClass() != obj.getClass())
            return false;
        
        ClientBox box = (ClientBox) obj;
        
        return (this.inBox.equals(box.getInBox()));
    }
    
    @Override
    public ClientBox clone(){
        return new ClientBox(this);
    }
    
    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();
        
        for(Message m : this.inBox) {
            s.append(m).append("\n");
        }
        
        return s.toString();
    }
    
    public void gravaObj(String fich) throws IOException {
      ObjectOutputStream oos = new ObjectOutputStream(
                                new FileOutputStream(fich));
      oos.writeObject(this);
      oos.flush(); oos.close();
    }
}
