package com.hackastars.eshop;

import static com.hackastars.eshop.MainActivity.API_ENDPOINT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

import androidx.recyclerview.widget.ItemTouchHelper;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.CloseableHttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ShoppingCartActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ShoppingCartAdapter mAdapter;
    private List<Product> mShoppingCartList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        mRecyclerView = findViewById(R.id.shopping_cart_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mShoppingCartList = new ArrayList<>();

        mAdapter = new ShoppingCartAdapter(mShoppingCartList);
        mRecyclerView.setAdapter(mAdapter);

        // Create an ItemTouchHelper that will handle swipe gestures
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                // Do nothing - we don't want to move items up or down in the list
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Get the position of the item that was swiped
                int position = viewHolder.getAdapterPosition();

                // remove from cart
                Product item = mShoppingCartList.get(position);
                CloseableHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(String.format("%s/removeItem/%s", API_ENDPOINT, item.getUpc()));
                new Thread(() -> {
                    try {
                        HttpResponse response = httpclient.execute(httpGet);
                        httpclient.close();
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                }).start();

                // Remove the item from the list
                mShoppingCartList.remove(position);

                // Notify the adapter that an item has been removed
                mAdapter.notifyItemRemoved(position);
            }
        });

        // Attach the ItemTouchHelper to the RecyclerView
        itemTouchHelper.attachToRecyclerView(mRecyclerView);


        // Start the network request
        new NetworkTask().execute();
    }

    private class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ShoppingCartViewHolder> {

        private final List<Product> mDataList;

        public ShoppingCartAdapter(List<Product> dataList) {
            mDataList = dataList;
        }

        public class ShoppingCartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView itemNameTextView;
            public ImageView gradeImageView;

            public ShoppingCartViewHolder(View itemView) {
                super(itemView);
                itemNameTextView = itemView.findViewById(R.id.item_name);
                gradeImageView = itemView.findViewById(R.id.ratingIcon);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                Product product = mDataList.get(position);
                Intent intent = new Intent(view.getContext(), ScannedActivity.class);
                intent.putExtra("upc", product.getUpc());
                intent.putExtra("name", product.getName());
                intent.putExtra("imageLink", product.getImageLink());
                intent.putExtra("averageGrade", product.getAverageGrade());
                Timber.d("avg grade: %s", product.getAverageGrade());
                startActivity(intent);
            }
        }


        @Override
        public void onBindViewHolder(ShoppingCartViewHolder holder, int position) {
            Product product = mDataList.get(position);
            holder.itemNameTextView.setText(product.getName());
            holder.gradeImageView.setImageResource(ScannedActivity.getGradeResource(product.getAverageGrade()));
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        @NonNull
        @Override
        public ShoppingCartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping_cart, parent, false);
            return new ShoppingCartViewHolder(view);
        }

    }

    private class NetworkTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(String.format("%s/WholeCart", API_ENDPOINT))
                    .build();

            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    return responseBody.string();
                }
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                List<Product> resultList = new ArrayList<>();
                try {
                    JSONArray cart;
                    cart = new JSONArray(result);
                    for (int i = 0; i < cart.length(); i++) {
                        JSONObject item = cart.getJSONObject(i);
                        String upc = item.getString("upc");
                        String name = item.getString("name");
                        String imageLink = item.getString("link");
                        int averageGrade = item.getInt("avg_grade");

                        Product temp = new Product(upc, name, imageLink, averageGrade);
                        resultList.add(temp);
                    }
                } catch (JSONException e) {
                    Timber.e(e);
                    return;
                }

                // Add the parsed result to the shopping cart list
                mShoppingCartList.clear();
                mShoppingCartList.addAll(resultList);

                // Notify the adapter that the data has changed
                mAdapter.notifyDataSetChanged();
            } else {
                Timber.e("FAIL LOL");
            }
        }
    }
}
