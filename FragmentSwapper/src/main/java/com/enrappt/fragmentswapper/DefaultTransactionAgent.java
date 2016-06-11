package com.enrappt.fragmentswapper;

import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

public class DefaultTransactionAgent implements TransactionAgent {

    @Override
    public void transactAdd(FragmentTransaction transaction, int containerId, Fragment fragment, String tag) {
        transaction.add(containerId, fragment, tag);
        transaction.commit();
    }

    @Override
    public void transactReplace(FragmentTransaction transaction, int containerId, Fragment fragment, String tag) {
        transaction.replace(containerId, fragment, tag);
        transaction.commit();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public DefaultTransactionAgent() {
    }

    protected DefaultTransactionAgent(Parcel in) {
    }

    public static final Creator<DefaultTransactionAgent> CREATOR = new Creator<DefaultTransactionAgent>() {
        @Override
        public DefaultTransactionAgent createFromParcel(Parcel source) {
            return new DefaultTransactionAgent(source);
        }

        @Override
        public DefaultTransactionAgent[] newArray(int size) {
            return new DefaultTransactionAgent[size];
        }
    };
}
