package com.example.texo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class MainActivity extends AppCompatActivity {

    EditText resulTxt;
    ImageView camera,dlt,copy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.btnScan);
        dlt = findViewById(R.id.eraseId);
        copy = findViewById(R.id.copyId);

        resulTxt = findViewById(R.id.resultId);


        //Camera Bottom function
        camera.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Toast.makeText(getApplicationContext(), "Access to Camera Permission ", Toast.LENGTH_SHORT).show();
                if(intent.resolveActivity(getPackageManager())!=null){
                    startActivityForResult(intent,123);
                }else{
                    Toast.makeText(getApplicationContext(), "Something Wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Erase Bottom Operation
        dlt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String output =  resulTxt.getText().toString();

                if(output.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Can't Delete,Text Box is Empty", Toast.LENGTH_SHORT).show();
                }else {
                    resulTxt.setText("");
                }
            }
        });


        //Copy to Clipboard operation
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String output =  resulTxt.getText().toString();

                if(output.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Can't Copy,Text Box is Empty", Toast.LENGTH_SHORT).show();
                }else{
                    ClipboardManager clipBoard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label",output);
                    clipBoard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 123 && resultCode == RESULT_OK){
            Bundle extras = data != null ? data.getExtras() : null;
            Bitmap bitmap = extras != null ? (Bitmap) extras.get("data") : null;
            detectTextUsingML(bitmap);
        }
    }

    private void detectTextUsingML(Bitmap bitmap) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                resulTxt.setText(visionText.getText());
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        e.printStackTrace();
                                    }
                                });
    }
}