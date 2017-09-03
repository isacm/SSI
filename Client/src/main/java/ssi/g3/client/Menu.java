/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ssi.g3.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author isacm
 */
public class Menu {
    private List<String> options;
    private Scanner scanner;
    
    public Menu(Scanner scanner){
        this.options = new ArrayList<>();
        this.scanner = scanner;
    }
    
    public Menu(Scanner scanner, List<String> options){
        this.scanner = scanner;
        this.options = new ArrayList<>();
        
        for(String option : options){
            this.options.add(option);
        }
    }
    
    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();
        
        int i = 1;
        for(String option : this.options){
            s.append(i).append(" - ").append(option).append("\n");
            i++;
        }
        
        s.append("0").append(" - Sair!\n");
        
        return s.toString();
    }
    
    public void showMenu(){
        System.out.println(this);
    }
    
    public String chooseOption() {
        int v = -1;
        int size = this.options.size();
        do{
            showMenu();
            v = this.scanner.nextInt();
        }
        while(v < 0 || v > size);
        
        return v == 0 ? "Sair!" : this.options.get(v-1);
    }
}
