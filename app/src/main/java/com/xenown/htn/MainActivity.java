package com.xenown.htn;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements Result {

    private VideoView mVideoView;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    private FFmpeg ffmpeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dispatchTakeVideoIntent();
        mVideoView = findViewById(R.id.vid);
        ffmpeg = FFmpeg.getInstance(this);

        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onSuccess() {
                    Log.d("MainActivity", "Compatible!");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            //TODO: Handle if FFmpeg is not supported by device
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
                Uri videoUri = intent.getData();


                String mp4Location = getRealPathFromURI(videoUri, this);

                //mp4Location = Utils.getRealPathFromURI(mp4Location, this);
                String wavLocation = mp4Location.substring(0, mp4Location.length()-4) + ".wav";
//                Log.d("MainActivity", mp4Location);
//                Log.d("MainActivity", wavLocation);
                executeCmd("-i " + mp4Location + " " + wavLocation);



                //File audio = new File(wavLocation)
                //File audio = new File("/storage/emulated/0/DCIM/Camera/20180915_145519.wav");
                //Log.d("test", Boolean.toString(audio.exists()));
                WatsonCall call = new WatsonCall(MainActivity.this, this);
//                try{
//                    MediaPlayer mediaPlayer = new MediaPlayer();
//                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(audio.getPath()));
//                mediaPlayer.prepare();
//                mediaPlayer.start();
//                } catch (Exception e){
//
//                }
                call.execute("/storage/emulated/0/DCIM/Camera/20180915_145519.wav");
        }
    }

    private String getRealPathFromURI(Uri contentUri, Context mContext) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(mContext, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private void executeCmd(final String command) {
        try {
            String[] cmd = command.split(" ");
            if (command.length() != 0) {
                ffmpeg.execute(cmd , new ExecuteBinaryResponseHandler() {

                    @Override
                    public void onSuccess(String s) {
                        Log.d("MainActivity", "S");
                        //FFmpeg command successfully executed
                    }
                    @Override
                    public void onFailure(String s) {
                        Log.d("MainActivity", "F");
                        //FFmpeg command fail to execute
                    }
                });
            }
        } catch (FFmpegCommandAlreadyRunningException e) {
            //There is a command already running
        }
    }

    @Override
    public void processFinished(String res) {
        Log.d("YAYA", res);
    }
}
