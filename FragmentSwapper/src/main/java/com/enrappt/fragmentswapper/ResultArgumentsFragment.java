package com.enrappt.fragmentswapper;

import android.os.Bundle;

public interface ResultArgumentsFragment {

    /**
     * Called right after creating the fragment instance and before any of the lifecycle methods,
     * passes result arguments set via FragmentSwapper.swapBack
     *
     * @param resultArguments
     */
    public void setResultArguments(Bundle resultArguments);
}
