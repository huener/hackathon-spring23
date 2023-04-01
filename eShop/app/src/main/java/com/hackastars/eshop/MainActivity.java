package com.hackastars.eshop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.*;
import com.google.zxing.oned.UPCAReader;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private static IntentIntegrator intentIntegrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         intentIntegrator = new IntentIntegrator(this);

        Button cameraButton = findViewById(R.id.camera_button);
        ImageButton cartButton = findViewById(R.id.shopping_cart);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if camera permission is granted
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request camera permission if not granted
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] { Manifest.permission.CAMERA },
                            CAMERA_PERMISSION_CODE);
                } else {
                    // Scan stuff
                    intentIntegrator.setPrompt("Scan a barcode");
                    intentIntegrator.setBeepEnabled(false);
                    intentIntegrator.setOrientationLocked(false);
                    intentIntegrator.initiateScan();
                }


            }
        });

        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, ShoppingCartActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // if the intentResult is not null we'll set
                // the content of scan message
                String nums = intentResult.getContents();

                Toast.makeText(getBaseContext(), intentResult.getContents(), Toast.LENGTH_LONG).show();
                Log.i("myapp", intentResult.toString());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        Intent myIntent = new Intent(MainActivity.this, ScannedActivityAa.class);
        MainActivity.this.startActivity(myIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open camera
                //openCamera();
            }
        }
    }
}
