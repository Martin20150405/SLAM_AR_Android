package com.martin.ads;

class Quaternion {
    public static final Quaternion IDENTITY = new Quaternion();
    private float x = 0.0F;
    private float y = 0.0F;
    private float z = 0.0F;
    private float w = 1.0F;

    public Quaternion() {
        this.setValues(0.0F, 0.0F, 0.0F, 1.0F);
    }

    public Quaternion(Quaternion other) {
        this.setValues(other.x, other.y, other.z, other.w);
    }

    public Quaternion(float x, float y, float z, float w) {
        this.setValues(x, y, z, w);
    }

    public Quaternion(float[] array) {
        this.setValues(array);
    }

    public void setValues(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public void setValues(float[] array) {
        this.setValues(array[0], array[1], array[2], array[3]);
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float z() {
        return this.z;
    }

    public float w() {
        return this.w;
    }

    public void getValues(float[] dest, int offset) {
        dest[offset + 0] = this.x;
        dest[offset + 1] = this.y;
        dest[offset + 2] = this.z;
        dest[offset + 3] = this.w;
    }

    public Quaternion inverse() {
        return new Quaternion(-this.x, -this.y, -this.z, this.w);
    }

    public Quaternion compose(Quaternion rhs) {
        Quaternion out = new Quaternion();
        multiplyQuaternions(this, rhs, out);
        return out;
    }

    public static Quaternion makeInterpolated(Quaternion a, Quaternion b, float t) {
        Quaternion out = new Quaternion();
        float cosHalfTheta = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
        if(cosHalfTheta < 0.0F) {
            b = new Quaternion(b);
            cosHalfTheta = -cosHalfTheta;
            b.x = -b.x;
            b.y = -b.y;
            b.z = -b.z;
            b.w = -b.w;
        }

        float halfTheta = (float)Math.acos((double)cosHalfTheta);
        float sinHalfTheta = (float)Math.sqrt((double)(1.0F - cosHalfTheta * cosHalfTheta));
        float ratioA;
        float ratioB;
        if((double)Math.abs(sinHalfTheta) > 0.001D) {
            float oneOverSinHalfTheta = 1.0F / sinHalfTheta;
            ratioA = (float)Math.sin((double)((1.0F - t) * halfTheta)) * oneOverSinHalfTheta;
            ratioB = (float)Math.sin((double)(t * halfTheta)) * oneOverSinHalfTheta;
        } else {
            ratioA = 1.0F - t;
            ratioB = t;
        }

        out.x = ratioA * a.x + ratioB * b.x;
        out.y = ratioA * a.y + ratioB * b.y;
        out.z = ratioA * a.z + ratioB * b.z;
        out.w = ratioA * a.w + ratioB * b.w;
        out.normalizeInPlace();
        return out;
    }

    private void normalizeInPlace() {
        float inverseNorm = (float)(1.0D / Math.sqrt((double)(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w)));
        this.x *= inverseNorm;
        this.y *= inverseNorm;
        this.z *= inverseNorm;
        this.w *= inverseNorm;
    }

    public void toMatrix(float[] dest, int offset, int stride) {
        float n = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
        float s = n > 0.0F?2.0F / n:0.0F;
        float xs = this.x * s;
        float ys = this.y * s;
        float zs = this.z * s;
        float wx = this.w * xs;
        float wy = this.w * ys;
        float wz = this.w * zs;
        float xx = this.x * xs;
        float xy = this.x * ys;
        float xz = this.x * zs;
        float yy = this.y * ys;
        float yz = this.y * zs;
        float zz = this.z * zs;
        dest[offset + 0 + stride * 0] = 1.0F - (yy + zz);
        dest[offset + 0 + stride * 1] = xy - wz;
        dest[offset + 0 + stride * 2] = xz + wy;
        dest[offset + 1 + stride * 0] = xy + wz;
        dest[offset + 1 + stride * 1] = 1.0F - (xx + zz);
        dest[offset + 1 + stride * 2] = yz - wx;
        dest[offset + 2 + stride * 0] = xz - wy;
        dest[offset + 2 + stride * 1] = yz + wx;
        dest[offset + 2 + stride * 2] = 1.0F - (xx + yy);
    }

    public void getTransformedAxis(int axis, float scale, float[] dest, int offset) {
        float[] vector = new float[]{0.0F, 0.0F, 0.0F};
        vector[axis] = scale;
        rotateVector(this, vector, 0, dest, offset);
    }

    public float[] transformedAxis(int axis, float scale) {
        float[] out = new float[3];
        this.getTransformedAxis(axis, scale, out, 0);
        return out;
    }

    public float[] xAxis() {
        return this.transformedAxis(0, 1.0F);
    }

    public float[] yAxis() {
        return this.transformedAxis(1, 1.0F);
    }

    public float[] zAxis() {
        return this.transformedAxis(2, 1.0F);
    }

    private static void multiplyQuaternions(Quaternion a, Quaternion b, Quaternion out) {
        out.x = a.x * b.w + a.y * b.z - a.z * b.y + a.w * b.x;
        out.y = -a.x * b.z + a.y * b.w + a.z * b.x + a.w * b.y;
        out.z = a.x * b.y - a.y * b.x + a.z * b.w + a.w * b.z;
        out.w = -a.x * b.x - a.y * b.y - a.z * b.z + a.w * b.w;
    }

    public static void rotateVector(Quaternion q, float[] v, int offsetIn, float[] out, int offsetOut) {
        float x = v[offsetIn + 0];
        float y = v[offsetIn + 1];
        float z = v[offsetIn + 2];
        float qx = q.x();
        float qy = q.y();
        float qz = q.z();
        float qw = q.w();
        float ix = qw * x + qy * z - qz * y;
        float iy = qw * y + qz * x - qx * z;
        float iz = qw * z + qx * y - qy * x;
        float iw = -qx * x - qy * y - qz * z;
        out[offsetOut + 0] = ix * qw + iw * -qx + iy * -qz - iz * -qy;
        out[offsetOut + 1] = iy * qw + iw * -qy + iz * -qx - ix * -qz;
        out[offsetOut + 2] = iz * qw + iw * -qz + ix * -qy - iy * -qx;
    }

    public static Quaternion fromMatrix(float[] m, int offset, int stride) {
        float m00 = m[offset + 0 + stride * 0];
        float m01 = m[offset + 0 + stride * 1];
        float m02 = m[offset + 0 + stride * 2];
        float m10 = m[offset + 1 + stride * 0];
        float m11 = m[offset + 1 + stride * 1];
        float m12 = m[offset + 1 + stride * 2];
        float m20 = m[offset + 2 + stride * 0];
        float m21 = m[offset + 2 + stride * 1];
        float m22 = m[offset + 2 + stride * 2];
        float w = 0.5F * (float)Math.sqrt((double)Math.max(0.0F, 1.0F + m00 + m11 + m22));
        float x = 0.5F * (float)Math.sqrt((double)Math.max(0.0F, 1.0F + m00 - m11 - m22));
        float y = 0.5F * (float)Math.sqrt((double)Math.max(0.0F, 1.0F - m00 + m11 - m22));
        float z = 0.5F * (float)Math.sqrt((double)Math.max(0.0F, 1.0F - m00 - m11 + m22));
        x = Math.copySign(x, m21 - m12);
        y = Math.copySign(y, m02 - m20);
        z = Math.copySign(z, m10 - m01);
        return new Quaternion(x, y, z, w);
    }

    public String toString() {
        return String.format("[%.3f, %.3f, %.3f, %.3f]", new Object[]{Float.valueOf(this.x), Float.valueOf(this.y), Float.valueOf(this.z), Float.valueOf(this.w)});
    }
}

