package com.example.texo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseIntArray;
import android.view.Surface;
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

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }
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


    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation()
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        // Cast the context to an activity.
        Activity activity = (Activity) getApplicationContext();
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // Get the device's sensor orientation.
        CameraManager cameraManager = (CameraManager) activity.getSystemService(CAMERA_SERVICE);
        String[] cameraId = new String[0];
        try {
            cameraId = cameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            // Handle camera access exception (e.g., permission not granted)
            e.printStackTrace();
        }

        int sensorOrientation = cameraManager
                .getCameraCharacteristics(Arrays.toString(cameraId))
                .get(CameraCharacteristics.SENSOR_ORIENTATION);

        rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360;

        return rotationCompensation;
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

    private void detectTextUsingML(Bitmap bitmap)  {
        //step 1
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        //step 2

//        int rotationDegrees = getRotationCompensation();
        //step 3
        InputImage image = InputImage.fromBitmap(bitmap, 0);


        //step
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