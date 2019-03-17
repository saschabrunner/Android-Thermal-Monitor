package com.gitlab.saschabrunner.thermalmonitor.main.ui;

public interface FirstTimeSetupFragment {
    /**
     * Called before the view pager switches from this fragment to the next one.
     * Use this to validate data before allowing the user to move to the next step.
     *
     * @return true if the user is allowed to proceed, causing the next fragment to be shown,
     * false otherwise.
     */
    boolean onNextScreenRequested();
}
