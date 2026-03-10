import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Stego_patched.java
 *
 * Improved and patched Swing GUI for the Stegenography class.
 * - Uses PNG (lossless) for watermark and stego outputs
 * - Robust error handling with stack traces
 * - Uses SwingWorker for long operations so UI doesn't freeze
 * - Modernized professional layout with status bar and tooltips
 * - Save retrieved text via Save dialog (instead of relying on saved state)
 *
 * Keep Stegenography.java in the same folder (patched version expected).
 * Compile with: javac Stegenography.java Stego_patched.java
 * Run with: java Stego_patched
 */
public class Stego_patched extends JFrame {
    private static final long serialVersionUID = 1L;

    // JFileChooser shared
    private final JFileChooser fileChooser = new JFileChooser();

    // Components: Watermark tab
    private JTextField tfWatermarkImage;
    private JTextField tfWatermarkOutput;
    private JTextField tfWatermarkText;
    private JButton btnWatermarkBrowse;
    private JButton btnWatermarkApply;

    // Components: Hide tab
    private JTextField tfHideImage;
    private JTextField tfHideFile;
    private JButton btnHideImageBrowse;
    private JButton btnHideFileBrowse;
    private JButton btnHideStart;

    // Components: Retrieve tab
    private JTextField tfRetrieveImage;
    private JTextArea taRetrieved;
    private JButton btnRetrieveBrowse;
    private JButton btnRetrieveStart;
    private JButton btnSaveRetrieved;

    // Status bar
    private JLabel lblStatus;

    public Stego_patched() {
        setTitle("Stego - Watermark & LSB Steganography");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(820, 520);
        setLocationRelativeTo(null);
        initLookAndFeel();
        initComponents();
    }

