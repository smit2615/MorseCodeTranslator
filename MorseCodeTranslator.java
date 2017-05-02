import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.application.Application;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
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
 */
public class MorseCodeTranslator extends Application {

    private HashMap<String, Character> code = new HashMap<>(26);
    private int location = 0; //index where the output is typing
    private int startOfKey = 0; //start index of current Morse expression
    private Stack<Integer> previousStartOfKeys = new Stack<>();

    public void start(Stage primaryStage) {

        File dictionary = new File("code.txt"); //file containing the Morse code for a-z

        try {

            Scanner in = new Scanner(dictionary);
            while(in.hasNext()) {

                code.put(in.next(), in.next().charAt(0)); //Morse code is the key, character a-z is the value

            }
            in.close();

        } catch(Exception e) {

            e.printStackTrace();

        }

        HBox hbox = new HBox();
        hbox.setStyle("-fx-border-color: black");
        TextArea input = new TextArea();
        input.setPromptText("Type Morse Code here");
        input.setFocusTraversable(false);
        input.setWrapText(true);
        input.setPrefWidth(250);
        Text output = new Text("");
        hbox.getChildren().addAll(input, output);

        input.setOnKeyPressed(e -> {
            switch(e.getCode()) {

                //Dots and dashes - update the current letter as the user types
                case PERIOD: output.setText(output.getText().substring(0, location) + code.get(input.getText().substring(startOfKey, input.getText().length()) + ".")); break;
                case MINUS: output.setText(output.getText().substring(0, location) + code.get(input.getText().substring(startOfKey, input.getText().length()) + "-")); break;
                case SPACE: location++; //User is now typing on the next letter, so index is incremented
                            previousStartOfKeys.push(startOfKey); 
                            startOfKey = (input.getText() + " ").indexOf(" ", startOfKey) + 1; break; //User is starting a new Morse expression
                case SLASH: location += 2; //Increment twice to skip over the space we're adding
                            output.setText(output.getText() + " ");
                            previousStartOfKeys.push(startOfKey);
                            startOfKey = (input.getText() + "/").indexOf("/", startOfKey) + 1; break;
                case BACK_SPACE: char deleted = input.getText().charAt(input.getText().length() - 1);
                                 if(deleted == ' ') { //A space was deleted
                                    location--;
                                    startOfKey = previousStartOfKeys.pop();
                                 } else if(deleted == '/') { //A forward slash was deleted
                                    location -= 2;
                                    output.setText(output.getText().substring(0, output.getText().length() - 1));
                                    startOfKey = previousStartOfKeys.pop();         
                                 } else { //A piece of a Morse expression was deleted
                                        if(input.getText().substring(startOfKey, input.getText().length() - 1).equals("")) {

                                            output.setText(output.getText().substring(0, output.getText().length() - 1));

                                        } else {
                                            output.setText(output.getText().substring(0, location) + code.get(input.getText().substring(startOfKey, input.getText().length() - 1)));
                                        }
                                 } break;

           }
        });

        Scene scene = new Scene(hbox, 500, 300);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Morse Code Translator");
        primaryStage.show();

    }

}