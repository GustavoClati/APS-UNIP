package aps;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;


public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JTextField emailField;
    private JPasswordField senhaField;
    private JComboBox<String> tipoUsuarioBox;
    private JButton cadastrarButton;
    private JButton loginButton;

    private static final String MASTER_PASSWORD = "1111"; // Senha mestre para administrador

    public MainFrame() {
        setTitle("Sistema de Help Desk");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Painel de cadastro e login
        
        JPanel cadastroLoginPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        emailField = new JTextField(20);
        senhaField = new JPasswordField(20);
        tipoUsuarioBox = new JComboBox<>(new String[]{"cliente", "admin"});
        cadastrarButton = new JButton("Cadastrar");
        loginButton = new JButton("Login");

        cadastroLoginPanel.add(new JLabel("Email:"));
        cadastroLoginPanel.add(emailField);
        cadastroLoginPanel.add(new JLabel("Senha:"));
        cadastroLoginPanel.add(senhaField);
        cadastroLoginPanel.add(new JLabel("Tipo de Usuário:"));
        cadastroLoginPanel.add(tipoUsuarioBox);
        cadastroLoginPanel.add(cadastrarButton);
        cadastroLoginPanel.add(loginButton);

        mainPanel.add(cadastroLoginPanel, "cadastroLogin");

        // Painel do usuário
        
        JPanel userPanel = new JPanel(new BorderLayout());
        JMenuBar userMenuBar = new JMenuBar();
        JMenuItem cadastroPatrimonioItem = new JMenuItem("Cadastro de Patrimônio");
        userMenuBar.add(cadastroPatrimonioItem);
        userPanel.add(userMenuBar, BorderLayout.NORTH);
        JLabel userLabel = new JLabel("Bem-vindo, Usuário!");
        userLabel.setHorizontalAlignment(JLabel.CENTER);
        userPanel.add(userLabel, BorderLayout.CENTER);
        JButton voltarUserButton = new JButton("Voltar");
        userPanel.add(voltarUserButton, BorderLayout.SOUTH);

        mainPanel.add(userPanel, "user");
        
        // Cadastro de patrimônio 
        
        cadastroPatrimonioItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField nomeField = new JTextField(20);
                JTextField cpfField = new JTextField(20);
                JTextArea observacaoArea = new JTextArea(5, 20);

                JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
                panel.add(new JLabel("Nome:"));
                panel.add(nomeField);
                panel.add(new JLabel("CPF:"));
                panel.add(cpfField);
                panel.add(new JLabel("Observação:"));
                panel.add(observacaoArea);

                int result = JOptionPane.showConfirmDialog(null, panel, "Cadastro de Patrimônio",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    String nome = nomeField.getText().trim();
                    String cpf = cpfField.getText().trim();
                    String observacao = observacaoArea.getText().trim();

                 // Verifica se a observação foi preenchida
                 if (observacao.isEmpty()) {
                     JOptionPane.showMessageDialog(null, "Por favor, preencha a observação.");
                     return; // Sai do método sem cadastrar
                 }

                 try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/helpdesk", "root", "DBadm")) {
                     String sql = "INSERT INTO patrimonio (nome, cpf, observacao) VALUES (?, ?, ?)";
                     try (PreparedStatement statement = connection.prepareStatement(sql)) {
                         statement.setString(1, nome);
                         statement.setString(2, cpf);
                         statement.setString(3, observacao);
                         statement.executeUpdate();
                         JOptionPane.showMessageDialog(null, "Patrimônio cadastrado com sucesso!");
                     }
                 } catch (SQLException ex) {
                     ex.printStackTrace();
                     JOptionPane.showMessageDialog(null, "Erro ao cadastrar patrimônio: " + ex.getMessage());
                 }

                }
            }
        });

        // Painel do administrador
        
        JPanel adminPanel = new JPanel(new BorderLayout());
        JMenuBar adminMenuBar = new JMenuBar();
        JMenuItem gerenciamentoChamadosItem = new JMenuItem("Gerenciamento de Chamados");
        adminMenuBar.add(gerenciamentoChamadosItem);
        adminPanel.add(adminMenuBar, BorderLayout.NORTH);
        JLabel adminLabel = new JLabel("Bem-vindo, Administrador!");
        adminLabel.setHorizontalAlignment(JLabel.CENTER);
        adminPanel.add(adminLabel, BorderLayout.CENTER);
        JButton voltarAdminButton = new JButton("Voltar");
        adminPanel.add(voltarAdminButton, BorderLayout.SOUTH);


        mainPanel.add(adminPanel, "admin");
        
        gerenciamentoChamadosItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/helpdesk", "root", "DBadm")) {
                    String sql = "SELECT * FROM patrimonio";
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        try (ResultSet resultSet = statement.executeQuery()) {
                            DefaultTableModel model = new DefaultTableModel();
                            model.addColumn("ID");
                            model.addColumn("Nome");
                            model.addColumn("CPF");
                            model.addColumn("Observação");

                            while (resultSet.next()) {
                                String id = resultSet.getString("id");
                                String nome = resultSet.getString("nome");
                                String cpf = resultSet.getString("cpf");
                                String observacao = resultSet.getString("observacao");

                                model.addRow(new String[]{id, nome, cpf, observacao});
                            }

                            JTable patrimoniosTable = new JTable(model);
                            patrimoniosTable.setDefaultEditor(Object.class, null);

                            JButton atenderButton = new JButton("Atender");
                            JButton encerrarButton = new JButton("Encerrar");

                            atenderButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    int selectedRow = patrimoniosTable.getSelectedRow();
                                    if (selectedRow != -1) {
                                        String id = (String) patrimoniosTable.getValueAt(selectedRow, 0);
                                        atenderChamado(id); // Chama o método para abrir a janela de chat
                                        Window window = SwingUtilities.windowForComponent(patrimoniosTable);
                                        window.dispose(); // Fecha a janela de gerenciamento de chamados
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Selecione um chamado para atender.");
                                    }
                                }
                            });




                            encerrarButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    int selectedRow = patrimoniosTable.getSelectedRow();
                                    if (selectedRow != -1) {
                                        String id = (String) patrimoniosTable.getValueAt(selectedRow, 0);
                                        encerrarChamado(id);
                                        model.removeRow(selectedRow);
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Selecione um chamado para encerrar.");
                                    }
                                }
                            });

                            JPanel panel = new JPanel(new BorderLayout());
                            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
                            buttonPanel.add(atenderButton);
                            buttonPanel.add(encerrarButton);

                            panel.add(new JScrollPane(patrimoniosTable), BorderLayout.CENTER);
                            panel.add(buttonPanel, BorderLayout.SOUTH);          
                            JOptionPane.showMessageDialog(null, panel, "Gerenciamento de Patrimônios", JOptionPane.PLAIN_MESSAGE);
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Erro ao carregar patrimônios: " + ex.getMessage());
                }
            }
        });

        add(mainPanel);

        cadastrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cadastrarUsuario();
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        voltarUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "cadastroLogin");
            }
        });

        voltarAdminButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "cadastroLogin");
            }
        });

        setLocationRelativeTo(null);
    }
    
    // Cadastro de usuário

    private void cadastrarUsuario() {
        String email = emailField.getText().trim();
        String senha = new String(senhaField.getPassword()).trim();
        String tipoUsuario = (String) tipoUsuarioBox.getSelectedItem();

        if (tipoUsuario.equals("admin")) {
            String masterPassword = JOptionPane.showInputDialog(this, "Digite a senha mestre:");
            if (!MASTER_PASSWORD.equals(masterPassword)) {
                JOptionPane.showMessageDialog(this, "Senha mestre incorreta.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Validação dos campos de email e senha
        
        if (email.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email e senha são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/helpdesk", "root", "DBadm")) {
            String sql = "INSERT INTO pessoa (email, senha, tipo_usuario) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, email);
                statement.setString(2, senha);
                statement.setString(3, tipoUsuario);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Usuário cadastrado com sucesso!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar usuário: " + ex.getMessage());
        }
    }

    // Login 
    
    private void login() {
        String email = emailField.getText().trim();
        String senha = new String(senhaField.getPassword()).trim();

        // Validação dos campos de email e senha
        
        if (email.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email e senha são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/helpdesk", "root", "DBadm")) {
            String sql = "SELECT tipo_usuario FROM pessoa WHERE email = ? AND senha = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, email);
                statement.setString(2, senha);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String tipoUsuario = resultSet.getString("tipo_usuario");
                        if (tipoUsuario.equals("admin")) {
                            String masterPassword = JOptionPane.showInputDialog(this, "Digite a senha mestre:");
                            if (!MASTER_PASSWORD.equals(masterPassword)) {
                                JOptionPane.showMessageDialog(this, "Senha mestre incorreta.", "Erro", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        JOptionPane.showMessageDialog(this, "Login bem-sucedido como " + tipoUsuario);
                        if (tipoUsuario.equals("cliente")) {
                            cardLayout.show(mainPanel, "user");
                        } else {
                            cardLayout.show(mainPanel, "admin");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Email ou senha incorretos.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao fazer login: " + ex.getMessage());
        }
    }
    
    // Caso clicado em atender o chamado
    
    private void atenderChamado(String id) {
        JDialog dialog = new JDialog(this, "Chat do Chamado " + id, Dialog.ModalityType.MODELESS);
        dialog.setSize(400, 300);

        JTextArea chatArea = new JTextArea(10, 30);
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        JTextField chatInputField = new JTextField(30);
        JButton sendButton = new JButton("Enviar");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = chatInputField.getText().trim();
                if (!message.isEmpty()) {
                    chatArea.append("Admin: " + message + "\n");
                    chatInputField.setText("");
                }
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(chatInputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(inputPanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(null); // Centraliza a janela
        dialog.setVisible(true);
    }

    // Caso clicado em encerrar chamado
    
    private void encerrarChamado(String id) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/helpdesk", "root", "DBadm")) {
            String sql = "DELETE FROM patrimonio WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, id);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "Chamado encerrado com sucesso!");
                } else {
                    JOptionPane.showMessageDialog(null, "Não foi possível encerrar o chamado.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao encerrar chamado: " + ex.getMessage());
        }
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
