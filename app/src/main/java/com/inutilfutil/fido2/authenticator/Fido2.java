package com.inutilfutil.fido2.authenticator;

public class Fido2 {
    static {
        System.loadLibrary("solo-android");
    }

    public static native void setDevice(Fido2Device soloDevice);
}
