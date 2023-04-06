package com.hackastars.eshop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.BasicResponseHandler;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.CloseableHttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    public static final String API_ENDPOINT = "http://api.arianb.me:8000";

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private static IntentIntegrator intentIntegrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable logging
        Timber.plant(new Timber.DebugTree());

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
                startActivity(myIntent);
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
                String upc = intentResult.getContents();
                CloseableHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(String.format("%s/getItemInfo/%s", API_ENDPOINT, upc));
                new Thread(() -> {
                    try {
                        HttpResponse response = httpclient.execute(httpGet);
                        httpclient.close();

                        // TODO: Store and parse response, then send intent with extras relating to product data
                        String jsonString = new BasicResponseHandler().handleResponse(response);
                        jsonString = jsonString.replace("\\", "");
                        jsonString = jsonString.substring(jsonString.indexOf("{"), jsonString.lastIndexOf("}") + 1);

                        Timber.d("jsonString: %s", jsonString);

                        Intent intent = new Intent(MainActivity.this, ScannedActivity.class);
                        try {
                            if (jsonString.equals("{}")) {
                                throw new JSONException("UPC was not found in database");
                            }
                            JSONObject json = new JSONObject(jsonString);
                            intent.putExtra("upc", json.getString("upc"));
                            intent.putExtra("name", json.getString("name"));
                            intent.putExtra("imageLink", json.getString("link"));
                            intent.putExtra("averageGrade", json.getInt("avg_grade"));
                        } catch (JSONException e) {
                            intent.putExtra("upc", upc);
                            Timber.e(e);
                        }
                        startActivity(intent);
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                }).start();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
