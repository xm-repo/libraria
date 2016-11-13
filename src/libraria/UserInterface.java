package libraria;

import libraria.document.LibrariaDocument;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

class UserInterface extends JFrame implements ActionListener, PropertyChangeListener {

    private final JButton listButton = new JButton("...");
    private final JTextField listTextField = new JTextField(100);

    private final JButton folderButton = new JButton("...");
    private final JTextField folderTextField = new JTextField();

    private final JButton copyButton = new JButton ("Копировать");
    private final JProgressBar progressBar = new JProgressBar();

    private final JCheckBox cpyCheckBox = new JCheckBox("Копировать");
    private final JCheckBox repCheckBox = new JCheckBox("Отчет");
    private final JCheckBox utcCheckBox = new JCheckBox("Время UTC");

    void createAndShowGUI() {

        //for test
        //listTextField.setText("/Users/quake/Desktop/samples/samples2.txt");
        //folderTextField.setText("/Users/quake/Desktop/111");

        //Create and set up the window.
        this.setTitle("libraria");
        this.setSize(600, 250);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        this.getContentPane().add(panel, BorderLayout.CENTER);

        //Create some controls
        setupOptions(panel);
        setupCopyButton(panel);
        setupGHLabel(panel);
        setupPathLine(panel, 0, new JLabel("Список файлов"), listTextField, listButton);
        setupPathLine(panel, 1, new JLabel("Папка"), folderTextField, folderButton);

        //Display the window.
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void setupPathLine(JPanel container, int line, JLabel label, JTextField textField, JButton button) {

        GridBagConstraints c;
        final Insets insets = new Insets(4, 4, 4, 4);
        final Insets buttonInsets = new Insets(4, 4, 4, 8);

        c = new GridBagConstraints();
        c.insets = insets;
        c.gridx = 0;
        c.gridy = line;
        container.add(label, c);

        c = new GridBagConstraints();
        c.insets = insets;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = line;
        c.weightx = 0.1;
        container.add(textField, c);

        c = new GridBagConstraints();
        c.insets = buttonInsets;
        c.gridx = 2;
        c.gridy = line;
        container.add(button, c);
        button.addActionListener(this);
    }

    private void setupCopyButton(JPanel container) {

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        panel.add(copyButton);
        copyButton.addActionListener(this);

        panel.add(progressBar);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setStringPainted(true);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        c.fill = GridBagConstraints.BOTH;
        container.add(panel, c);
    }

    private void setupGHLabel(JPanel container) {

        String gitHubURL = "https://github.com/xm-repo/libraria";

        JLabel gitHubLabel = new JLabel("<html><br><font size=2><a href=#>" + gitHubURL + "</a></font></html>");
        gitHubLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gitHubLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(gitHubURL));
                } catch (URISyntaxException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 5;
        container.add(gitHubLabel, c);
    }

    private void setupOptions(JPanel container) {

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        cpyCheckBox.setSelected(true); panel.add(cpyCheckBox);
        repCheckBox.setSelected(true); panel.add(repCheckBox);
        utcCheckBox.setSelected(true); panel.add(utcCheckBox);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 3;
        c.fill = GridBagConstraints.BOTH;
        container.add(panel, c);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == listButton) {

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Список файлов");

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                listTextField.setText(file.getPath());
            }

        } else if(e.getSource() == folderButton) {

            JFileChooser folderChooser = new JFileChooser();
            folderChooser.setDialogTitle("Папка для файлов");
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            folderChooser.setAcceptAllFileFilterUsed(false);

            if (folderChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = folderChooser.getSelectedFile();
                folderTextField.setText(file.getPath());
            }

        } else if(e.getSource() == copyButton) {

            if(listTextField.getText().trim().isEmpty() || folderTextField.getText().isEmpty()) {
                return;
            }

            cpyCheckBox.setEnabled(false);
            repCheckBox.setEnabled(false);
            utcCheckBox.setEnabled(false);
            copyButton.setEnabled(false);
            progressBar.setValue(0);

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Worker worker = new Worker(listTextField.getText(), folderTextField.getText());
            worker.addPropertyChangeListener(this);
            worker.execute();
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            progressBar.setValue((Integer) evt.getNewValue());
        }
    }

    private class Worker extends SwingWorker<Void, Integer> {

        private final FileLogger mFileLogger;
        private final Path mListFile;
        private final Path mTargetDirectory;
        private final HtmlReport mHtmlReport;
        int mLines = 1;

        Worker(String inFile, String targetDir) {
            mListFile = Paths.get(inFile);
            mTargetDirectory = Paths.get(targetDir);
            mFileLogger = new FileLogger(mTargetDirectory);
            mHtmlReport = new HtmlReport(mTargetDirectory, utcCheckBox.isSelected());
            mLines = getLinesCount();
        }

        @Override
        public Void doInBackground() {

            int line = 1;
            try (BufferedReader br = new BufferedReader(new FileReader(mListFile.toString()))) {
                if(repCheckBox.isSelected()) {
                    mHtmlReport.writeHeader();
                }
                String docFile;
                while ((docFile = br.readLine()) != null) {
                    if(!docFile.startsWith("--") && !docFile.trim().isEmpty()) {
                        LibrariaDocument document = new LibrariaDocument(docFile, mFileLogger);
                        if(cpyCheckBox.isSelected()) {
                            document.copyTo(mTargetDirectory);
                        }
                        if(repCheckBox.isSelected()) {
                            mHtmlReport.writeDocument(document);
                        }
                    }
                    int progress = line++ * 100 / mLines;
                    setProgress(progress > 100 ? 99 : progress);
                }
                if(repCheckBox.isSelected()) {
                    mHtmlReport.writeFooter();
                }

            } catch (IOException e) {
                e.printStackTrace();
                mFileLogger.log("ОШИБКА: " + "не удалось прочитать исходый файл \"" + mListFile.toString() + "\"");
                return null;
            }

            setProgress(100);

            return null;
        }

        private int getLinesCount() {
            try (LineNumberReader lnr = new LineNumberReader(new FileReader(new File(mListFile.toString())))) {
                lnr.skip(Long.MAX_VALUE);
                return lnr.getLineNumber();
            } catch (Exception e) {
                e.printStackTrace();
                mFileLogger.log("ОШИБКА: " + "не удалось прочитать исходый файл \"" + mListFile.toString() + "\"");
                return 1;
            }
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            setCursor(null); //turn off the wait cursor

            cpyCheckBox.setEnabled(true);
            repCheckBox.setEnabled(true);
            utcCheckBox.setEnabled(true);
            copyButton.setEnabled(true);
        }
    }
}
