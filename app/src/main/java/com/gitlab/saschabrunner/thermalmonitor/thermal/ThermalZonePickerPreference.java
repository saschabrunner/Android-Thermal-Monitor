package com.gitlab.saschabrunner.thermalmonitor.thermal;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.gitlab.saschabrunner.thermalmonitor.R;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

public class ThermalZonePickerPreference extends DialogPreference {
    private Set<String> values = new HashSet<>();

    public ThermalZonePickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ThermalZonePickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ThermalZonePickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.dialogPreferenceStyle);
    }

    public ThermalZonePickerPreference(Context context) {
        this(context, null);
    }

    /**
     * Get the current value of the preference.
     *
     * @return Selected zone IDs as strings.
     */
    public Set<String> getValues() {
        return Collections.unmodifiableSet(values);
    }

    /**
     * Set a new value for the preference.
     *
     * @param values Selected zone IDs as strings.
     */
    public void setValues(Set<String> values) {
        this.values.clear();
        this.values.addAll(values);

        persistStringSet(this.values);
        notifyChanged();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        final CharSequence[] defaultValues = a.getTextArray(index);
        final Set<String> result = new HashSet<>();

        for (final CharSequence defaultValue : defaultValues) {
            result.add(defaultValue.toString());
        }

        return result;
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        // noinspection unchecked
        setValues(getPersistedStringSet((Set<String>) defaultValue));
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.dialog_thermal_zone_picker;
    }
}
