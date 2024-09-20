import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class CipherGUI extends JFrame {
    private JRadioButton vigenereRB, playfairRB, hillRB;
    private JTextArea inputTextArea, outputTextArea;
    private JTextField keyTextField;
    private JButton uploadButton, encryptButton, decryptButton;

    public CipherGUI() {
        setTitle("Program Cipher");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel untuk memilih metode cipher
        JPanel cipherPanel = new JPanel();
        cipherPanel.setLayout(new FlowLayout());
        vigenereRB = new JRadioButton("Vigenere Cipher", true);
        playfairRB = new JRadioButton("Playfair Cipher");
        hillRB = new JRadioButton("Hill Cipher");
        ButtonGroup group = new ButtonGroup();
        group.add(vigenereRB);
        group.add(playfairRB);
        group.add(hillRB);
        cipherPanel.add(new JLabel("Pilih Metode Cipher:"));
        cipherPanel.add(vigenereRB);
        cipherPanel.add(playfairRB);
        cipherPanel.add(hillRB);

        // Panel untuk input teks
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputTextArea = new JTextArea(5, 50);
        inputPanel.add(new JLabel("Masukkan Pesan:"), BorderLayout.NORTH);
        inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);
        uploadButton = new JButton("Upload File");
        inputPanel.add(uploadButton, BorderLayout.SOUTH);

        // Panel untuk input kunci
        JPanel keyPanel = new JPanel();
        keyPanel.setLayout(new FlowLayout());
        keyTextField = new JTextField(30);
        keyPanel.add(new JLabel("Masukkan Kunci (min. 12 karakter):"));
        keyPanel.add(keyTextField);

        // Panel untuk tombol enkripsi dan dekripsi
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new FlowLayout());
        encryptButton = new JButton("Enkripsi");
        decryptButton = new JButton("Dekripsi");
        actionPanel.add(encryptButton);
        actionPanel.add(decryptButton);

        // Panel untuk output teks
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BorderLayout());
        outputTextArea = new JTextArea(5, 50);
        outputTextArea.setEditable(false);
        outputPanel.add(new JLabel("Hasil:"), BorderLayout.NORTH);
        outputPanel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

        // Menambahkan semua panel ke frame utama
        add(cipherPanel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(keyPanel);
        southPanel.add(actionPanel);
        southPanel.add(outputPanel);
        add(southPanel, BorderLayout.SOUTH);

        // Menambahkan action listeners
        uploadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uploadFile();
            }
        });

        encryptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                encrypt();
            }
        });

        decryptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                decrypt();
            }
        });
    }

    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                String line;
                StringBuilder content = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                inputTextArea.setText(content.toString());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void encrypt() {
        String plaintext = inputTextArea.getText();
        String key = keyTextField.getText();

        if (key.length() < 12) {
            JOptionPane.showMessageDialog(this, "Kunci harus minimal 12 karakter!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String result;
        if (vigenereRB.isSelected()) {
            result = vigenereCipher(plaintext, key, true);
        } else if (playfairRB.isSelected()) {
            result = playfairCipher(plaintext, key, true);
        } else {
            result = hillCipher(plaintext, key, true);
        }

        outputTextArea.setText(result);
    }

    private void decrypt() {
        String ciphertext = inputTextArea.getText();
        String key = keyTextField.getText();

        if (key.length() < 12) {
            JOptionPane.showMessageDialog(this, "Kunci harus minimal 12 karakter!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String result;
        if (vigenereRB.isSelected()) {
            result = vigenereCipher(ciphertext, key, false);
        } else if (playfairRB.isSelected()) {
            result = playfairCipher(ciphertext, key, false);
        } else {
            result = hillCipher(ciphertext, key, false);
        }

        outputTextArea.setText(result);
    }

    private String vigenereCipher(String text, String key, boolean encrypt) {
        StringBuilder result = new StringBuilder();
        text = text.replaceAll("[^a-zA-Z]", "").toUpperCase();
        key = key.toUpperCase();
        int keyLength = key.length();

        for (int i = 0; i < text.length(); i++) {
            char textChar = text.charAt(i);
            char keyChar = key.charAt(i % keyLength);
            int shift = keyChar - 'A';

            if (encrypt) {
                char encryptedChar = (char) ((textChar - 'A' + shift) % 26 + 'A');
                result.append(encryptedChar);
            } else {
                char decryptedChar = (char) ((textChar - keyChar + 26) % 26 + 'A');
                result.append(decryptedChar);
            }
        }

        return result.toString();
    }

    private String playfairCipher(String text, String key, boolean encrypt) {
        // Buat matriks kunci
        char[][] matrix = createPlayfairMatrix(key);
        
        // Siapkan teks
        text = text.replaceAll("[^A-Za-z]", "").toUpperCase().replace("J", "I");
        if (text.length() % 2 != 0) text += "X";
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i += 2) {
            char a = text.charAt(i);
            char b = text.charAt(i + 1);
            int[] posA = findPosition(matrix, a);
            int[] posB = findPosition(matrix, b);
            
            if (posA[0] == posB[0]) { // Baris sama
                result.append(matrix[posA[0]][(posA[1] + (encrypt ? 1 : 4)) % 5]);
                result.append(matrix[posB[0]][(posB[1] + (encrypt ? 1 : 4)) % 5]);
            } else if (posA[1] == posB[1]) { // Kolom sama
                result.append(matrix[(posA[0] + (encrypt ? 1 : 4)) % 5][posA[1]]);
                result.append(matrix[(posB[0] + (encrypt ? 1 : 4)) % 5][posB[1]]);
            } else { // Bentuk persegi
                result.append(matrix[posA[0]][posB[1]]);
                result.append(matrix[posB[0]][posA[1]]);
            }
        }
        
        return result.toString();
    }

    private char[][] createPlayfairMatrix(String key) {
        key = key.toUpperCase().replaceAll("[^A-Z]", "").replace("J", "I");
        boolean[] used = new boolean[26];
        char[][] matrix = new char[5][5];
        int row = 0, col = 0;
        
        // Isi matriks dengan kunci
        for (char c : key.toCharArray()) {
            if (!used[c - 'A']) {
                matrix[row][col] = c;
                used[c - 'A'] = true;
                if (++col == 5) {
                    col = 0;
                    row++;
                }
            }
        }
        
        // Isi sisa matriks dengan huruf yang belum dipakai
        for (char c = 'A'; c <= 'Z'; c++) {
            if (c != 'J' && !used[c - 'A']) {
                matrix[row][col] = c;
                if (++col == 5) {
                    col = 0;
                    row++;
                }
            }
        }
        
        return matrix;
    }

    private int[] findPosition(char[][] matrix, char c) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (matrix[i][j] == c) {
                    return new int[]{i, j};
                }
            }
        }
        return null; // Seharusnya tidak pernah terjadi
    }

    private String hillCipher(String text, String key, boolean encrypt) {
        // Ubah kunci jadi matriks 2x2
        int[][] keyMatrix = new int[2][2];
        int k = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                keyMatrix[i][j] = key.charAt(k++) - 'A';
            }
        }
        
        // Siapkan teks
        text = text.replaceAll("[^A-Za-z]", "").toUpperCase();
        if (text.length() % 2 != 0) text += "X";
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i += 2) {
            int[] pair = {text.charAt(i) - 'A', text.charAt(i+1) - 'A'};
            int[] encrypted = new int[2];
            
            for (int j = 0; j < 2; j++) {
                encrypted[j] = (keyMatrix[j][0] * pair[0] + keyMatrix[j][1] * pair[1]) % 26;
                if (!encrypt) {
                    encrypted[j] = (encrypted[j] + 26) % 26; // Untuk dekripsi
                }
            }
            
            result.append((char)(encrypted[0] + 'A'));
            result.append((char)(encrypted[1] + 'A'));
        }
        
        return result.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CipherGUI().setVisible(true);
            }
        });
    }
}