    private void initLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // fallback silently
        }
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Watermark", createWatermarkPanel());
        tabs.addTab("Hide Data", createHidePanel());
        tabs.addTab("Retrieve", createRetrievePanel());

        lblStatus = new JLabel("Ready");
        lblStatus.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        root.add(tabs, BorderLayout.CENTER);
        root.add(createTopBanner(), BorderLayout.NORTH);
        root.add(createStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JComponent createTopBanner() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(6, 6, 10, 6));

        JLabel title = new JLabel("Stego — Watermark & LSB Steganography");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JLabel subtitle = new JLabel("Lossless watermarking and simple LSB hide/retrieve for text files");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setForeground(Color.DARK_GRAY);

        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);
        text.add(title);
        text.add(subtitle);

        p.add(text, BorderLayout.WEST);

        return p;
    }

    private JComponent createStatusBar() {
        JPanel s = new JPanel(new BorderLayout());
        s.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), new EmptyBorder(4, 6, 4, 6)));
        s.add(lblStatus, BorderLayout.WEST);
        return s;
    }

    private JPanel createWatermarkPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;

        panel.add(new JLabel("Source Image (PNG/JPG):"), gbc);
        tfWatermarkImage = new JTextField();
        tfWatermarkImage.setToolTipText("Choose an image file to apply watermark (PNG recommended)");
        gbc.gridx = 1; gbc.weightx = 1.0; panel.add(tfWatermarkImage, gbc);
        btnWatermarkBrowse = new JButton("Browse");
        btnWatermarkBrowse.setToolTipText("Browse for source image");
        gbc.gridx = 2; gbc.weightx = 0.0; panel.add(btnWatermarkBrowse, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Watermark Text:"), gbc);
        tfWatermarkText = new JTextField("© MyLab");
        tfWatermarkText.setToolTipText("Text that will be drawn on the image as a visible watermark");
        gbc.gridx = 1; gbc.gridwidth = 2; panel.add(tfWatermarkText, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Output Image (PNG):"), gbc);
        tfWatermarkOutput = new JTextField();
        tfWatermarkOutput.setToolTipText("Path where the watermarked PNG will be saved (choose a .png file)");
        gbc.gridx = 1; gbc.weightx = 1.0; panel.add(tfWatermarkOutput, gbc);
        JButton btnWatermarkSaveAs = new JButton("Choose...");
        btnWatermarkSaveAs.setToolTipText("Choose output file (will be saved as PNG)");
        gbc.gridx = 2; gbc.weightx = 0.0; panel.add(btnWatermarkSaveAs, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST;
        btnWatermarkApply = new JButton("Apply Watermark");
        btnWatermarkApply.setToolTipText("Apply watermark and save output as PNG (lossless)");
        panel.add(btnWatermarkApply, gbc);

        // Actions
        btnWatermarkBrowse.addActionListener(e -> browseForImage(tfWatermarkImage));
        btnWatermarkSaveAs.addActionListener(e -> saveAsPng(tfWatermarkOutput));
        btnWatermarkApply.addActionListener(e -> applyWatermarkAsync());

        return panel;
    }

    private JPanel createHidePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Cover Image (PNG recommended):"), gbc);
        tfHideImage = new JTextField(); tfHideImage.setToolTipText("The image that will carry the hidden data (PNG recommended)");
        gbc.gridx = 1; gbc.weightx = 1.0; panel.add(tfHideImage, gbc);
        btnHideImageBrowse = new JButton("Browse"); gbc.gridx = 2; gbc.weightx = 0.0; panel.add(btnHideImageBrowse, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("File to Hide (text or binary):"), gbc);
        tfHideFile = new JTextField(); tfHideFile.setToolTipText("Choose a file to embed into the image");
        gbc.gridx = 1; gbc.weightx = 1.0; panel.add(tfHideFile, gbc);
        btnHideFileBrowse = new JButton("Browse"); gbc.gridx = 2; panel.add(btnHideFileBrowse, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.gridwidth = 2;
        btnHideStart = new JButton("Hide File into Image");
        btnHideStart.setToolTipText("Embed the selected file into the chosen image (creates <name>Msg.png)");
        panel.add(btnHideStart, gbc);

        // Actions
        btnHideImageBrowse.addActionListener(e -> browseForImage(tfHideImage));
        btnHideFileBrowse.addActionListener(e -> browseForAnyFile(tfHideFile));
        btnHideStart.addActionListener(e -> hideFileAsync());

        return panel;
    }

    private JPanel createRetrievePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.0;
        panel.add(new JLabel("Stego Image (PNG):"), gbc);
        tfRetrieveImage = new JTextField(); tfRetrieveImage.setToolTipText("Select an image that may contain hidden data");
        gbc.gridx = 1; gbc.weightx = 1.0; panel.add(tfRetrieveImage, gbc);
        btnRetrieveBrowse = new JButton("Browse"); gbc.gridx = 2; gbc.weightx = 0.0; panel.add(btnRetrieveBrowse, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        taRetrieved = new JTextArea(); taRetrieved.setEditable(false); taRetrieved.setLineWrap(true); taRetrieved.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(taRetrieved);
        sp.setPreferredSize(new Dimension(600, 220));
        panel.add(sp, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridy = 2; gbc.weighty = 0.0; gbc.gridwidth = 1; gbc.gridx = 1; gbc.anchor = GridBagConstraints.EAST;
        btnRetrieveStart = new JButton("Retrieve Text"); btnSaveRetrieved = new JButton("Save Retrieved...");
        btnSaveRetrieved.setEnabled(false);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btns.add(btnRetrieveStart); btns.add(btnSaveRetrieved);
        gbc.gridx = 1; panel.add(btns, gbc);

        // Actions
        btnRetrieveBrowse.addActionListener(e -> browseForImage(tfRetrieveImage));
        btnRetrieveStart.addActionListener(e -> retrieveAsync());
        btnSaveRetrieved.addActionListener(e -> saveRetrievedText());

        return panel;
    }

    // Helper: show file chooser restricted to images
    private void browseForImage(JTextField target) {
        fileChooser.resetChoosableFileFilters();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files (png, jpg, jpeg)", "png", "jpg", "jpeg");
        fileChooser.setFileFilter(filter);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            target.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    // Helper: show file chooser for any file
    private void browseForAnyFile(JTextField target) {
        fileChooser.resetChoosableFileFilters();
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            target.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    // Helper: Save As dialog for PNG
    private void saveAsPng(JTextField target) {
        fileChooser.resetChoosableFileFilters();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG file", "png");
        fileChooser.setFileFilter(filter);
        fileChooser.setSelectedFile(new File("watermarked.png"));
        int ret = fileChooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            String path = f.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".png")) path = path + ".png";
            target.setText(path);
        }
    }

    // Apply watermark in background
    private void applyWatermarkAsync() {
        final String src = tfWatermarkImage.getText().trim();
        final String out = tfWatermarkOutput.getText().trim();
        final String watermark = tfWatermarkText.getText();
        if (src.isEmpty() || out.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please choose source image and output path.", "Missing input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        btnWatermarkApply.setEnabled(false);
        lblStatus.setText("Applying watermark...");
        new SwingWorker<Boolean, Void>() {
            Exception ex = null;
            @Override protected Boolean doInBackground() {
                try {
                    File file = new File(src);
                    if (!file.exists()) throw new FileNotFoundException(src);
                    BufferedImage srcImg = ImageIO.read(file);
                    if (srcImg == null) throw new IOException("Unsupported image format");

                    BufferedImage bufferedImage = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D g2d = bufferedImage.createGraphics();
                    g2d.drawImage(srcImg, 0, 0, null);

                    // watermark styling
                    AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f);
                    g2d.setComposite(alpha);
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(18, srcImg.getWidth() / 20)));
                    FontMetrics fm = g2d.getFontMetrics();
                    Rectangle bounds = fm.getStringBounds(watermark, g2d).getBounds();

                    int x = (srcImg.getWidth() - bounds.width) / 2;
                    int y = (srcImg.getHeight() - bounds.height) / 2 + fm.getAscent();

                    g2d.setColor(new Color(255, 255, 255, 200));
                    g2d.drawString(watermark, x, y);
                    g2d.dispose();

                    // write PNG (lossless)
                    File outFile = new File(out);
                    ImageIO.write(bufferedImage, "png", outFile);
                    return true;
                } catch (Exception e) {
                    ex = e;
                    e.printStackTrace();
                    return false;
                }
            }
            @Override protected void done() {
                btnWatermarkApply.setEnabled(true);
                try {
                    boolean ok = get();
                    if (ok) {
                        lblStatus.setText("Watermark applied — saved to " + out);
                        JOptionPane.showMessageDialog(Stego_patched.this, "Watermark saved to:\n" + out, "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        lblStatus.setText("Watermark failed");
                        JOptionPane.showMessageDialog(Stego_patched.this, "Watermark failed: " + (ex != null ? ex.getMessage() : "unknown"), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // Hide file (call Stegenography.hide) in background
    private void hideFileAsync() {
        final String img = tfHideImage.getText().trim();
        final String fileToHide = tfHideFile.getText().trim();
        if (img.isEmpty() || fileToHide.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select both the cover image and the file to hide.", "Missing input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        btnHideStart.setEnabled(false);
        lblStatus.setText("Hiding file into image...");

        new SwingWorker<Boolean, Void>() {
            Exception ex = null;
            @Override protected Boolean doInBackground() {
                try {
                    // Using Stegenography.hide(textFnm, imFnm) where textFnm is the file to hide
                    boolean ok = Stegenography.hide(fileToHide, img);
                    return ok;
                } catch (Exception e) {
                    ex = e; e.printStackTrace(); return false;
                }
            }
            @Override protected void done() {
                btnHideStart.setEnabled(true);
                try {
                    boolean ok = get();
                    if (ok) {
                        lblStatus.setText("File hidden successfully. Output saved as <name>Msg.png");
                        JOptionPane.showMessageDialog(Stego_patched.this, "File embedded successfully. Output image: <originalName>Msg.png", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        lblStatus.setText("Hiding failed");
                        JOptionPane.showMessageDialog(Stego_patched.this, "Hiding failed. See console for details.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    // Retrieve hidden message asynchronously
    private void retrieveAsync() {
        final String img = tfRetrieveImage.getText().trim();
        if (img.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an image to retrieve from.", "Missing input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        btnRetrieveStart.setEnabled(false);
        lblStatus.setText("Retrieving message...");
        taRetrieved.setText("");
        btnSaveRetrieved.setEnabled(false);

        new SwingWorker<String, Void>() {
            Exception ex = null;
            @Override protected String doInBackground() {
                try {
                    String message = Stegenography.reveal(img);
                    return message;
                } catch (Exception e) { ex = e; e.printStackTrace(); return null; }
            }
            @Override protected void done() {
                btnRetrieveStart.setEnabled(true);
                try {
                    String result = get();
                    if (result == null) {
                        lblStatus.setText("Retrieve failed");
                        JOptionPane.showMessageDialog(Stego_patched.this, "Could not retrieve message (see console)", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        taRetrieved.setText(result);
                        btnSaveRetrieved.setEnabled(true);
                        lblStatus.setText("Retrieve completed");
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void saveRetrievedText() {
        String text = taRetrieved.getText();
        if (text == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nothing to save.", "No data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        fileChooser.resetChoosableFileFilters();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text file", "txt");
        fileChooser.setFileFilter(filter);
        fileChooser.setSelectedFile(new File("retrieved.txt"));
        int ret = fileChooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File out = fileChooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(out)) {
                fw.write(text);
                lblStatus.setText("Saved retrieved text to " + out.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Saved to:\n" + out.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to save: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Stego_patched app = new Stego_patched();
            app.setVisible(true);
        });
    }
}
