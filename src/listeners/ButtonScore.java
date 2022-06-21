package listeners;

import models.DataScores;
import models.Model;
import views.View;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;

/**
 * Klass nupu Edetabel jaoks
 */
public class ButtonScore implements ActionListener {
    /**
     * Model
     */
    private Model model;
    /**
     * View
     */
    private View view;
    /**
     * JTable
     */
    private JTable table;
    /**
     * Päise String[] list
     */
    private String[] header = new String[] {"Kuupäev", "Nimi", "Sõna", "Tähed"};
    /**
     * Siia Dialog-i aknasse pannakse edetabeli tabel
     */
    private JDialog dialogScore; // Siin näidatakse edetabelit eraldi aknas

    /**
     * Konstruktor
     * @param model mudel
     * @param view vaade
     */
    public ButtonScore(Model model, View view) {
        this.model = model;
        this.view = view;

        createDialog();
    }

    /**
     * Kui klikitakse nupul Edetabel
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        //JOptionPane.showMessageDialog(null, "Keegi klikkis edetabeli nuppu!");
        model.scoreSelect(); // Loeme kogu sisu uuesti listi
        createScoreboard(); // Teeb dialoogi akna

        view.getTxtChar().requestFocus(); // Peale selle nupu klikkimist anna fookus teksti kastile
    }

    /**
     * Teeb dialog-i akna. Kuna seda on vaja korduvalt kasutada, peab ta olemasolema.
     */
    private void createDialog() {
        dialogScore = new JDialog();
        dialogScore.setPreferredSize(new Dimension(500, 200));
    }

    /**
     * Teeb Edetabeli tabeli sisu valmis ja pannakse dialog-i aknale.
     */
    private void createScoreboard() {
        DefaultTableModel dtm = new DefaultTableModel();
        table = new JTable(dtm);
        table.setDefaultEditor(Object.class, null); //Tabeli lahtreid ei saa muuta

        // Tabeli päis
        for (String column : header) {
            dtm.addColumn(column);
        }
        // Tabeli sisu
        String gametime = null;
        for (DataScores ds : model.getDataScores()) {
            gametime = ds.getGameTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
            String name = ds.getPlayerName();
            String word = ds.getGuessWord();
            String chars = ds.getMissingLetters();
            dtm.addRow(new Object[]{gametime, name, word, chars});
        }
        /**
         * Need read panevad tehtud tabeli dialogi aknale.
         */
        dialogScore.add(new JScrollPane(table));
        dialogScore.setTitle("Edetabel");
        dialogScore.pack();
        dialogScore.setLocationRelativeTo(view); //Edetabel ekraani keskele
        dialogScore.setModal(true); //Ei lase Edetabel nuppu mitu korda vajutada
        dialogScore.setVisible(true);
    }
}
