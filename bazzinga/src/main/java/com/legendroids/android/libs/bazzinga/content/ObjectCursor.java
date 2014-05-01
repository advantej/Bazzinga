package com.legendroids.android.libs.bazzinga.content;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.SparseArray;

import com.legendroids.android.libs.bazzinga.model.AbstractBaseModel;

/**
 * Created by advantej on 4/7/14.
 */
public class ObjectCursor<T extends AbstractBaseModel> extends CursorWrapper
{

    T model;
    private final SparseArray<T> mCache;
    Class<T> mModelClass;

    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public ObjectCursor(Cursor cursor, Class<T> modelClass)
    {
        super(cursor);

        if (cursor != null)
        {
            mCache = new SparseArray<T>(cursor.getCount());
        } else
        {
            mCache = null;
        }

        mModelClass = modelClass;


    }

    public final T getModel()
    {
        final Cursor c = getWrappedCursor();
        if (c == null)
        {
            return null;
        }
        final int currentPosition = c.getPosition();
        // The cache contains this object, return it.
        final T prev = mCache.get(currentPosition);
        if (prev != null)
        {
            return prev;
        }
        // Get the object at the current position and add it to the cache.
        try
        {
            model = mModelClass.newInstance();
        } catch (InstantiationException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

        if (model != null)
        {
            model.updateFromCursor(c);
        }

        mCache.put(currentPosition, model);

        return model;
    }


    /**
     * Reads the entire cursor to populate the objects in the cache. Subsequent calls to {@link
     * #getModel()} will return the cached objects as far as the underlying cursor does not change.
     */
    public final void fillCache()
    {
        final Cursor c = getWrappedCursor();
        if (c == null || !c.moveToFirst())
        {
            return;
        }
        do
        {
            // As a side effect of getModel, the model is cached away.
            getModel();
        } while (c.moveToNext());
    }

    @Override
    public void close()
    {
        super.close();
        mCache.clear();
    }


}
