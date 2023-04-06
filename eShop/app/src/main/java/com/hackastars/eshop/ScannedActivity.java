package com.hackastars.eshop;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import timber.log.Timber;


public class ScannedActivity extends AppCompatActivity {
	private String upc;
	private String name;
	private String imageLink;
	private int averageRating;

	private static final String DEFAULT_PRODUCT_NAME = "Unknown Item";
	private static final int DEFAULT_PRODUCT_IMAGE = R.drawable.question_mark;
	private static final int DEFAULT_GRADE_IMAGE = R.drawable.question_mark;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanned);

		// Get references to UI elements
		TextView itemNameTextView = findViewById(R.id.itemNameTextView);
		ImageView productImage = findViewById(R.id.productImage);
		ImageView averageGradeImage = findViewById(R.id.averageGradeImageView);

		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			upc = extras.getString("upc", "Unknown");
			name = extras.getString("name", String.format("%s (UPC: %s)", DEFAULT_PRODUCT_NAME, upc));
			imageLink = extras.getString("imageLink", "");
			averageRating = extras.getInt("averageGrade", -1);
		}

		// Set UI elements
		itemNameTextView.setText(name);

		if (!imageLink.isEmpty()) {
			Glide.with(this).load(imageLink).into(productImage);
		}

		if (averageRating != -1) {
			averageGradeImage.setImageResource(getGradeImageName());
		}
	}

	private int getGradeImageName() {
		switch (averageRating) {
			case 1:
				return R.drawable.f;
			case 2:
				return R.drawable.d_minus;
			case 3:
				return R.drawable.d;
			case 4:
				return R.drawable.d_plus;
			case 5:
				return R.drawable.c_minus;
			case 6:
				return R.drawable.c;
			case 7:
				return R.drawable.c_plus_plus;
			case 8:
				return R.drawable.b_minus;
			case 9:
				return R.drawable.b;
			case 10:
				return R.drawable.b_plus;
			case 11:
				return R.drawable.a_minus;
			case 12:
				return R.drawable.a;
			case 13:
				return R.drawable.a_plus;
			default: // If grade is invalid, default to f
				return R.drawable.f;
		}
	}
}