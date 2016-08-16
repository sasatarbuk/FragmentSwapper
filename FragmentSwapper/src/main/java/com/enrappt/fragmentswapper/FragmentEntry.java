package com.enrappt.fragmentswapper;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

public class FragmentEntry implements Parcelable {

    private Class<?> fragmentClass;
    private String tag;
    private Bundle arguments;
    private Fragment.SavedState savedState;

    public FragmentEntry(Class<?> fragmentClass, String tag, Bundle arguments, Fragment.SavedState savedState) {
        this.fragmentClass = fragmentClass;
        this.tag = tag;
        this.arguments = arguments;
        this.savedState = savedState;
    }

    public Class<?> getFragmentClass() {
        return fragmentClass;
    }

    public String getTag() {
        return tag;
    }

    public Bundle getArguments() {
        return arguments;
    }

    public Fragment.SavedState getSavedState() {
        return savedState;
    }

    @Override
    public String toString() {
        return "FragmentEntry{" +
                "fragmentClass=" + fragmentClass +
                ", tag='" + tag + '\'' +
                ", arguments=" + arguments +
                ", savedState=" + savedState +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.fragmentClass);
        dest.writeString(this.tag);
        dest.writeBundle(this.arguments);
        dest.writeParcelable(this.savedState, flags);
    }

    protected FragmentEntry(Parcel in) {
        this.fragmentClass = (Class<?>) in.readSerializable();
        this.tag = in.readString();
        this.arguments = in.readBundle(Bundle.class.getClassLoader());
        this.savedState = in.readParcelable(Fragment.SavedState.class.getClassLoader());
    }

    public static final Creator<FragmentEntry> CREATOR = new Creator<FragmentEntry>() {
        @Override
        public FragmentEntry createFromParcel(Parcel source) {
            return new FragmentEntry(source);
        }

        @Override
        public FragmentEntry[] newArray(int size) {
            return new FragmentEntry[size];
        }
    };
}
