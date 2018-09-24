package com.xenown.htn;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.service.exception.NotFoundException;
import com.ibm.watson.developer_cloud.service.exception.RequestTooLargeException;
import com.ibm.watson.developer_cloud.service.exception.ServiceResponseException;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;

public class WatsonCall extends AsyncTask<String, Void, String> {

    private Result delegate;
    private Context c;

    public WatsonCall(Result delegate, Context context){
        this.delegate = delegate;
        this.c = context;
    }

    @Override
    protected String doInBackground(String... s) {
 //       HttpClient httpclient = HttpClients.createDefault();
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);

//                try{
//                    MediaPlayer mediaPlayer = new MediaPlayer();
//                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                mediaPlayer.setDataSource(c, Uri.parse(s[0]));
//                mediaPlayer.prepare();
//                mediaPlayer.start();
//                } catch (Exception e){
//
//                }
//            try {
//                SpeechToText speechToText = new SpeechToText();
//                speechToText
//                        .setUsernameAndPassword(Config.STUN,Config.STPW);
//
//                speechToText.setEndPoint(Config.STURL);
//                File f = new File(s[0]);
//                Log.d("test", Boolean.toString(f.exists()));
//                RecognizeOptions options = new RecognizeOptions.Builder()
//                        .audio(f)
//                        .contentType(HttpMediaType.AUDIO_WAV)
//                        .build();
//                SpeechRecognitionResults returnValue = speechToText.recognize(options)
//                        //.addHeader("Custom-Header", "{header_value}")
//                        .execute();
//
//                return returnValue.toString();
//            } catch (IOException e) {
//                Log.d("TESTING", e.getMessage());
//            } catch (NotFoundException e) {
//                Log.d("TESTING", e.getMessage());
//                // Handle Not Found (404) exception
//            } catch (RequestTooLargeException e) {
//                Log.d("TESTING", e.getMessage());
//                // Handle Request Too Large (413) exception
//            } catch (ServiceResponseException e) {
//                // Base class for all exceptions caused by error responses from the service
//                System.out.println("Service returned status code " + e.getStatusCode() + ": " + e.getMessage());
//            } catch (Exception e){
//                Log.d("TESTING", e.getMessage());
//            }
//            return "No";

//            URIBuilder builder = new URIBuilder(Config.STURL);
//
//            URI uri = builder.build();
//            HttpPost request = new HttpPost(uri);
//            request.setHeader("Content-Type", "audio/wav");
//            request.setHeader("Ocp-Apim-Subscription-Key", "ae1cef46a5c24bc4be6864e95d725ac1");
//
//
//            // Request body. The parameter of setEntity converts the image to base64
//            request.setEntity(new ByteArrayEntity(toBase64(files[0])));
//
//            // getting a response and assigning it to the string res
//            HttpResponse response = httpclient.execute(request);
//            HttpEntity entity = response.getEntity();
//            String res = EntityUtils.toString(entity);

        URL url = null;             //Url is defined by argument address
        try {
            url = new URL(Config.STURL);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");           //HTTP Connection is opened and request type is set to POST.
            con.setRequestProperty("Content-Type", "audio/wav");
            con.setDoOutput(true);                  //Allows me to receive information

            String userpass = Config.STUN + ":" + Config.STPW;
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes("UTF-8"));
            con.setRequestProperty ("Authorization", basicAuth);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            String toSend = "timestamps=true&profanity_filter=false";
            wr.writeBytes(toSend);                  //Dataoutputstream is created to send the argument information
            wr.flush();                             //AKA the stop code and route number
            wr.close();                             //Stream is flushed and closed

            InputStream in = new BufferedInputStream(con.getInputStream());
            return getData(in);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "no";
//        catch (Exception e){
//            Log.d("WatsonCAll", e.getMessage());
//            return "null";
//        }
    }

    @Override
    protected void onPostExecute(String s) {
        this.delegate.processFinished(s);
    }

    public byte[] toBase64(File file) {
        try {
            byte[] bytesArray = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(bytesArray);
            fis.close();
            return bytesArray;
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        } catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        }
        return null;
    }

    private String getData (InputStream is) {      //Method to get the JSON information
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();    //Buffered reader and Stringbuilder for efficient input and storage of information

        String line;
        try {
            while ((line = reader.readLine()) != null) {    //Reads in nextline and adds to stringbuilder if there is a line.
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            Log.e("WatsonCall", "IOException: " + e.getMessage());
        } finally {
            try {
                is.close();                                 //Stream is closed.
            } catch (IOException e) {
                Log.e("WatsonCall", "IOException: " + e.getMessage());
            }
        }
        return sb.toString();                               //JSON info is returned
    }
}
