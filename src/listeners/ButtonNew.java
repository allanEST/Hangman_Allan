package listeners;

import models.Model;
import views.View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Klass nupu Uus mäng jaoks
 */
public class ButtonNew implements ActionListener {
    /**
     * Model
     */
    private Model model;
    /**
     * View
     */
    private View view;

    /**
     * Konstruktor
     * @param model Model
     * @param view View
     */
    public ButtonNew(Model model, View view) {
        this.model = model;
        this.view = view;
    }

    /**
     * Kui klikitakse nupul Uus mäng
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        //JOptionPane.showMessageDialog(null, "Klikiti nupul Uus mäng");

        view.setStartGame();
        view.getTxtChar().requestFocus(); // Peale selle nupu klikkimist anna fookus teksti kastile

        String category = view.getCmbCategory().getSelectedItem().toString(); //Kontrollib valitud kategooriat
        model.setRandomWordByCategory(category); //Valitud kategooria põhjal juhuslik sõna
        String addSpaces = model.spaceBetweenChars(model.getHiddenWord().toString());
        view.getLblGuessWord().setText(addSpaces); //Paneb sõna lblGuessWord'ile
        //System.out.println(model.getRandomWord()); //Kontrolliks sõna terminali
    }
}
