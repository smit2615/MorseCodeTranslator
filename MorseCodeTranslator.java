import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.application.Application;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.input.KeyEvent;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import java.io.File;

/**
 * MorseCodeTranslator.java
 * @author Nathan Smith
 * @version 1.0
 * 
 * Purpose - Allows the user to type Morse Code into 
 * a TextArea while displaying the current translation in 
 * a Text Node on the right. Spacebar denotes a change to
 * to a new letter, forwad slash denotes a new word. Currently working
 * on handling unexpected characters and an Enlgish to Morse translation.
 * Only characters a-z are currently supported.
 * TODO: find better way to check if the character deleted is the first character of a Morse expression
 */
public class MorseCodeTranslator extends Application {

    private HashMap<String, Character> code = new HashMap<>(26);
    private HashMap<Character, String> en = new HashMap<>(26);
    private int location = 0; //index where the output is typing
    private int startOfKey = 0; //start index of current Morse expression
    private Stack<Integer> previousStartOfKeys = new Stack<>();
    private HBox hbox = new HBox();
    private TextArea input = new TextArea();
    private TextArea output = new TextArea();

    @Override
    /**
     * Overrides start method in Application class
     * 
     * Reads a file to fill our HashMaps. Sets on actions
     * for key presses. Provides layout information for GUI.
     */
    public void start(Stage primaryStage) {

        //read from the file to fill our hashmaps
        File dictionary = new File("code.txt"); //file containing the Morse code for a-z

        try {

            Scanner in = new Scanner(dictionary);
            while(in.hasNext()) {

                String morse = in.next();
                char ch = in.next().charAt(0);
                code.put(morse, ch); //Morse code is the key, character a-z is the value
                en.put(ch, morse); //a-z are keys, values are Morse code

            }
            in.close();

        } catch(Exception e) {

            e.printStackTrace();

        }
        
        //set the layout information
        hbox.setStyle("-fx-border-color: black");
        input.setPromptText("Type Morse Code here");
        input.setFocusTraversable(false);
        input.setWrapText(true);
        input.setPrefWidth(250);
        output.setPromptText("Or type English here");
        output.setFocusTraversable(false);
        output.setWrapText(true);
        output.setPrefWidth(250);
        hbox.getChildren().addAll(input, output);

        //set how to handle KeyEvents
        input.setOnKeyPressed(e -> {
            switch(e.getCode()) {

                //Dots and dashes - update the current letter as the user types
                case PERIOD: updateEnglish(e); break;
                case MINUS: updateEnglish(e); break;
                case SPACE: advanceEnglish(1, e); break;
                case SLASH: advanceEnglish(2, e); break;
                case BACK_SPACE: deleteEnglish(input.getText().charAt(input.getText().length() - 1)); break;

           }
        });

        output.setOnKeyPressed(e -> {
            switch(e.getCode()) {

                case SPACE: advanceMorse(); break;
                case BACK_SPACE: deleteMorse(output.getText().charAt(output.getText().length() - 1)); break;
                default: updateMorse(e); break;

            }
        });

        Scene scene = new Scene(hbox, 500, 300);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Morse Code Translator");
        primaryStage.show();

    }

    /**
     * Updates the English translation of Morse code as it is typed
     */
    public void updateEnglish(KeyEvent k) {

        output.setText(output.getText().substring(0, location) + code.get(input.getText().substring(startOfKey, input.getText().length()) + k.getText()));

    }

    /**
     * Advances the current index that we are translating at as well
     * as updating the index of the start of our current Morse expression
     */
    public void advanceEnglish(int spaces, KeyEvent k) {

        location += spaces; //User is now typing on the next letter, so index is incremented
        String eAdvancement = location == 1 ? "" : " ";
        output.setText(output.getText() + eAdvancement);
        previousStartOfKeys.push(startOfKey); 
        startOfKey = (input.getText() + k.getText()).indexOf(k.getText(), startOfKey) + 1; //User is starting a new Morse expression

    }

    /**
     * Specifies the action to take when when certain
     * Morse characters are deleted by the user.
     */
    public void deleteEnglish(char deleted) {

        switch(deleted) {

            case ' ' : location--; break;
            case '/': location -= 2; break; //one extra to account for the space we must delete

        }
        
        if(deleted == ' ') 
            startOfKey = previousStartOfKeys.pop();
        else if(deleted == '/') {  
            output.setText(output.getText().substring(0, output.getText().length() - 1)); //cut off the space that the / put there
            startOfKey = previousStartOfKeys.pop();
        }
        else {
            if(input.getText().substring(startOfKey, input.getText().length() - 1).equals("")) //first character of a Morse Expression was deleted
                output.setText(output.getText().substring(0, output.getText().length() - 1)); //delete the English character that was being displayed
            else
                output.setText(output.getText().substring(0, location) + code.get(input.getText().substring(startOfKey, input.getText().length() - 1))); 
        }

    }

    /**
     * Translates the English typed by the user and displays it
     */
    public void updateMorse(KeyEvent k) {

        char keyTyped = k.getText().toLowerCase().charAt(0);
        if(keyTyped != 32 && !(keyTyped <= 122 && keyTyped >= 97)) //neither a-z nor a space was typed
            return;
        location++;
        input.setText(input.getText() + en.get(keyTyped) + " ");
        previousStartOfKeys.push(startOfKey);
        startOfKey = (input.getText()).indexOf(" ", startOfKey) + 1;

    }

    /**
     * Advances the index of the current Morse expression to be displayed
     */
    public void advanceMorse() {

        location += 2;
        input.setText(input.getText() + "/");
        previousStartOfKeys.push(startOfKey);
        startOfKey = (input.getText()).indexOf("/", startOfKey) + 1;

    }

    /**
     * Deletes Morse as the user backspaces over English characters
     */
    public void deleteMorse(char deleted) {

        location -= deleted == ' ' ? 2 : 1;
        startOfKey = previousStartOfKeys.pop();
        if(deleted == ' ')
            input.setText(input.getText().substring(0, input.getText().length() - 2));
        else
            input.setText(input.getText().substring(0, startOfKey));

    }

}

