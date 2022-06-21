package models;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Klass mudel (Üldine)
 */
public class Model {
    /**
     * Siin on kõik unikaalsed kategooriad mis andmebaasi failist leiti
     */
    private String[] categories;
    /**
     * Andmeaasi nimi kettal
     */
    private String dbName = "words.db";
    /**
     *  Andmebaasi ühenduse jaoks
     */
    private String dbUrl = "jdbc:sqlite:" + dbName;
    /**
     * Andmebaasi tabeli scores sisu (Edetabel)
     */
    private List<DataScores> dataScores;
    /**
     * Andmebaasi tabeli words sisu Sõnad
     */
    private List<DataWords> dataWords;
    /**
     * Andmebaasi ühendust algselt pole
     */
    Connection connection = null;
    /**
     * Juhuslik sõna, mis genereeritakse andmebaasist
     */
    private String randomWord;
    /**
     * Andmebaasist võetud sõna, mille kõik tähed pole nähtavad
     */
    private StringBuilder hiddenWord;
    /**
     * Valesti arvatud tähtede list
     */
    private List<String> missedCharacter = new ArrayList<>();
    /**
     * Valesti arvatud tähtede arv
     */
    private int missedCharCount;
    /**
     * Konstruktor
     */
    public Model() {
        dataScores = new ArrayList<>(); // Teeme tühja edetabeli listi
        dataWords = new ArrayList<>(); // Teeme tühja sõnade listi
        //categories = new String[]{"Kõik kategooriad", "Kategooria 1", "Kategooria 2"}; // TESTIKS!
        scoreSelect(); // Loeme edetabeli dataScores listi, kui on!
        wordsSelect(); // Loeme sõnade tabeli dataWords listi.
    }
    // ANDMEBAASI ASJAD
    /**
     * Andmebaaasi ühenduseks
     * @return tagastab ühenduse või rakendus lõpetab töö
     */
    private Connection dbConnection() throws SQLException {
        if (connection != null) { // Kui ühendus on püsti
            connection.close(); // Sulge ühendus
        }
        connection = DriverManager.getConnection(dbUrl); // Tee ühendus
        return connection; // Tagasta ühendus
    }
    /**
     * SELECT lause edetabeli sisu lugemiseks ja info dataScores listi lisamiseks
     */
    public void scoreSelect() {
        String sql = "SELECT * FROM scores ORDER BY playertime DESC";
        try {
            Connection conn = this.dbConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            dataScores.clear(); // Tühjenda dataScores list vanadest andmetest
            while (rs.next()) {
                //int id = rs.getInt("id");
                String datetime = rs.getString("playertime");
                LocalDateTime playerTime = LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String playerName = rs.getString("playername");
                String guessWord = rs.getString("guessword");
                String wrongCharacters = rs.getString("wrongcharacters");
                // Lisame tabeli kirje dataScores listi
                dataScores.add(new DataScores(playerTime, playerName, guessWord, wrongCharacters));

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Andmete lisamine andmebaasi, kasutades objekti DataScore
     */
    public void scoreInsert(DataScores winnerScore){
        String sql = "INSERT INTO scores (playertime, playername, guessword, wrongcharacters) VALUES (?, ?, ?, ?)";
        try{
            Connection conn = this.dbConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String playerTime = winnerScore.getGameTime().format(formatDate);
            stmt.setString(1, playerTime);
            stmt.setString(2, winnerScore.getPlayerName());
            stmt.setString(3, winnerScore.getGuessWord());
            stmt.setString(4, winnerScore.getMissingLetters());
            stmt.executeUpdate();
            scoreSelect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * SELECT lause tabeli words sisu lugemiseks ja info dataWords listi lisamiseks
     */
    public void wordsSelect() {
        String sql = "SELECT * FROM words ORDER BY category, word";
        List<String> categories = new ArrayList<>(); // NB! See on meetodi sisene muutuja categories!
        try {
            Connection conn = this.dbConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            dataWords.clear(); // Tühjenda dataScores list vanadest andmetest
            while (rs.next()) {
                //int id = rs.getInt("id");
                int id = rs.getInt("id");
                String word = rs.getString("word");
                String category = rs.getString("category");
                dataWords.add(new DataWords(id, word, category)); // Lisame tabeli kirje dataWords listi
                categories.add(category);
            }
            // https://howtodoinjava.com/java8/stream-find-remove-duplicates/
            List<String> unique = categories.stream().distinct().collect(Collectors.toList());
            setCorrectCategoryNames(unique); // Unikaalsed nimed Listist String[] listi categories
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Meetod lisamaks arvatava sõna vahele tühikuid
     */
    public String spaceBetweenChars(String word) {
        String[] wordCharArray = word.split(""); //Tükeldab sõna tähtedeks
        StringJoiner join = new StringJoiner(" ");
        for (String w : wordCharArray){
            join.add(w);
            //System.out.println(w);
        }
        return join.toString();
    }

    // SETTERS
    /**
     * Paneb unikaalsed kategooriad ComboBox-i jaoks muutujasse
     * @param unique unikaalsed kategooriad
     */
    private void setCorrectCategoryNames(List<String> unique) {
        categories = new String[unique.size()+1]; // Vali kategooria. See on klassi sisene muutuja!
        categories[0] = "Kõik kategooriad";
        for(int x = 0; x < unique.size(); x++) {
            categories[x+1] = unique.get(x);
        }
    }

    /**
     * Setter valesti arvatud tähtede arvule
     * @param missedCharCount
     */
    public void setMissedCharCount(int missedCharCount) {
        this.missedCharCount = missedCharCount;
    }
    /**
     * Kategooria järgi juhusliku sõna valimine
     */
    public void setRandomWordByCategory(String category){
        List<String> wordsList = new ArrayList<>(); //Tühi list sõnade jaoks
        Random random = new Random(); //muutuja random defineerimine, et teha juhuslik sõna valik
        String randomWord; //Juhuslik valitud sõna
        if (category.equalsIgnoreCase("Kõik kategooriad")){ //Võrdleb, kas valik tehakse kõikide kategooriate seast
            randomWord = dataWords.get(random.nextInt(dataWords.size())).getWord();
        } else {
            for (DataWords word : dataWords){
                if (category.equalsIgnoreCase(word.getCategory())) { //Lisab valitud kategooriast sõnad wordsListi
                    //System.out.println(word.getWord());
                    wordsList.add(word.getWord());
                }
            }
            randomWord = wordsList.get(random.nextInt(wordsList.size())); //Valib juhusliku sõna wordsListist
        }
        this.randomWord = randomWord.toUpperCase(); //Kuvab sõna ja teeb kõik tähed suureks
        hideWord(); //Tekitab peidetud sõna
    }
    public void hideWord(){
        StringBuilder newWord = new StringBuilder(this.randomWord);
        for (int i = 1; i < this.randomWord.length() - 1; i++){ //kõik stringi tähted alakriipsudeks v.a esimene ja viimane
            char toCheck = newWord.charAt(i);
            char firstChar = newWord.charAt(0);
            char lastChar = newWord.charAt(newWord.length()-1);
            if (toCheck != firstChar && toCheck != lastChar){ //Esimese ja viimase tähega samad tähed mitte peidetuks
                newWord.setCharAt(i, '_');
            }
        }
        this.hiddenWord = newWord;
    }

    // GETTERS
    /**
     * Tagasta kategooriad
     * @return tagastab String[] listi kategooria nimedega
     */
    public String[] getCategories() {
        return categories;
    }
    /**
     * Tagastab edetabeli listi
     * @return tagastab List&lt;DataScores&gt; listi edetabeli tabelis sisuga
     */
    public List<DataScores> getDataScores() {
        return dataScores;
    }
    /**
     * Tagastab sõnade listi
     * @return List
     */
    public List<DataWords> getDataWords() {
        return dataWords;
    }
    public String getRandomWord() {
        return randomWord;
    }
    public StringBuilder getHiddenWord() {
        return hiddenWord;
    }
    public List<String> getMissedCharacter() {
        return missedCharacter;
    }
    public int getMissedCharCount() {
        return missedCharCount;
    }
}
