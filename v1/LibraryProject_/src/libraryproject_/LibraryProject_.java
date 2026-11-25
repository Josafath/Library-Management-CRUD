/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package libraryproject_;

/**
 * Entry point of the app: opens the login window.
 *
 * @author kevin
 */
public class LibraryProject_ {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Swing windows must be created on the Event Dispatch Thread.
        // showLogin() displays an error dialog if MySQL is not reachable.
        java.awt.EventQueue.invokeLater(Principal::showLogin);
    }

}
