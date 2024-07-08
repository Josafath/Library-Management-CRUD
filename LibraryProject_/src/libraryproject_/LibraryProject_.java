/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package libraryproject_;


import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kevin
 */
public class LibraryProject_ {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Principal project = new Principal();
            project.setVisible(true);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LibraryProject_.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(LibraryProject_.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
