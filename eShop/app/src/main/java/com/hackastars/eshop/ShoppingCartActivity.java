package com.hackastars.eshop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ShoppingCartActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ShoppingCartAdapter mAdapter;
    private List<String> mShoppingCartList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        mRecyclerView = findViewById(R.id.shopping_cart_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mShoppingCartList = new ArrayList<>();

        mAdapter = new ShoppingCartAdapter(mShoppingCartList);
        mRecyclerView.setAdapter(mAdapter);

        // Start the network request
        new NetworkTask().execute();
    }

    private class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ShoppingCartViewHolder> {

        private List<String> mDataList;

        public ShoppingCartAdapter(List<String> dataList) {
            mDataList = dataList;
        }

        @Override
        public ShoppingCartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping_cart, parent, false);
            return new ShoppingCartViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ShoppingCartViewHolder holder, int position) {
            String itemName = mDataList.get(position);
            holder.itemNameTextView.setText(itemName);
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        public class ShoppingCartViewHolder extends RecyclerView.ViewHolder {
            public TextView itemNameTextView;

            public ShoppingCartViewHolder(View itemView) {
                super(itemView);
                itemNameTextView = itemView.findViewById(R.id.item_name);
            }
        }
    }

    private class NetworkTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://158.101.11.38:8000/WholeCart")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    return responseBody.string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                result = result.replace("{", "");
                result = result.replace("}", "");
                result = result.replace("[", "");
                result = result.replace("]", "");
                result = result.replace("'", "");
                result = result.replace("(", "");
                result = result.replace("\"", "");
                result = result.substring(8);


                String result_array[] = result.split(",");

                String company_array[] = new String[result_array.length / 4];

                Log.e("gfhgjkdfjgjkdfngh", String.valueOf(result_array.length));
                Log.e("gfhgjkdfjgjkdfngh", String.valueOf(company_array.length));
                int i = 1;
                int j = 0;
                while (i < result_array.length) {
                    try {
                        company_array[j] = result_array[i];
                        i = i + 4;
                        j++;
                    }
                    catch(Exception e){
                        break;
                    }
                }

                List<String> resultList = new ArrayList<>(Arrays.asList(company_array));
                // Add the parsed result to the shopping cart list
                mShoppingCartList.clear();
                mShoppingCartList.addAll(resultList);

                // Notify the adapter that the data has changed
                mAdapter.notifyDataSetChanged();
            } else {
                Log.e("gjkfldhjkfla", "FAIL LOL");
            }
        }
    }
}
