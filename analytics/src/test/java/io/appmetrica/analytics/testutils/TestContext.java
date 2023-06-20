package io.appmetrica.analytics.testutils;

import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestContext extends ContextWrapper {

    private Set<String> mSelfPermissions = new HashSet<String>();
    private Set<String> mCallingPermissions = new HashSet<String>();
    private Map<String, Object> mSystemServices = new HashMap<String, Object>();

    public TestContext() {
        super(null);
    }

    public void addSelfPermissions(String... permissions) {
        addToSet(mSelfPermissions, permissions);
    }

    public void addCallingPermissions(String permissions) {
        addToSet(mCallingPermissions, permissions);
    }

    public void addSystemService(String name, Object service) {
        mSystemServices.put(name, service);
    }

    private void addToSet(Set<String> target, String... addedValues) {
        for (String addedValue : addedValues) {
            target.add(addedValue);
        }
    }

    @Override
    public int checkCallingPermission(String permission) {
        return mCallingPermissions.contains(permission) ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
    }

    @Override
    public int checkCallingOrSelfPermission(String permission) {
        if (checkCallingPermission(permission) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return PackageManager.PERMISSION_GRANTED;
        }
        return PackageManager.PERMISSION_DENIED;
    }

    @Override
    public int checkSelfPermission(String permission) {
        return mSelfPermissions.contains(permission) ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
    }

    @Override
    public Object getSystemService(String name) {
        return mSystemServices.get(name);
    }
}
