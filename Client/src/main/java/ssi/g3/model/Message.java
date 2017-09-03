/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ssi.g3.model;

import java.io.Serializable;

/**
 *
 * @author isacm
 */
public class Message implements Serializable, Cloneable, Comparable<Message>{
    
    private Long id;
    private String sender;
    private String subject;
    private String content;
    
    public Message(Long id, String sender, String subject, String content) {
        this.id = id;
        this.sender = sender;
        this.subject = subject;
        this.content = content;
    }

    public Message(Message m) {
        this.id = m.getId();
        this.sender = m.getSender();
        this.subject = m.getSubject();
        this.content = m.getContent();
    }
    
    public Long getId(){
        return this.id;
    }

    public String getSender() {
        return sender;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }
    
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setContent(String content) {
        this.content = content;
    }
    

    @Override
    public int hashCode(){
        int result = 7;
        
        result = 31 * result + this.id.hashCode();
        result = 31 * result + this.sender.hashCode();
        result = 31 * result + this.subject.hashCode();
        result = 31 * result + this.content.hashCode();
        
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        
        if(obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        
        Message m = (Message) obj;
        
        return (this.id.equals(m.getId()));
    }
    
    @Override
    public Message clone() {
        return new Message(this);
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        
            s.append("Sender: ").append(this.sender).append("\n").append("Subject: ")
            .append(this.subject)
            .append("\n")
            .append("Content: ")
            .append(this.content)
            .append("\n");
        
        return s.toString();
    }

    @Override
    public int compareTo(Message t) {
        return - this.id.compareTo(t.id);
    }
}
