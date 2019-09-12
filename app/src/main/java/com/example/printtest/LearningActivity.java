package com.example.printtest;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class LearningActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    public  int positionOfLanguage;
    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private TextView chosenLanguage;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    Spinner spinner2;
    int position2;
    int positionOfPhrase;
    String newTranslation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        positionOfLanguage = getIntent().getIntExtra("number", 0);

        Log.d("learn", "we made it to the next activity");

        String[] languages = getResources().getStringArray(R.array.languages);

        chosenLanguage = (TextView) findViewById(R.id.speakLanguage);
        chosenLanguage.setText(languages[positionOfLanguage]);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        spinner2 = (Spinner) findViewById(R.id.spinner2);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.practicePhrases, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner2.setAdapter(adapter);

        spinner2.setOnItemSelectedListener(this);

        // hide the action bar
        //getActionBar().hide();



        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {

        String[] speechToText = getResources().getStringArray(R.array.speechToText);
        String extraLanguage = speechToText[positionOfLanguage];

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, extraLanguage);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));

                    TextView view = findViewById(R.id.view);

                    // Calculate the "distance" between the text we've prompted the user to say
                    // and the result from the translation.
                    LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
                    double distance = levenshteinDistance.apply(
                            view.getText().toString(),
                            result.get(0)
                    );
                    double total = view.getText().length();
                    // Calculate the "percentage of correct-ness".
                    // Determined by (total - distance) / total.
                    double score = ((total - distance) * 100) / total;


                    TextView response = findViewById(R.id.response);
                    response.setText("Your score was " + new DecimalFormat("##").format(score) + "%");
                }
                break;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
        positionOfPhrase = position;
        position2 = positionOfLanguage;
        Log.d("translatecheck", "position set" + position2);

        new Thread(new Runnable() {
            public void run() {
                Log.d("translatecheck", "I clicked a button");
                String[] isoArr = getResources().getStringArray(R.array.iso);
                Log.d("translatecheck", "isoarr created");
                String[] practicePhrase = getResources().getStringArray(R.array.practicePhrases);

                //Translate translate = TranslateOptions.getDefaultInstance().getService();

                TranslateOptions options = TranslateOptions.newBuilder()
                        .setApiKey("AIzaSyBPcNTQy6AqjwpXKQuDMl6Qz5epr1j35H4").build();
                Translate translate = options.getService();
                //Translate translate = createTranslateService();

                Log.d("translatecheck", "translate service created");
                String sourceLang = "en";
                String targetLang = isoArr[positionOfLanguage];
                Translate.TranslateOption srcLang = Translate.TranslateOption.sourceLanguage(sourceLang);
                Translate.TranslateOption tgtLang = Translate.TranslateOption.targetLanguage(targetLang);
                Log.d("translatecheck", "translate service initialized");
                // The text to translate

                Log.d("translatecheck", "converted edit text to string");
                //Translates some text into spinner language
                Translation translation = translate.translate(practicePhrase[positionOfPhrase], srcLang, tgtLang);

                Log.d("translatecheck", "we have translated");
                newTranslation = translation.getTranslatedText();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textElement = (TextView) findViewById(R.id.view);
                        Log.d("translatecheck", "textElement initialized");
                        textElement.setText(newTranslation);
                    }
                });

            }

        }).start();
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


}
