package com.olx.techathon.sqlite;

import java.util.ArrayList;
import java.util.HashMap;

import com.olx.techathon.bean.Advertisement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "olx_hack.db";
	public static final String AD_TABLE_NAME = "Advertisement";
	public static final String AD_COLUMN_ID = "id";
	public static final String AD_COLUMN_TITLE = "title";
	public static final String AD_COLUMN_CATEGORY = "category";
	public static final String AD_COLUMN_DESC = "desc";
	public static final String AD_COLUMN_LATITUDE = "latitude";
	public static final String AD_COLUMN_LONGITUDE = "longitude";
	public static final String AD_COLUMN_USERNAME = "userName";
	public static final String AD_COLUMN_PHONE = "phoneNumber";
	public static final String AD_COLUMN_EMAIL = "email";
	private HashMap hp;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table "
				+ AD_TABLE_NAME
				+ "(id integer primary key, title text, category text, desc text, userName text, phoneNumber text, email text)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + AD_TABLE_NAME);
		onCreate(db);
	}

	public boolean insertAd(Advertisement ad) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(AD_COLUMN_USERNAME, ad.getUserName());
		contentValues.put(AD_COLUMN_DESC, ad.getDesc());
		contentValues.put(AD_COLUMN_CATEGORY, ad.getCategory());
		contentValues.put(AD_COLUMN_EMAIL, ad.getEmail());
		contentValues.put(AD_COLUMN_PHONE, ad.getPhoneNumber());
		contentValues.put(AD_COLUMN_TITLE, ad.getTitle());
		android.location.Location location = ad.getLocation();
		/*if (location != null) {
			contentValues.put(AD_COLUMN_LATITUDE, location.getLatitude());
			contentValues.put(AD_COLUMN_LONGITUDE, location.getLongitude());
		}*/
		db.insert(AD_TABLE_NAME, null, contentValues);
		return true;
	}

	public Cursor getData(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery("select * from Advertisement where id=" + id
				+ "", null);
		return res;
	}

	public int numberOfRows() {
		SQLiteDatabase db = this.getReadableDatabase();
		int numRows = (int) DatabaseUtils.queryNumEntries(db, AD_TABLE_NAME);
		return numRows;
	}

	public boolean updateContact(Advertisement ad) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();

		db.update(AD_TABLE_NAME, contentValues, "id = ? ",
				new String[] { Integer.toString(ad.getId()) });
		return true;
	}

	public Integer deleteContact(Integer id) {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(AD_TABLE_NAME, "id = ? ",
				new String[] { Integer.toString(id) });
	}

	public ArrayList<String> getAllAds() {
		ArrayList<String> array_list = new ArrayList<String>();

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery("select * from " + AD_TABLE_NAME, null);
		res.moveToFirst();

		while (res.isAfterLast() == false) {
			array_list.add(res.getString(res.getColumnIndex(AD_COLUMN_TITLE)));
			res.moveToNext();
		}
		return array_list;
	}
}