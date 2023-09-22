package com.example.runningtracker.contentprovider;

import android.net.Uri;

public class DataProviderContract {

        public static final String AUTHORITY = "com.example.runningtracker.contentprovider.dataContentProvider";

        public static final Uri RUN_URI = Uri.parse("content://"+AUTHORITY+"run_table");

        public final static String TIMELOGGED = "timeLogged";
        public final static String DATE = "dateLogged";
        public final static String TIMERAN = "timeRan";
        public final static String DISTANCE = "distance";
        public final static String AVERAGESPEED = "averageSpeed";
        public final static String COMMENT = "comment";

        public final static String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/dataContentProvider.data.text";
        public final static String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/dataContentProvider.data.text";


}
