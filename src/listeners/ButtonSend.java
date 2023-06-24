package listeners;

import models.DataScores;
import models.Model;
import views.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;

/**
 * Klass nupu Saada täht jaoks
 */
public class ButtonSend implements ActionListener {
    /**
     * Mudel
     */
    private Model model;
    /**
     * View
     */
    private View view;

    /**
     * Konstuktor
     * @param model Model
     * @param view View
     */
    public ButtonSend(Model model, View view) {
        this.model = model;
        this.view = view;
    }

    /**
     * Kui kliikida nupul Saada täht
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        //JOptionPane.showMessageDialog(null, "Kes vajutas nuppu/Enter: " + view.getTxtChar().getText().toUpperCase());
        try { // Lisatud try, catch, sest kui vajutada saada nuppu ilma sisestamata, viskab errori
            view.getTxtChar().requestFocus(); // Peale selle nupu klikkimist anna fookus tekstikastile
            String guessedStringChar = view.getTxtChar().getText().toUpperCase();
            char guessedChar = guessedStringChar.charAt(0);
            String[] wordCharArray = model.getRandomWord().split(""); //Teeb arvatava sõna arrayks
            boolean wrongAnswer = true;

            if (model.getMissedCharacter().contains(guessedStringChar)) { //Märguanne korduvalt sisestatud tähele
                JOptionPane.showMessageDialog(null, "Seda tähte on juba pakutud - " + guessedStringChar, "Viga!", JOptionPane.ERROR_MESSAGE);
                wrongAnswer = false; //Et ei arvestaks veana
            } else {
                for (int i = 0; i < wordCharArray.length; i++) { // Kui Modeli all on 1. ja viimane täht nähtav: (int i = 1; i < wordCharArray.length - 1; i++)
                    if (wordCharArray[i].equalsIgnoreCase(guessedStringChar)) {
                        //System.out.println("Tuvastati sõnas pakutud täht");
                        model.getHiddenWord().setCharAt(i, guessedChar); //Avaldab ära arvatud tähe
                        view.getLblGuessWord().setText(model.spaceBetweenChars(String.valueOf(model.getHiddenWord()))); //Uuendab lblGuessWord
                        wrongAnswer = false;
                    }
                }
            }
            if (wrongAnswer) {
                model.getMissedCharacter().add(guessedStringChar);
                view.getLblWrongInfo().setForeground(Color.RED);
            }
        } catch (Exception ignored) {
        }
        view.getTxtChar().setText(""); //Teeb TxtChari pärast sendi uuesti tühjaks
        String missedCharListFormat = model.getMissedCharacter().toString().replace("[", "").replace("]", "");
        model.setMissedCharCount(model.getMissedCharacter().size());
        view.getLblWrongInfo().setText("Valesti " + model.getMissedCharCount() + " täht(e). " + missedCharListFormat);

        if (!model.getHiddenWord().toString().contains("_")) { //Kontroll, kas kõik tähed on arvatud
            String playerName = JOptionPane.showInputDialog(view, "Sisesta oma nimi (Vähemalt 2 tähemärki): ", "Võit!", JOptionPane.INFORMATION_MESSAGE);
            try { // Lisatud try, sest kui lõpus jätta nimi sisestamata, viskab errori
                if (playerName.length() < 2 || playerName.isBlank()) {
                    JOptionPane.showMessageDialog(view, "Nimi ei olnud sisestatud korrektselt. Edetabelisse ei lisatud");

                } else {
                    DataScores winnerScore = new DataScores(LocalDateTime.now(), playerName, model.getRandomWord(), missedCharListFormat);
                    model.scoreInsert(winnerScore);
                    model.getDataScores().add(winnerScore);
                    JOptionPane.showMessageDialog(view, "Nimi lisatud edetabelisse!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, "Nime ei sisestatud või ei olnud korrektne. Edetabelisse ei lisatud");
            }
            view.setEndGame();
        }
    }
}