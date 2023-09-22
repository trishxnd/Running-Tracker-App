package com.example.runningtracker.contentprovider;

import static com.example.runningtracker.contentprovider.DataProviderContract.TIMELOGGED;
import static com.example.runningtracker.db.MyDatabase.getDatabase;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;

import com.example.runningtracker.db.MyDatabase;
import com.example.runningtracker.db.Run;
import com.example.runningtracker.db.RunDao;

public class DataContentProvider extends ContentProvider {
    private MyDatabase db;
    private RunDao dao;
    private static final UriMatcher uriMatcher;


    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(DataProviderContract.AUTHORITY, "run_table",1);
        uriMatcher.addURI(DataProviderContract.AUTHORITY, "run_table/#",2);
        uriMatcher.addURI(DataProviderContract.AUTHORITY, "run_table/timeRan",3);
        uriMatcher.addURI(DataProviderContract.AUTHORITY, "run_table/distance",4);
        uriMatcher.addURI(DataProviderContract.AUTHORITY, "run_table/averageSpeed",5);
    }

    public static Run fromContentValues(ContentValues values) {
        long timeLogged = values.getAsLong("timeLogged");
        String dateLogged = values.getAsString("dateLogged");
        long timeRan = values.getAsLong("timeRan");
        double distance = values.getAsDouble("distance");
        double averageSpeed = values.getAsDouble("averageSpeed");
        String comment = values.getAsString("comment");

        return new Run(timeLogged, dateLogged, timeRan, distance, averageSpeed, comment);
    }

    @Override
    public boolean onCreate() {
        db = getDatabase(this.getContext());
        dao = db.runDao();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        final Cursor cursor;
        Context context = getContext();

        if(context == null){
            return null;
        }

        switch(uriMatcher.match(uri)){
            case 2:
                cursor = (Cursor) dao.getRuns();
                break;
            case 1:
                cursor = (Cursor) dao.getRunByTimeLogged(ContentUris.parseId(uri));
                break;
            case 3:
                cursor = (Cursor) dao.getSumOfTimeRan();
                break;
            case 4:
                cursor = (Cursor) dao.getSumOfDistance();
                break;
            case 5:
                cursor = (Cursor) dao.getMeanAverageSpeed();
                break;
            default:
                return null;


        }
        cursor.setNotificationUri(context.getContentResolver(),uri);
        return cursor;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        String contentType;

        if(uri.getLastPathSegment() == null){
            contentType = DataProviderContract.CONTENT_TYPE_MULTIPLE;
        }
        else{
            contentType = DataProviderContract.CONTENT_TYPE_SINGLE;
        }
        return contentType;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        String tableName;
        Run run = fromContentValues(contentValues);
        long id = dao.insert(run);
        Uri nu = ContentUris.withAppendedId(uri,id);
        getContext().getContentResolver().notifyChange(nu,null);
        return nu;

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
