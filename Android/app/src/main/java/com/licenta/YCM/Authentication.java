package com.licenta.YCM;

import android.content.Intent;
import android.content.res.Configuration;

public interface Authentication {
    void performAuth();
    void onActivityResult(int requestCode, int resultCode, Intent result);
    void onConfigurationChanged(Configuration newConfig);
}
