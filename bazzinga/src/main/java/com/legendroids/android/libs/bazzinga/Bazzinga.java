package com.legendroids.android.libs.bazzinga;

import android.content.Context;

import com.legendroids.android.libs.bazzinga.content.BaseDb;

/**
 * Created by advantej on 6/30/14.
 */
public class Bazzinga {

    public static void init(Context context, String packageName, Class[] modelClasses, String dbName, int dbVersion) {
        BaseDb db = BaseDb.initDatabase(context, packageName, dbName, dbVersion, modelClasses);
        db.checkDBStatusAndFix();
    }
}
