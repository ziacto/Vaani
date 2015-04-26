/*

Copyright 2015, vyastech

This file is part of Vaani.

    Vaani is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Vaani is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Vaani.  If not, see <http://www.gnu.org/licenses/>.

*/
package com.vyasware.vaani;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
//import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.vyasware.vaani.model.Weather;

import org.json.JSONException;

public class MainActivity extends ActionBarActivity implements
        RecognitionListener {

    private TextView returnedText;
    private TextView outputText;
    private ToggleButton toggleButton;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private final String LOG_TAG = "VoiceRecognition";
    private String input;
    private TextToSpeech tts;
    private String speak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        returnedText = (TextView) findViewById(R.id.textView1);
        outputText = (TextView) findViewById(R.id.textView2);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);

        progressBar.setVisibility(View.INVISIBLE);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
//                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"hi-IN");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "hi-IN");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        tts = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            tts.setLanguage(new Locale("hi_IN"));
                            tts.setSpeechRate(0.9f);
                        }
                    }
                });
        returnedText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, returnedText.getText());
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivity(intent);

            }
        });

        toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    speech.startListening(recognizerIntent);
                    outputText.setText("");
                } else {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    speech.stopListening();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        toggleButton.setChecked(false);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        returnedText.setText(errorMessage);
        toggleButton.setChecked(false);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        input = matches.get(0);
     /*   for (String result : matches)
            text += result + "\n";*/

        returnedText.setText(input);
        new Thread(new Runnable() {
            @Override
            public void run() {
                performAction(input);
            }
        }).start();

    }

    private void performAction(String input) {

        String[] verbs = {"कॉल","फोन","डायल","संपर्क","मेसेज","सन्देश","संदेश","संदेसा","ओपन","खोलो","खोल", "मौसम", "मोसम","वेदर","तापमान", "weather", "नमस्ते", "नमस्कार", "कौन" , "हेलो", "हेल्लो"};
        String[] actions = {"call","call","call","call","msg","msg","msg","msg","open","open","open", "weather", "weather", "weather", "weather", "weather", "greet", "greet", "greet", "greet", "greet"};
        String[] sentence = input.split(" ");
        String action="",noun="",result="";
        int verbindex = -1, nounindex = -1;
        for (String word : sentence)
        {

            //out.println(word);

            for(int i=0; i<verbs.length; i++){
                //System.out.println(verbs[i]);
                if(word.equalsIgnoreCase(verbs[i])) verbindex = i;
            }

            if (verbindex >=0) {
                //	out.println("true");
                action = actions[verbindex];
                System.out.println(action);
                break;
            }

        }
        for (int j = 0; j<sentence.length; j++)
        {

            if (sentence[j].equalsIgnoreCase("को")){
                nounindex = j-1;
            }
        }
        if(nounindex>=0) noun = sentence[nounindex];
        String search = "google!";
        if(action.equals("")/*||noun.equals("")*/) {tts.speak(search,TextToSpeech.QUEUE_FLUSH, null);
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, returnedText.getText());
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivity(intent);
        }
        else if(action.equals("call")) doCall(noun);
        else if(action.equals("msg")) doMsg(noun);
        else if(action.equals("open")) doOpen(sentence);
        else if(action.equals("weather")) doWeather();
        else if(action.equals("greet")) doGreet();
        // tts.speak(result,TextToSpeech.QUEUE_FLUSH, null);


    }

    private void doGreet() {
        Random random = new Random();
        int r = random.nextInt(3);
        String[] greetings = {"नमस्ते, मेरा नाम वाणी है!", "नमस्कार, वाणी आपकी सेवा में तत्पर है", "नमस्ते. मैं वाणी हूँ! मैं एक हिंदी डिजिटल असिस्टेंट हूँ!"};
        tts.speak(greetings[r],TextToSpeech.QUEUE_FLUSH, null);

    }

    private void doWeather() {
        System.out.println("weather");
        new JSONWeatherTask().execute("mumbai");
       /* speak = "मुंबई का मौसम, \nतापमान 31.23 अंक सेल्सियस\nदबाव 77 मिलीमीटर \nआर्द्रता 80  प्रति शत !";
        tts.speak(speak ,TextToSpeech.QUEUE_FLUSH, null);
        outputText.setText(speak);*/
    }

    private void doOpen(String[] sentence) {
        System.out.println("open");
        for (String word : sentence){
            switch (word)
            {
                //फेसबुक व्हाट्सप्प कैमरा कमरा
                case "फेसबुक":
                case "facebook":
                    //open facebook
                    Intent fbintent = new Intent();
                    fbintent.setAction(Intent.ACTION_VIEW);
                    fbintent.setData(android.net.Uri.parse("http://www.facebook.com"));
                    startActivity(fbintent);
                    break;
                case "whatsapp":
                case "व्हाट्सप्प":
                    //todo run whatsapp
                    break;
                case "camera":
                case "कैमरा":
                case "कमरा":
                    //open camera
                    Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    try {
                        PackageManager pm = getPackageManager();

                        final ResolveInfo mInfo = pm.resolveActivity(i, 0);

                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(mInfo.activityInfo.packageName, mInfo.activityInfo.name));
                        intent.setAction(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);

                        startActivity(intent);
                    } catch (Exception e){ Log.i("open", "Unable to launch camera: " + e); }
                    break;
                case "browser":
                case "ब्राउज़र":
                    // open browser
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(android.net.Uri.parse("http://www.google.com"));
                    startActivity(intent);
                    break;
                case "youtube":
                case "युट्युब":
                    //run youtube
                    Intent ytintent = new Intent();
                    ytintent.setAction(Intent.ACTION_VIEW);
                    ytintent.setData(android.net.Uri.parse("http://www.youtube.com"));
                    startActivity(ytintent);
                    break;


            }
        }

    }

    private void doMsg(String noun) {
        System.out.println("msg");


        Cursor cur = null;
        ContentResolver cr = null;

        try {
            cr = getContentResolver();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null,
                    null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            boolean called = false;
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {

                    String id = cur.getString(cur
                            .getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur
                            .getString(cur
                                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    // Log.i("Names", name);
                    if (Integer
                            .parseInt(cur.getString(cur
                                    .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        // Query phone here. Covered next
                        Cursor phones = getContentResolver()
                                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                                + " = " + id, null, null);
                        while (phones.moveToNext()) {
                            String phoneNumberX = phones
                                    .getString(phones
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            // Log.i("Number", phoneNumber);
                            boolean a = (name.equalsIgnoreCase(noun) );
                            System.out.println(name + "  " + a + "  "
                                    + phoneNumberX);

                            if (a)
                                System.out.println(phoneNumberX);


                            if (a) {
                                String b = "sms:";
                                String smsUri = b + phoneNumberX;
                                Intent smsIntent = new Intent(
                                        Intent.ACTION_VIEW);
                                smsIntent
                                        .setData(Uri.parse(smsUri));
                                called = true;
                                startActivity(smsIntent);
                                tts.speak(noun + " को सन्देश भेजो!",TextToSpeech.QUEUE_FLUSH, null);

                            }


                        }
                        phones.close();

                    }

                }

                if(!called) tts.speak(noun + " आपकी संपर्क सूची में नहीं है!",TextToSpeech.QUEUE_FLUSH, null);

            }

        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    private void doCall(String noun) {
        System.out.println("call");
        System.out.println(noun);


        Cursor cur = null;
        ContentResolver cr = null;

        try {
            cr = getContentResolver();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null,
                    null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            boolean called = false;
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {

                    String id = cur.getString(cur
                            .getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur
                            .getString(cur
                                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    // Log.i("Names", name);
                    if (Integer
                            .parseInt(cur.getString(cur
                                    .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        // Query phone here. Covered next
                        Cursor phones = getContentResolver()
                                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                                + " = " + id, null, null);
                        while (phones.moveToNext()) {
                            String phoneNumberX = phones
                                    .getString(phones
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            // Log.i("Number", phoneNumber);
                            boolean a = (name.equalsIgnoreCase(noun) );
                            System.out.println(name + "  " + a + "  "
                                    + phoneNumberX);

                            if (a)
                                System.out.println(phoneNumberX);


                            if (a) {
                                String b = "tel:";
                                String phoneCallUri = b + phoneNumberX;
                                Intent phoneCallIntent = new Intent(
                                        Intent.ACTION_CALL);
                                phoneCallIntent
                                        .setData(Uri.parse(phoneCallUri));
                                called = true;
                                startActivity(phoneCallIntent);

                            }


                        }
                        phones.close();

                    }

                }

                if(!called) tts.speak(noun + " आपकी संपर्क सूची में नहीं है!",TextToSpeech.QUEUE_FLUSH, null);

            }

        } catch (Exception ex) {
            ex.printStackTrace();

        }
        // tts.speak(noun + " आपकी संपर्क सूची में नहीं है!",TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(),DoTheTrickActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {
        @Override
        protected Weather doInBackground(String... params) {
            Weather weather = new Weather();
            String data = ( (new WeatherHttpClient()).getWeatherData(params[0]));
            try {
                weather = JSONWeatherParser.getWeather(data);
// Let's retrieve the icon
                weather.iconData = ( (new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }
        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            String temp = String.format("%.2f", (weather.temperature.getTemp() - 273.15));
            // String max = ""+ (weather.temperature.getMaxTemp() - 273.15);
            // String min = ""+ (weather.temperature.getMinTemp() - 273.15);
            String hum = "" +Math.round( weather.currentCondition.getHumidity());
            String speak = "मुंबई का मौसम\nतापमान  " + temp+ " अंश सेल्सियस\nआर्द्रता "+hum+" प्रतिशत ";
            outputText.setText(speak);
            tts.speak(speak, TextToSpeech.QUEUE_FLUSH,null);
/*
            cityText.setText(weather.location.getCity() + "," + weather.location.getCountry());
            condDescr.setText(weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescr() + ")");
            temp.setText("" + Math.round((weather.temperature.getTemp() - 273.15)) + "�C");
            hum.setText("" + weather.currentCondition.getHumidity() + "%");
            press.setText("" + weather.currentCondition.getPressure() + " hPa");
            windSpeed.setText("" + weather.wind.getSpeed() + " mps");
            windDeg.setText("" + weather.wind.getDeg() + "�");*/
        }
    }
}
