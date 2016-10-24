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
import java.util.ArrayList;

class GUI extends JFrame implements ActionListener, PropertyChangeListener {

    private final JButton listButton = new JButton("...");
    private final JTextField listTextField = new JTextField(100);

    private final JButton folderButton = new JButton("...");
    private final JTextField folderTextField = new JTextField();

    private final JButton copyButton = new JButton ("Копировать");

    private final JProgressBar progressBar = new JProgressBar();

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
        progressBar.setStringPainted(true);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 3;
        c.fill = GridBagConstraints.BOTH;
        container.add(panel, c);
    }

    private void setupEmailLabel(JPanel container) {

        String gitHubURL = "https://github.com/xm-repo/libraria";

        JLabel gitHubLabel = new JLabel("<html><br><font size=2><a href=#>" + gitHubURL + "</a></font></html>");
        gitHubLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gitHubLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(gitHubURL));
                } catch (URISyntaxException | IOException ex) {
                    // ...
                }
            }
        });


        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        container.add(gitHubLabel, c);
    }

    void createAndShowGUI() {

        //for test
        //listTextField.setText("/Users/quake/IdeaProjects/libraria/samples/samples.txt");
        //folderTextField.setText("/Users/quake/Desktop/111");

        //Create and set up the window.
        this.setTitle("libraria");
        this.setSize(600, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        this.getContentPane().add(panel, BorderLayout.CENTER);

        //Create some buttons
        setupCopyButton(panel);
        setupEmailLabel(panel);
        setupPathLine(panel, 0, new JLabel("Список файлов"), listTextField, listButton);
        setupPathLine(panel, 1, new JLabel("Папка"), folderTextField, folderButton);

        //Display the window.
        this.setVisible(true);
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

            copyButton.setEnabled(false);
            progressBar.setValue(0);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Task task = new Task();
            task.addPropertyChangeListener(this);
            task.execute();
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            progressBar.setValue((Integer) evt.getNewValue());
        }
    }

    private class Task extends SwingWorker<Void, Void> {

        private FileLogger mFileLogger;
        private Path mListFile;
        private Path mTargetDirectory;

        @Override
        public Void doInBackground() {

            if(listTextField.getText().trim().isEmpty() || folderTextField.getText().isEmpty()) {
                return null;
            }

            mListFile = Paths.get(listTextField.getText());
            mTargetDirectory = Paths.get(folderTextField.getText());
            mFileLogger =  new FileLogger(mTargetDirectory);

            progressBar.setMinimum(0);
            progressBar.setMaximum(getLinesCount());

            java.util.List<LibrariaDocument> documents = new ArrayList<>();

            int progress = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(mListFile.toString()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if(!line.startsWith("--") && !line.trim().isEmpty()) {
                        LibrariaDocument document = new LibrariaDocument(line, mTargetDirectory, mFileLogger);
                        document.copyTo();
                        documents.add(document);
                        setProgress(progress++);
                    }
                }
            } catch (IOException e) {
                mFileLogger.log("ОШИБКА: " + "не удалось прочитать исходый файл \"" + mListFile.toString() + "\"");
                return null;
            }

            new HtmlReport(documents).writeHTML(mTargetDirectory);
            setProgress(progress);

            return null;
        }

        private int getLinesCount() {
            try (LineNumberReader lnr = new LineNumberReader(new FileReader(new File(mListFile.toString())))) {
                lnr.skip(Long.MAX_VALUE);
                return lnr.getLineNumber();
            } catch (Exception e) {
                mFileLogger.log("ОШИБКА: " + "не удалось прочитать исходый файл \"" + mListFile.toString() + "\"");
                return 0;
            }
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            setCursor(null); //turn off the wait cursor
            copyButton.setEnabled(true);
        }
    }
}
