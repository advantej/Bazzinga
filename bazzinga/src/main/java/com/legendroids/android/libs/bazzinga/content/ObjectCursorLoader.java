package com.legendroids.android.libs.bazzinga.content;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;

import com.legendroids.android.libs.bazzinga.model.AbstractBaseModel;

/**
 * Created by advantej on 4/7/14.
 */
public class ObjectCursorLoader<T extends AbstractBaseModel> extends AsyncTaskLoader<ObjectCursor<T>>
{
    private static final String TAG = "ObjectCursorLoader";

    private ObjectCursor<T> mWrappedCursor;
    private final Loader.ForceLoadContentObserver mObserver;
    private BaseDb mBaseDb;

    private String mTableName;
    private String[] mTableColumns;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mGroupBy;
    private String mHaving;
    private String mOrderBy;
    private String mLimit;
    private String mUniqueUri;

    private Class<T> mModelClass;


    private void init(BaseDb db, Class<T> modelClass, Context context, String uniqueUri, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit)
    {
        mModelClass = modelClass;
        mBaseDb = db;
        mTableName = table;
        mTableColumns = columns;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mGroupBy = groupBy;
        mHaving = having;
        mOrderBy = orderBy;
        mLimit = limit;
        mUniqueUri = uniqueUri;
    }

    public ObjectCursorLoader(BaseDb db, Class<T> modelClass, Context context, String uniqueUri, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit)
    {
        super(context);
        mObserver = new ForceLoadContentObserver();
        init(db, modelClass, context, uniqueUri, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public ObjectCursorLoader(BaseDb db, Class<T> modelClass, Context context, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit)
    {
        super(context);
        mObserver = new ForceLoadContentObserver();
        init(db, modelClass, context, null, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    // Runs on a worker thread
    @Override
    public ObjectCursor<T> loadInBackground()
    {
        Cursor hiddenCursor = mBaseDb.query(mUniqueUri, mTableName, mTableColumns, mSelection, mSelectionArgs, mGroupBy, mHaving, mOrderBy, mLimit);

        if (hiddenCursor != null)
        {
            hiddenCursor.getCount();
            hiddenCursor.registerContentObserver(mObserver);
        }

        ObjectCursor<T> wrappedCursor =  new ObjectCursor<T>(hiddenCursor, mModelClass);

        wrappedCursor.fillCache();

        return wrappedCursor;
    }



    @Override
    public void deliverResult(ObjectCursor<T> wrappedCursorToDeliver)
    {
        if (isReset())
        {
            if (wrappedCursorToDeliver != null)
            {
                wrappedCursorToDeliver.close();
            }
            return;
        }

        ObjectCursor<T> oldWrappedCursor = mWrappedCursor;
        mWrappedCursor = wrappedCursorToDeliver;

        if (isStarted())
        {
            super.deliverResult(wrappedCursorToDeliver);
        }

        if (oldWrappedCursor != null && oldWrappedCursor != wrappedCursorToDeliver && !oldWrappedCursor.isClosed())
        {
            oldWrappedCursor.close();
        }
    }

    @Override
    protected void onStartLoading()
    {
        if (mWrappedCursor != null)
        {
            deliverResult(mWrappedCursor);
        }

        if (mWrappedCursor == null || takeContentChanged())
        {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading()
    {
        cancelLoad();
    }

    @Override
    public void onCanceled(ObjectCursor<T> wrappedCursor)
    {
        if (wrappedCursor != null && !wrappedCursor.isClosed())
        {
            wrappedCursor.close();
        }
    }

    @Override
    protected void onReset()
    {
        super.onReset();

        stopLoading();

        if (mWrappedCursor != null && !mWrappedCursor.isClosed())
        {
            mWrappedCursor.close();
        }

        mWrappedCursor = null;

    }

    @Override
    protected void onForceLoad()
    {
        super.onForceLoad();
    }

    @Override
    public void onContentChanged()
    {
        super.onContentChanged();
    }

    public void setSelectionAndArgs(String selection, String[] selectionArgs)
    {
        mSelection = selection;
        mSelectionArgs = selectionArgs;
    }

    public void setOrderBy(String orderBy)
    {
        mOrderBy = orderBy;
    }
}
