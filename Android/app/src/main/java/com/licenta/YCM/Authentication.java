package com.licenta.YCM;

import android.content.Intent;

public interface Authentication {
    void performAuth();
    void onActivityResult(int requestCode, int resultCode, Intent result);
}
