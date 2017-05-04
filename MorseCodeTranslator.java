import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.application.Application;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
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
 *       figure out why hbox.geChildren().addAll() doesn't add to the pane in the correct order
 */
public class MorseCodeTranslator extends Application {

    private HashMap<String, Character> code = new HashMap<>(26);
    private HashMap<Character, String> en = new HashMap<>(26);
    private int location = 0; //index where the output is typing
    private int mStartOfKey = 0; //start index of current Morse expression
    private int eStartOfKey = 0;
    private Stack<Integer> mPreviousStartOfKeys = new Stack<>();
    private Stack<Integer> ePreviousStartOfKeys = new Stack<>();
    private StackPane sp = new StackPane();
    private HBox hbox = new HBox();
    private TextArea mInput = new TextArea();
    private Text mOutput = new Text();
    private TextArea eInput = new TextArea();
    private Text eOutput = new Text();
    private Button swap = new Button("<->");
    private char mode = 'M';


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
        mInput.setPromptText("Type Morse Code here");
        mInput.setFocusTraversable(false);
        mInput.setWrapText(true);
        mInput.setPrefWidth(250);
        eInput.setPromptText("Type English here");
        eInput.setFocusTraversable(false);
        eInput.setWrapText(true);
        eInput.setPrefWidth(250);
        mOutput.setWrappingWidth(250);
        eOutput.setWrappingWidth(250);
        swap.setOpacity(0.6);

        hbox.getChildren().addAll(mInput, mOutput);
        sp.getChildren().addAll(hbox, swap);

        //set how to handle KeyEvents
        mInput.setOnKeyPressed(e -> {
            mInput.setEditable(true);
            switch(e.getCode()) {

                //Dots and dashes - update the current letter as the user types
                case PERIOD: updateEnglish(e); break;
                case MINUS: updateEnglish(e); break;
                case SPACE: advanceEnglish(1, e); break;
                case SLASH: advanceEnglish(2, e); break;
                case BACK_SPACE: 
                    if(!mInput.getText().isEmpty())
                        deleteEnglish(mInput.getText().charAt(mInput.getText().length() - 1)); break;
                default: mInput.setEditable(false);

           }
        });

        eInput.setOnKeyPressed(e -> {
            eInput.setEditable(true);
            switch(e.getCode()) {

                case SPACE: advanceMorse(); break;
                case BACK_SPACE: 
                    if(!eInput.getText().isEmpty())
                        deleteMorse(eInput.getText().charAt(eInput.getText().length() - 1)); break;
                default: 
                    if(!e.getCode().isLetterKey() && !(e.getCode() == KeyCode.SPACE))
                        eInput.setEditable(false);
                    else   
                        updateMorse(e); break;

            }
        });

        eInput.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            String keyTyped = e.getText();
            if(keyTyped.length() != 1) 
                eInput.setEditable(false);
        });

        swap.setOnAction(e -> {
            hbox.getChildren().clear();
            switch(mode) {
                case 'M': hbox.getChildren().addAll(eInput, eOutput); 
                          mode = 'E'; break;
                case 'E': hbox.getChildren().addAll(mInput, mOutput); 
                          mode = 'M'; break;
            }
        });

        Scene scene = new Scene(sp, 500, 300);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Morse Code Translator");
        primaryStage.show();

    }

    /**
     * Updates the English translation of Morse code as it is typed
     */
    public void updateEnglish(KeyEvent k) {

        mOutput.setText(mOutput.getText().substring(0, location) + code.get(mInput.getText().substring(mStartOfKey, mInput.getText().length()) + k.getText()));

    }

    /**
     * Advances the current index that we are translating at as well
     * as updating the index of the start of our current Morse expression
     */
    public void advanceEnglish(int spaces, KeyEvent k) {

        location += spaces; //User is now typing on the next letter, so index is incremented
        String eAdvancement = location == 1 ? "" : " ";
        mOutput.setText(mOutput.getText() + eAdvancement);
        mPreviousStartOfKeys.push(mStartOfKey); 
        mStartOfKey = (mInput.getText() + k.getText()).indexOf(k.getText(), mStartOfKey) + 1; //User is starting a new Morse expression

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
            mStartOfKey = mPreviousStartOfKeys.pop();
        else if(deleted == '/') {  
            mOutput.setText(mOutput.getText().substring(0, mOutput.getText().length() - 1)); //cut off the space that the / put there
            mStartOfKey = mPreviousStartOfKeys.pop();
        }
        else {
            if(mInput.getText().substring(mStartOfKey, mInput.getText().length() - 1).equals("")) //first character of a Morse Expression was deleted
                mOutput.setText(mOutput.getText().substring(0, mOutput.getText().length() - 1)); //delete the English character that was being displayed
            else
                mOutput.setText(mOutput.getText().substring(0, location) + code.get(mInput.getText().substring(mStartOfKey, mInput.getText().length() - 1))); 
        }

    }

    /**
     * Translates the English typed by the user and displays it
     */
    public void updateMorse(KeyEvent k) {

        char keyTyped = k.getText().toLowerCase().charAt(0);
        if(keyTyped != 32 && !(keyTyped <= 122 && keyTyped >= 97)) //neither a-z nor a space was typed
            return;
        eOutput.setText(eOutput.getText() + en.get(keyTyped) + " ");
    ePreviousStartOfKeys.push(eStartOfKey);
        eStartOfKey = (eOutput.getText()).indexOf(" ", eStartOfKey) + 1;

    }

    /**
     * Advances the index of the current Morse expression to be displayed
     */
    public void advanceMorse() {

        eOutput.setText(eOutput.getText() + "/");
        ePreviousStartOfKeys.push(eStartOfKey);
        eStartOfKey = (eOutput.getText()).indexOf("/", eStartOfKey) + 1;

    }

    /**
     * Deletes Morse as the user backspaces over English characters
     */
    public void deleteMorse(char deleted) {

        eStartOfKey = ePreviousStartOfKeys.pop();
        if(deleted == ' ')
            eOutput.setText(eOutput.getText().substring(0, eOutput.getText().length() - 2));
        else
            eOutput.setText(eOutput.getText().substring(0, eStartOfKey));

    }

}

