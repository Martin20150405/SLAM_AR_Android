//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.calib3d;



// C++: class UsacParams

public class UsacParams {

    protected final long nativeObj;
    protected UsacParams(long addr) { nativeObj = addr; }

    public long getNativeObjAddr() { return nativeObj; }

    // internal usage only
    public static UsacParams __fromPtr__(long addr) { return new UsacParams(addr); }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // native support for java finalize()
    private static native void delete(long nativeObj);

}
