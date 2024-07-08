/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package libraryproject_;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
/**
 *
 * @author kevin
 */
public class Principal extends javax.swing.JFrame {

    Connection connection;
    public Principal() throws ClassNotFoundException, SQLException {
        initComponents();
        this.connection = null;
        Class.forName("com.mysql.cj.jdbc.Driver");
        this.connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/libreria",
                "root", "Jossa123#//");
        this.setLocationRelativeTo(null);
        this.setTitle("Welcome !");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        field1 = new javax.swing.JTextField();
        field2 = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Serif", 3, 36)); // NOI18N
        jLabel1.setText("Jossa's Library");

        jLabel2.setFont(new java.awt.Font("Serif", 3, 24)); // NOI18N
        jLabel2.setText("Login");

        field1.setFont(new java.awt.Font("Serif", 2, 14)); // NOI18N
        field1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                field1ActionPerformed(evt);
            }
        });

        field2.setFont(new java.awt.Font("Serif", 2, 14)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Serif", 3, 14)); // NOI18N
        jLabel3.setText("Username");

        jLabel4.setFont(new java.awt.Font("Serif", 3, 14)); // NOI18N
        jLabel4.setText("Password");

        jButton1.setFont(new java.awt.Font("Serif", 2, 14)); // NOI18N
        jButton1.setText("Login");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(186, 186, 186)
                .addComponent(jButton1)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(120, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(field1)
                            .addComponent(field2, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE))
                        .addGap(155, 155, 155))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(183, 183, 183))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(182, 182, 182))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(76, 76, 76))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(field1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(field2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addGap(14, 14, 14))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void field1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_field1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_field1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            String username, password;
            username = field1.getText();
            password = field2.getText();
            PreparedStatement stmt = this.connection.prepareStatement("select * from users where username=? and password=?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            int records = 0;
            
            while(rs.next()) {
                records +=1;
            }
            rs.first();
            
            if(records == 1) {
                
                if(rs.getString("username").equals("jossax123") && rs.getString("password").equals("jossa123#")) {
                    JOptionPane.showMessageDialog(this, "You Login as an Administrator");
                    AdminDashboard a = new AdminDashboard(connection);
                    a.setVisible(true);
                    this.setVisible(false);
                }
                else if(username.equals(rs.getString("username")) && password.equals(rs.getString("password"))) {
                    JOptionPane.showMessageDialog(this, "You Login as a Librarian");
                    LibrarianDashboard l = new LibrarianDashboard(connection);
                    l.setVisible(true);
                    this.setVisible(false);
                }
            }
            else {
                JOptionPane.showMessageDialog(this, "Please enter valid credentials","E R R O R", JOptionPane.ERROR_MESSAGE);
            }
            
        rs.close();
        
        } catch (SQLException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    public static void main(String args[]) {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Principal().setVisible(true);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField field1;
    private javax.swing.JPasswordField field2;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    // End of variables declaration//GEN-END:variables
}
