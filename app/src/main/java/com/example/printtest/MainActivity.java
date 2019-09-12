package com.example.printtest;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.printtest.common.Common;
import com.example.printtest.common.MsgDialog;
import com.example.printtest.common.MsgHandle;
import com.example.printtest.printprocess.TemplatePrint;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.util.ArrayList;
import java.util.HashMap;

public class
MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private MsgDialog mDialog;
    private MsgHandle mHandle;
    private TemplatePrint myPrint;
    Spinner spinner1;
    EditText editText;
    int pos;
    String newTranslation;
    boolean test;

    private ArrayList<HashMap<String, Object>> listItems = new ArrayList<HashMap<String, Object>>();
    private String printTranslation;
    //private String[] names = {"Wallet", "Phone stand", "Phone speaker", "CD case", "Envelope", "Dice"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        test = false;
        mDialog = new MsgDialog(this);
        mHandle = new MsgHandle(this, mDialog);
        myPrint = new TemplatePrint(this, mHandle, mDialog);

        // when use bluetooth print set the adapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        myPrint.setBluetoothAdapter(bluetoothAdapter);

        spinner1 = (Spinner) findViewById(R.id.spinner1);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner1.setAdapter(adapter);

        spinner1.setOnItemSelectedListener(this);
        Log.d("translatecheck", "test");


        Button button = findViewById(R.id.button);
        Button learn = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        if(test == true) {

                            listItems.clear();
                        }

                        Log.d("translatecheck", "I clicked a button");
                        String[] isoArr = getResources().getStringArray(R.array.iso);
                        Log.d("translatecheck", "isoarr created");

                        //Translate translate = TranslateOptions.getDefaultInstance().getService();

                        TranslateOptions options = TranslateOptions.newBuilder()
                                .setApiKey("AIzaSyBPcNTQy6AqjwpXKQuDMl6Qz5epr1j35H4").build();
                        Translate translate = options.getService();
                        //Translate translate = createTranslateService();

                        Log.d("translatecheck", "translate service created");
                        String sourceLang = "en";
                        String targetLang = isoArr[pos];
                        Translate.TranslateOption srcLang = Translate.TranslateOption.sourceLanguage(sourceLang);
                        Translate.TranslateOption tgtLang = Translate.TranslateOption.targetLanguage(targetLang);
                        Log.d("translatecheck", "translate service initialized");
                        // The text to translate
                        editText = findViewById(R.id.edit);
                        String text = editText.getText().toString();
                        Log.d("translatecheck", "converted edit text to string");
                        //Translates some text into spinner language
                        Translation translation = translate.translate(text, srcLang, tgtLang);

                        Log.d("translatecheck", "we have translated");
                        newTranslation = translation.getTranslatedText();

                        runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                TextView textElement = (TextView) findViewById(R.id.view);
                                Log.d("translatecheck", "textElement initialized");
                                textElement.setText(newTranslation);
                            }
                        });
                        Log.d("translatecheck", translation.getTranslatedText());

                        printTranslation = newTranslation;


                        myPrint.setEncoding(Common.ENCODING_ENG);


                        addStartFlg();
                        // Set up the thing to be printed.
                        HashMap<String, Object> mapData = new HashMap<String, Object>();

                        // the actual good stuff
                        mapData.put(Common.TEMPLATE_REPLACE_TYPE,
                            Common.TEMPLATE_REPLACE_TYPE_TEXT);
                        mapData.put(Common.TEMPLATE_REPLACE_TEXT, printTranslation);
                        listItems.add(mapData);

                        addEndFlg();

                        Log.d("Corey", "items: " + listItems.toString());

                        myPrint.setPrintData(listItems);

                        myPrint.print();
                        //listItems.remove();
                        //mapData.remove(Common.TEMPLATE_REPLACE_TEXT);
                        Log.d("Corey", "Successful Print" + listItems.toString());
                        test = true;








                    }
                }).start();

            }
        });


        learn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View l) {
                Log.d("learn", "we made it to the on click listener");
                Intent learningIntent = new Intent(getApplicationContext(), LearningActivity.class);
                learningIntent.putExtra("number", pos);

                startActivity(learningIntent);
            }
        });




    }


    private void addStartFlg() {

        HashMap<String, Object> mapData = new HashMap<String, Object>();
        mapData.put(Common.TEMPLATE_REPLACE_TYPE,
                Common.TEMPLATE_REPLACE_TYPE_START);

        mapData.put(Common.TEMPLATE_KEY, "1");
        listItems.add(mapData);
        //currentInput = true;
    }

    /**
     * Add end flag for multiple pdz's print
     */
    private void addEndFlg() {

        HashMap<String, Object> mapData = new HashMap<String, Object>();
        mapData.put(Common.TEMPLATE_REPLACE_TYPE,
                Common.TEMPLATE_REPLACE_TYPE_END);
        listItems.add(mapData);
        //currentInput = false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
        pos = position;
        Log.d("translatecheck", "position set" + pos);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}