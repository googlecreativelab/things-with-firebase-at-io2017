package net.trippedout.android.thingswithfirebase.listeners;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by atripaldi on 3/28/17.
 */

public class SimpleValueListener implements ValueEventListener {

    private static final String TAG = SimpleValueListener.class.getSimpleName();

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if(dataSnapshot.getValue() != null) {
            if(dataSnapshot.hasChildren())
                onChildValues(dataSnapshot.getChildren());
            else
                onValue(dataSnapshot);
        }
    }

    public void onChildValues(Iterable<DataSnapshot> children) {

    }

    public void onValue(DataSnapshot value) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "Failed to read value.", databaseError.toException());
    }
}
