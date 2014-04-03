package com.legendroids.android.libs.bazzinga.net;

public interface ResponseHandler2<T1, T2>
{
    public void onResponse(T1 t1, T2 t2, Throwable throwable);
}
