package com.hackastars.eshop;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;


public class ShoppingCartActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ShoppingCartAdapter mAdapter;
    private List<String> mShoppingCartList;

    private List<String> mShoppingCartList2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        mRecyclerView = findViewById(R.id.shopping_cart_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mShoppingCartList = new ArrayList<>();
        mShoppingCartList2 = new ArrayList<>();

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

        private List<String> mDataList;

        public ShoppingCartAdapter(List<String> dataList) {
            mDataList = dataList;
        }

        public class ShoppingCartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView itemNameTextView;

            public ShoppingCartViewHolder(View itemView) {
                super(itemView);
                itemNameTextView = itemView.findViewById(R.id.item_name);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                String itemName = mDataList.get(position);
                Intent intent = new Intent(view.getContext(), ScannedActivityAa.class);
                intent.putExtra("itemName", itemName);
                view.getContext().startActivity(intent);
            }
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
                    .url("http://api.arianb.me:8000/WholeCart")
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

                String[] result_array = result.split(",");

                String[] company_array = new String[result_array.length / 4];

                String[] score_array =  new String[result_array.length / 4];

                Timber.e(String.valueOf(result_array.length));
                Timber.e(String.valueOf(company_array.length));
                int i = 1;
                int j = 0;
                while (i < result_array.length) {
                    try {
                        company_array[j] = result_array[i];
                        score_array[j] = result_array[i-1];
                        i = i + 4;
                        j++;
                    }
                    catch(Exception e){
                        break;
                    }
                }

                List<String> resultList = new ArrayList<>(Arrays.asList(company_array));
                List<String> resultList2 = new ArrayList<>(Arrays.asList(score_array));
                // Add the parsed result to the shopping cart list
                mShoppingCartList.clear();
                mShoppingCartList.addAll(resultList);
                mShoppingCartList2.addAll(resultList2);

                // Notify the adapter that the data has changed
                mAdapter.notifyDataSetChanged();
            } else {
                Timber.e("FAIL LOL");
            }
        }
    }
}
