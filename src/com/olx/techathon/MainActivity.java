package com.olx.techathon;

import com.google.android.gms.common.ConnectionResult;
import com.olx.techathon.bean.Advertisement;
import com.olx.techathon.sqlite.DBHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemSelectedListener,
		OnClickListener, OnLocationChangeListener {

	private Spinner mCategorySpinner;
	private EditText mTitleEditText;
	private EditText mDescEditText;
	private EditText mLocationEditText;
	private EditText mNameEditText;
	private EditText mPhoneEditText;
	private EditText mEmailEditText;
	private ImageView mImage;

	private ImageButton mSelectLocationButton;
	private ImageButton mSelectImageButton;
	private Button mSubmitButton;

	private LocationHelper mLocationHelper;

	private String[] chooserArray;

	private Location mLocation;

	private DBHelper mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mLocationHelper = LocationHelper.getInstance();
		mLocationHelper.setLocationListener(this);
		mLocationHelper.buildGoogleApiClient(this);

		mTitleEditText = (EditText) findViewById(R.id.title_edit_text);
		mDescEditText = (EditText) findViewById(R.id.desc_edit_text);
		mLocationEditText = (EditText) findViewById(R.id.location_edit_text);
		mNameEditText = (EditText) findViewById(R.id.name_edit_text);
		mPhoneEditText = (EditText) findViewById(R.id.phone_edit_text);
		mEmailEditText = (EditText) findViewById(R.id.email_edit_text);
		mSelectLocationButton = (ImageButton) findViewById(R.id.location_button);
		mSelectLocationButton.setOnClickListener(this);
		mSelectImageButton = (ImageButton) findViewById(R.id.image_capture_button);
		mImage = (ImageView) findViewById(R.id.image_view);
		mSelectImageButton.setOnClickListener(this);
		mSubmitButton = (Button) findViewById(R.id.submit_button);
		mSubmitButton.setOnClickListener(this);

		mCategorySpinner = (Spinner) findViewById(R.id.category_spinner);
		mCategorySpinner.setOnItemSelectedListener(this);

		String[] categories = getResources().getStringArray(
				R.array.category_array);

		ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, categories);

		categoryAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCategorySpinner.setAdapter(categoryAdapter);

		chooserArray = getResources().getStringArray(R.array.chooser_array);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.image_capture_button:
			showCooserDialog();
			break;

		case R.id.submit_button:
			insertAd();
			break;
		
		default:
			break;
		}
	}

	private void showCooserDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setItems(chooserArray, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					startCamera();
				} else {
					selectImagefromGallery();
				}
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	public void selectImagefromGallery() {
		Intent galleryIntent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(galleryIntent, Util.RESULT_PICK_IMG);
	}

	private void startCamera() {
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, Util.RESULT_CAMERA_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && requestCode == Util.RESULT_PICK_IMG
				&& data != null) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String imageString = cursor.getString(columnIndex);

			Bitmap bitmap = BitmapFactory.decodeFile(imageString);
			if (bitmap != null) {
				mImage.setImageBitmap(bitmap);
				mSelectImageButton.setVisibility(View.GONE);
				mImage.setVisibility(View.VISIBLE);
			}
			cursor.close();
		} else if (resultCode == RESULT_OK
				&& requestCode == Util.RESULT_CAMERA_REQUEST && data != null) {
			Bitmap photo = (Bitmap) data.getExtras().get("data");
			if (photo != null) {
				mImage.setImageBitmap(photo);
				mSelectImageButton.setVisibility(View.GONE);
				mImage.setVisibility(View.VISIBLE);
			}
		}

		else {
			Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public void onLocationFound(Location location) {
		Log.v("Rahul", "onLocationFound Location : " + location);
		mLocation = location;
		if (location != null) {
			String locality = mLocationHelper.getAddressByLocation(this,
					location);
			Log.v("Rahul", "onLocationFound Location : " + locality);
			mLocationEditText.setText(locality);
		}
	}

	@Override
	public void onLocationFailed(ConnectionResult connectionResult) {
	}

	private void insertAd() {
		int categoryPos = mCategorySpinner.getSelectedItemPosition();
		String category = (String) mCategorySpinner.getSelectedItem();
		String title = mTitleEditText.getText().toString();
		String desc = mDescEditText.getText().toString();
		String userName = mNameEditText.getText().toString();
		String phone = mPhoneEditText.getText().toString();
		String email = mEmailEditText.getText().toString();

		if (categoryPos == 0 && title.isEmpty() && desc.isEmpty()
				&& userName.isEmpty() && phone.isEmpty()) {
			Toast.makeText(this, "Form Incomplete, please fill all the fields",
					Toast.LENGTH_LONG).show();
		} else {
			if (mDbHelper == null) {
				mDbHelper = new DBHelper(this);
			}
			Advertisement ad = new Advertisement();
			ad.setTitle(title);
			ad.setCategory(category);
			ad.setDesc(desc);
			ad.setPhoneNumber(phone);
			ad.setEmail(email);
			ad.setLocation(mLocation);
			ad.setUserName(userName);

			mDbHelper.insertAd(ad);
		}
	}
}
