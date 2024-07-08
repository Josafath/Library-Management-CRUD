/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package libraryproject_;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author kevin
 */
public class AdminDashboard extends javax.swing.JFrame {

    /**
     * Creates new form AdminDashboard
     */
    Connection connection;
    DefaultTableModel model;
    public AdminDashboard(Connection connection) {
        this.connection = connection;
        initComponents();
        this.setLocationRelativeTo(null);
        this.setTitle("Admin Dashboard");
        librarian_list();
    }

    private AdminDashboard() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        namefield = new javax.swing.JTextField();
        lastnamefield = new javax.swing.JTextField();
        usernamefield = new javax.swing.JTextField();
        passwordfield = new javax.swing.JPasswordField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        agefield = new javax.swing.JComboBox<>();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        namefield.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        namefield.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                namefieldActionPerformed(evt);
            }
        });

        lastnamefield.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        usernamefield.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Serif", 2, 24)); // NOI18N
        jLabel1.setText("Admin Dashboard");

        jLabel2.setFont(new java.awt.Font("Serif", 2, 18)); // NOI18N
        jLabel2.setText("Nombre (s):");

        jLabel3.setFont(new java.awt.Font("Serif", 2, 18)); // NOI18N
        jLabel3.setText("Apellido (s):");

        jLabel4.setFont(new java.awt.Font("Serif", 2, 18)); // NOI18N
        jLabel4.setText("Edad:");

        jLabel5.setFont(new java.awt.Font("Serif", 2, 18)); // NOI18N
        jLabel5.setText("Password:");

        jLabel6.setFont(new java.awt.Font("Serif", 2, 18)); // NOI18N
        jLabel6.setText("Username:");

        jButton1.setFont(new java.awt.Font("Serif", 2, 18)); // NOI18N
        jButton1.setText("Agregar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Serif", 2, 18)); // NOI18N
        jButton2.setText("Eliminar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        agefield.setFont(new java.awt.Font("Serif", 2, 14)); // NOI18N
        agefield.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40" }));

        jButton3.setFont(new java.awt.Font("Serif", 2, 18)); // NOI18N
        jButton3.setText("Logout");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(namefield, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, Short.MAX_VALUE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGap(45, 45, 45)))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lastnamefield, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addContainerGap(27, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(agefield, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(usernamefield, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(passwordfield, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(172, 172, 172)
                        .addComponent(jButton1)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(namefield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lastnamefield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(agefield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernamefield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addGap(0, 15, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void namefieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_namefieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_namefieldActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if(namefield.getText().equals("") || lastnamefield.getText().equals("") || usernamefield.getText().equals("") || passwordfield.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Data incomplete", "E R R O R", JOptionPane.ERROR_MESSAGE);
        }
        else {            
            String name, lastname, username, password;
            String age;
            name = namefield.getText();
            lastname = lastnamefield.getText();
            age = agefield.getSelectedItem().toString();
            username = usernamefield.getText();
            password = passwordfield.getText();
                         
            try {
                PreparedStatement stmt = this.connection.prepareStatement("insert into users (name, last_name, age, username, password) values (?,?,?,?,?)", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                
                stmt.setString(1, name);
                stmt.setString(2, lastname);
                stmt.setInt(3, Integer.parseInt(age));
                stmt.setString(4, username);
                stmt.setString(5, password);
            
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "New Librarian Added", "Successful Operation", JOptionPane.INFORMATION_MESSAGE);
                
                namefield.setText("");
                lastnamefield.setText("");
                agefield.setSelectedIndex(0);
                usernamefield.setText("");
                passwordfield.setText("");
                
                librarian_list();
                
            } catch (SQLException ex) {
                Logger.getLogger(AdminDashboard.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if(jTable1.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select some row", "E R R O R", JOptionPane.ERROR_MESSAGE);   
        }
        else {
            int row = jTable1.getSelectedRow();
            try{
                
                PreparedStatement stmt = this.connection.prepareStatement("delete from users where idusers = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                row +=1;
                stmt.setInt(1, row);
                
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Librarian Deleted", "Successful Operation", JOptionPane.INFORMATION_MESSAGE);
                librarian_list();
                    
            } catch (SQLException ex) {
                Logger.getLogger(AdminDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            Principal a = new Principal();
            this.setVisible(false);
            a.setVisible(true);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AdminDashboard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(AdminDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_jButton3ActionPerformed
    public void librarian_list() {
        try {
                PreparedStatement stmt = this.connection.prepareStatement("select * from users",ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                
                ResultSet rs = stmt.executeQuery();
                
                int records = 0;
                if(rs.last()) {
                    records = rs.getRow();
                    rs.first();
                }
                String[] titulos = {"ID", "Name", "Last Name", "Age", "Username"};
                String [][] data = new String[records][5];
                
                for (int i = 0; i < records; i++) {
                    data[i][0] = String.valueOf(rs.getInt("idusers"));
                    data[i][1] = rs.getString("name");
                    data[i][2] = rs.getString("last_name");
                    data[i][3] = String.valueOf(rs.getInt("age"));
                    data[i][4] = rs.getString("username");
                    rs.next();
                }
                
                System.out.println("# de records: " + records);
                
                model = new DefaultTableModel(data, titulos);
                jTable1.setModel(model);
                
            } catch (SQLException ex) {
                Logger.getLogger(AdminDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminDashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> agefield;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField lastnamefield;
    private javax.swing.JTextField namefield;
    private javax.swing.JPasswordField passwordfield;
    private javax.swing.JTextField usernamefield;
    // End of variables declaration//GEN-END:variables
}