package libraria;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {

        // Use an appropriate Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | IllegalAccessException
                | InstantiationException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        //Turn off metal's use of bold fonts
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        //Schedule a job for the event dispatch thread:
        //creating and showing this application's UserInterface.
        EventQueue.invokeLater((new UserInterface())::createAndShowGUI);
    }
}