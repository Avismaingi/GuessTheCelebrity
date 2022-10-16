package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int choseCeleb = 0;
    ImageView image;

    String[] answers = new String[4];
    int locationAnswer = 0;

    Button o1;
    Button o2;
    Button o3;
    Button o4;

    public void submit(View view){
        if(view.getTag().toString().equals(Integer.toString(locationAnswer))){
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(this, "Wrong!", Toast.LENGTH_SHORT).show();
        }

        newQuestion();
    }

    public void newQuestion(){
        try {
            Random rand = new Random();
            choseCeleb = rand.nextInt(celebURLs.size());

            Log.i("test", "choseCeleb = " + choseCeleb);

            Log.i("test", "Celeb = " + celebNames.get(choseCeleb));
            Log.i("test", "URL = " + celebURLs.get(choseCeleb));

            ImageDownloader imageTask = new ImageDownloader();

            Bitmap celebImage = imageTask.execute(celebURLs.get(choseCeleb)).get();

            image.setImageBitmap(celebImage);

            locationAnswer = rand.nextInt(4);

            Log.i("test", "locationAnswer = " + locationAnswer);
            int incorrectLocation;

            for (int i = 0; i < 4; i++) {
                if (i == locationAnswer) {
                    answers[i] = celebNames.get(choseCeleb);
                } else {
                    incorrectLocation = rand.nextInt(celebURLs.size());

                    // To avoid getting correct answer

                    while (incorrectLocation == choseCeleb) {
                        incorrectLocation = rand.nextInt(celebURLs.size());
                    }
                    answers[i] = celebNames.get(incorrectLocation);
                }
            }

            o1.setText(answers[0]);
            o2.setText(answers[1]);
            o3.setText(answers[2]);
            o4.setText(answers[3]);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream in = urlConnection.getInputStream();

                // Takes all elements of stream in one swoop
                Bitmap myBitmap = BitmapFactory.decodeStream(in);

                return myBitmap;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        // String... means that zero or more String objects (or a single array of them) may be passed as the argument(s) for that method.
        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null; //Kind of a browser, to get the text from URL

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                // To gather the data coming through we create an input stream
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    // For increment
                    data = reader.read();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }
            return result;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView) findViewById(R.id.imageView);

        o1 = (Button) findViewById(R.id.o1);
        o2 = (Button) findViewById(R.id.o2);
        o3 = (Button) findViewById(R.id.o3);
        o4 = (Button) findViewById(R.id.o4);

        DownloadTask task = new DownloadTask();

        //What is returned from background thread(class)
        String result = null;
        try {
            result = task.execute("https://www.imdb.com/list/ls052283250/").get();

            String[] splitResult = result.split("footer filmosearch");
            result = splitResult[0];
            splitResult = result.split("header filmosearch");
            result = splitResult[1];

            Pattern p1 = Pattern.compile("<img alt=\"(.*?)\"\n");
            Matcher m1 = p1.matcher(result);
            while (m1.find()) {
//                System.out.println(m1.group(1));
                celebNames.add(m1.group(1));
            }
            Pattern p2 = Pattern.compile("src=\"(.*?)\"\n");
            Matcher m2 = p2.matcher(result);
            while (m2.find()) {
//                System.out.println(m2.group(1));
                celebURLs.add(m2.group(1));
            }

            newQuestion();

        } catch (Exception e) {
            //Log.i("Exception", e.toString());
            e.printStackTrace();
        }

//        Log.i("Result", result);
    }
}