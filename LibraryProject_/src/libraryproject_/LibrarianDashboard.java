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
public class LibrarianDashboard extends javax.swing.JFrame {

    /**
     * 
     */
    Connection connection;
    DefaultTableModel model;
    public LibrarianDashboard(Connection connection) {
        this.connection = connection;
        initComponents();
        this.setLocationRelativeTo(null);
        this.setTitle("Librarian Dashboard");
        librarian_list();
    }

    private LibrarianDashboard() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    
    public void librarian_list() {
        try {
                PreparedStatement stmt = this.connection.prepareStatement("select * from book",ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                
                ResultSet rs = stmt.executeQuery();
                
               
                int records = 0;
                if(rs.last()) {
                    records = rs.getRow();
                    rs.first();
                }
                String[] titulos = {"ID", "Titulo", "Autor", "Genero", "Status", "Editorial"};
                String [][] data = new String[records][6];
                
                for (int i = 0; i < records; i++) {
                    data[i][0] = String.valueOf(rs.getInt("idbook"));
                    data[i][1] = rs.getString("title");
                    data[i][2] = rs.getString("author");
                    data[i][3] = rs.getString("genre");
                    if(rs.getInt("status") == 1){
                        data[i][4] = "Available";
                    } else data[i][4] = "Occupied";
                    
                    data[i][5] = rs.getString("editorial");
                    rs.next();
                }
                
                System.out.println("# de records: " + records);
                
                model = new DefaultTableModel(data, titulos);
                jTable1.setModel(model);
                
            } catch (SQLException ex) {
                Logger.getLogger(AdminDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        authorfield = new javax.swing.JTextField();
        titlefield = new javax.swing.JTextField();
        editorialfield = new javax.swing.JTextField();
        genrefield = new javax.swing.JTextField();
        statusfield = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setFont(new java.awt.Font("Serif", 3, 24)); // NOI18N
        jLabel1.setText("Librarian Dashboard");

        authorfield.setFont(new java.awt.Font("Serif", 2, 14)); // NOI18N

        titlefield.setFont(new java.awt.Font("Serif", 2, 14)); // NOI18N

        editorialfield.setFont(new java.awt.Font("Serif", 2, 14)); // NOI18N

        genrefield.setFont(new java.awt.Font("Serif", 2, 14)); // NOI18N

        statusfield.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Occupied", "Available", " " }));

        jLabel2.setFont(new java.awt.Font("Serif", 2, 14)); // NOI18N
        jLabel2.setText("Title:");

        jLabel3.setFont(new java.awt.Font("Serif", 2, 14)); // NOI18N
        jLabel3.setText("Author:");

        jLabel4.setFont(new java.awt.Font("Serif", 2, 14)); // NOI18N
        jLabel4.setText("Genre:");

        jLabel5.setFont(new java.awt.Font("Serif", 2, 14)); // NOI18N
        jLabel5.setText("Status:");

        jLabel6.setFont(new java.awt.Font("Serif", 2, 14)); // NOI18N
        jLabel6.setText("Editorial:");

        jButton1.setFont(new java.awt.Font("Serif", 2, 15)); // NOI18N
        jButton1.setText("Agregar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Serif", 2, 15)); // NOI18N
        jButton2.setText("Eliminar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Serif", 2, 15)); // NOI18N
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
                .addContainerGap()
                .addComponent(jScrollPane1))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(titlefield, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(authorfield, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(151, 151, 151)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(genrefield, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(37, 37, 37)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(statusfield, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(editorialfield, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(46, 46, 46)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titlefield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authorfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editorialfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(genrefield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addComponent(jButton1)
                .addGap(22, 22, 22))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if(jTable1.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select some row", "E R R O R", JOptionPane.ERROR_MESSAGE);   
        }
        else {
            int row = jTable1.getSelectedRow();
            try{
                
                PreparedStatement stmt = this.connection.prepareStatement("delete from book where idbook = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                row +=1;
                stmt.setInt(1, row);
                
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Book Deleted", "Successful Operation", JOptionPane.INFORMATION_MESSAGE);
                librarian_list();
                    
            } catch (SQLException ex) {
                Logger.getLogger(AdminDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if(titlefield.getText().equals("") || authorfield.getText().equals("") || genrefield.getText().equals("") || editorialfield.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Data incomplete", "E R R O R", JOptionPane.ERROR_MESSAGE);
        }
        else {
            String title, author, genre, editorial;
            int status;
            title = titlefield.getText();
            author = authorfield.getText();
            genre = genrefield.getText();
            status = statusfield.getSelectedIndex();
            editorial = editorialfield.getText();
            
            try {
                PreparedStatement stmt = this.connection.prepareStatement("insert into book (title, author, genre, status, editorial) values (?,?,?,?,?)", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                stmt.setString(1, title);
                stmt.setString(2, author);
                stmt.setString(3, genre);
                stmt.setInt(4, status);
                stmt.setString(5, editorial);
            
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "New Book Added", "Successful Operation", JOptionPane.INFORMATION_MESSAGE);
                
                titlefield.setText("");
                authorfield.setText("");
                statusfield.setSelectedIndex(0);
                editorialfield.setText("");
                genrefield.setText("");
                
                librarian_list();
                
            } catch (SQLException ex) {
                Logger.getLogger(AdminDashboard.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LibrarianDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LibrarianDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LibrarianDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LibrarianDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LibrarianDashboard().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField authorfield;
    private javax.swing.JTextField editorialfield;
    private javax.swing.JTextField genrefield;
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
    private javax.swing.JComboBox<String> statusfield;
    private javax.swing.JTextField titlefield;
    // End of variables declaration//GEN-END:variables
}
