package com.twominuteplays.firebase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.twominuteplays.db.FirebaseStuff;

public class ShareCounter {
    private static final String TAG = ShareCounter.class.getName();

    private final OnCounterIncremented nextvalCallback;
    private final OnFailure failureCallback;

    public ShareCounter(@NonNull OnCounterIncremented nextvalCallback, @Nullable OnFailure failureCallback) {
        this.nextvalCallback = nextvalCallback;
        this.failureCallback = failureCallback;
    }

    public void nextval() {
        FirebaseStuff.getFirebase().child("shareCounter").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long nextval = (Long) mutableData.getValue();
                if (nextval == null) {
                    mutableData.setValue(1000L);
                }
                else {
                    mutableData.setValue(nextval + 1L);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean success, DataSnapshot dataSnapshot) {
                if (success) {
                    nextvalCallback.nextval(dataSnapshot.getValue(Long.class));
                }
                else {
                    Log.e(TAG, "Unable to get nextval. " + databaseError.getMessage(), databaseError.toException());
                    if (failureCallback != null) {
                        failureCallback.error(databaseError);
                    }
                }
            }
        });
    }

    public interface OnCounterIncremented {
        void nextval(Long value);
    }

    public interface OnFailure {
        void error(DatabaseError databaseError);
    }
}
