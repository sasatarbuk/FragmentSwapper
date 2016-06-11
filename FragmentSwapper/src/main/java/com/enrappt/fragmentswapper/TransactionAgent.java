package com.enrappt.fragmentswapper;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

/**
 * Swap transaction handler
 */
public interface TransactionAgent extends Parcelable {

    /**
     * Perform initial transaction
     *
     * @param transaction Transaction to be performed
     * @param containerId Container ID to hold the new fragment
     * @param fragment New fragment
     * @param tag Tag identifying the fragment
     */
    public void transactAdd(FragmentTransaction transaction, int containerId, Fragment fragment, String tag);

    /**
     * Perform swapping transaction
     *
     * @param transaction Transaction to be performed
     * @param containerId Container ID to hold the new fragment
     * @param fragment New fragment
     * @param tag Tag identifying the fragment
     */
    public void transactReplace(FragmentTransaction transaction, int containerId, Fragment fragment, String tag);
}
