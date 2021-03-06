package com.enrappt.fragmentswapper;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class FragmentSwapper implements Parcelable {

    /**
     * Activity this FragmentSwapper is associated with
     */
    private WeakReference<FragmentActivity> activity;

    /**
     * Current fragment's tag
     */
    private String currentTag;

    /**
     * Fragment history up to (not including) current fragment
     */
    private List<FragmentEntry> history = new ArrayList<>();

    /**
     * ID of the ViewGroup holding the fragment
     */
    private int containerId;

    /**
     * Transaction agent
     */
    private TransactionAgent transactionAgent = new DefaultTransactionAgent();

    /**
     * Constructor
     *
     * @param containerId ID of the view holding the fragment
     */
    public FragmentSwapper(FragmentActivity activity, int containerId, Fragment initFragment, String initTag) {
        this.activity = new WeakReference<>(activity);
        this.containerId = containerId;
        transactAdd(initFragment, initTag);
    }

    /**
     * Constructor
     *
     * @param containerId ID of the view holding the fragment
     * @param transactionAgent Transaction agent to handle swaps
     */
    public FragmentSwapper(FragmentActivity activity, int containerId, Fragment initFragment, String initTag, TransactionAgent transactionAgent) {
        this.activity = new WeakReference<>(activity);
        this.containerId = containerId;
        this.transactionAgent = transactionAgent;
        transactAdd(initFragment, initTag);
    }

    /**
     * Associate an activity with this FragmentSwapper
     *
     * @param activity Activity containing the fragment
     */
    public void setActivity(FragmentActivity activity) {
        this.activity = new WeakReference<>(activity);
    }

    /**
     * Get FragmentManager from the associated activity
     *
     * @return FragmentManager instance
     */
    public FragmentManager getFragmentManager() {
        if (activity.get() == null) {
            throw new FragmentSwapperException("No activity");
        }
        return activity.get().getSupportFragmentManager();
    }

    /**
     * Get currently attached fragment
     *
     * @return Fragment
     */
    public Fragment getCurrentFragment() {
        if (activity.get() == null) {
            throw new FragmentSwapperException("No activity");
        }
        return activity.get().getSupportFragmentManager().findFragmentById(containerId);
    }

    /**
     * Get tag of the currently attached fragment
     *
     * @return Tag identifying the current fragment
     */
    public String getCurrentTag() {
        return currentTag;
    }

    /**
     * Whether there are fragments in history
     *
     * @return true if there are fragments, false otherwise
     */
    public boolean hasHistory() {
        return history.size() > 0;
    }

    /**
     * Return history
     *
     * @return History
     */
    public List<FragmentEntry> getHistory() {
        return history;
    }

    /**
     * Add fragment entry to history
     *
     * @param entry
     */
    public void addHistory(FragmentEntry entry) {
        history.add(entry);
    }

    /**
     * Put current fragment onto stack and swap it with a new one
     *
     * @param newFragment Fragment to replace the current one
     * @param newTag Tag identifying the new fragment
     */
    public void swap(Fragment newFragment, String newTag) {

        // Save current fragment and put to history
        Fragment fragment = getCurrentFragment();
        String tag = getCurrentTag();
        Bundle arguments = fragment.getArguments();
        Fragment.SavedState state = getFragmentManager().saveFragmentInstanceState(fragment);
        history.add(new FragmentEntry(fragment.getClass(), tag, arguments, state));

        // Swap with the new fragment
        transactReplace(newFragment, newTag, true);
    }

    /**
     * Pop fragment entry from stack and swap
     */
    public void swapBack() {
        swapBack((Bundle) null);
    }

    /**
     * Pop fragment entry from stack and swap
     *
     * @param resultArguments Result arguments to pass to the fragment below if it implements the
     *                        ResultArgumentsFragment interface
     */
    public void swapBack(Bundle resultArguments) {
        if (!hasHistory()) {
            throw new FragmentSwapperException("No fragments in history");
        }
        FragmentEntry entry = history.get(history.size() - 1);
        history.remove(history.size() - 1);
        swapFragmentFromEntry(entry, resultArguments);
    }

    /**
     * Search for the supplied fragment entry in history identified by the supplied tag and
     * swap current fragment for the new one created with the found entry
     *
     * @param tag Tag to search
     */
    public void swapBack(String tag) {
        swapBack(tag, null);
    }

    /**
     * Search for the supplied fragment entry in history identified by the supplied tag and
     * swap current fragment for the new one created with the found entry
     *
     * @param tag Tag to search
     * @param resultArguments Result arguments to pass to the found fragment if it implements the
     *                        ResultArgumentsFragment interface
     */
    public void swapBack(String tag, Bundle resultArguments) {
        if (tag == null) {
            throw new FragmentSwapperException("Tag is null");
        }
        if (!hasHistory()) {
            throw new FragmentSwapperException("No fragments in history");
        }
        for (int i = history.size() - 1; i >= 0; i --) {
            FragmentEntry entry = history.get(i);
            if (entry.getTag() != null && entry.getTag().equals(tag)) {
                history = history.subList(0, i);
                swapFragmentFromEntry(entry, resultArguments);
                return;
            }
        }
        throw new FragmentSwapperException("Fragment with the supplied tag not found");
    }

    /**
     * Swap current fragment with the new one clearing entire history
     *
     * @param newFragment Fragment to replace the current one
     * @param newTag Tag identifying the new fragment
     */
    public void swapClearHistoryToBottom(Fragment newFragment, String newTag) {
        if (!hasHistory()) {
            throw new FragmentSwapperException("No fragments in history");
        }
        // Clear all of history
        history = new ArrayList<>();
        transactReplace(newFragment, newTag, true);
    }

    /**
     * Swap current fragment with the new one clearing entire history except the init fragment
     *
     * @param newFragment Fragment to replace the current one
     * @param newTag Tag identifying the new fragment
     */
    public void swapClearHistoryToInit(Fragment newFragment, String newTag) {
        if (!hasHistory()) {
            throw new FragmentSwapperException("No fragments in history");
        }
        history = history.subList(0, 1);
        transactReplace(newFragment, newTag, true);
    }

    /**
     * Swap current fragment with the new one without touching history
     *
     * @param newFragment
     * @param newTag
     */
    public void swapSkipHistory(Fragment newFragment, String newTag) {
        transactReplace(newFragment, newTag, true);
    }

    /**
     * Swap old fragment for the new one created with fragment entry
     *
     * @param fragmentEntry Fragment entry object
     * @param resultArguments Arguments to pass to the entry fragment if it implements the
     *                        ResultArgumentsFragment interface.
     */
    private void swapFragmentFromEntry(FragmentEntry fragmentEntry, Bundle resultArguments) {

        // Try to instantiate fragment
        Fragment fragment;
        try {
            fragment = (Fragment) fragmentEntry.getFragmentClass().newInstance();
        } catch (InstantiationException e) {
            throw new FragmentSwapperException("Error instantiating fragment", e);
        } catch (IllegalAccessException e) {
            throw new FragmentSwapperException("Error instantiating fragment", e);
        }

        // Restore fragment state
        fragment.setArguments(fragmentEntry.getArguments());
        fragment.setInitialSavedState(fragmentEntry.getSavedState());

        if (fragment instanceof ResultArgumentsFragment && resultArguments != null) {
            ((ResultArgumentsFragment) fragment).setResultArguments(resultArguments);
        }

        // Swap current with the fragment from entry
        transactReplace(fragment, fragmentEntry.getTag(), false);
    }

    /**
     * Set parameters and perform initial fragment transaction on the fragment manager
     *
     * @param newFragment Initial fragment
     * @param newTag Initial fragment tag
     */
    private void transactAdd(Fragment newFragment, String newTag) {
        currentTag = newTag;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transactionAgent.transactAdd(transaction, containerId, newFragment, newTag);
    }

    /**
     * Set parameters and perform replace fragment transaction on the fragment manager
     *
     * @param fragment
     * @param tag
     */
    private void transactReplace(Fragment fragment, String tag, boolean isForward) {
        currentTag = tag;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transactionAgent.transactReplace(transaction, containerId, fragment, tag, isForward);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.currentTag);
        dest.writeTypedList(this.history);
        dest.writeInt(this.containerId);
        dest.writeParcelable(this.transactionAgent, flags);
    }

    protected FragmentSwapper(Parcel in) {
        this.currentTag = in.readString();
        this.history = in.createTypedArrayList(FragmentEntry.CREATOR);
        this.containerId = in.readInt();
        this.transactionAgent = in.readParcelable(TransactionAgent.class.getClassLoader());
    }

    public static final Parcelable.Creator<FragmentSwapper> CREATOR = new Parcelable.Creator<FragmentSwapper>() {
        @Override
        public FragmentSwapper createFromParcel(Parcel source) {
            return new FragmentSwapper(source);
        }

        @Override
        public FragmentSwapper[] newArray(int size) {
            return new FragmentSwapper[size];
        }
    };
}
