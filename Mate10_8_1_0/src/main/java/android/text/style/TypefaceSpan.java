package android.text.style;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;

public class TypefaceSpan extends MetricAffectingSpan implements ParcelableSpan {
    private final String mFamily;

    private static void apply(android.graphics.Paint r1, java.lang.String r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.style.TypefaceSpan.apply(android.graphics.Paint, java.lang.String):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:31)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:56)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:296)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.style.TypefaceSpan.apply(android.graphics.Paint, java.lang.String):void");
    }

    public TypefaceSpan(String family) {
        this.mFamily = family;
    }

    public TypefaceSpan(Parcel src) {
        this.mFamily = src.readString();
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
        return 13;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeString(this.mFamily);
    }

    public String getFamily() {
        return this.mFamily;
    }

    public void updateDrawState(TextPaint ds) {
        apply(ds, this.mFamily);
    }

    public void updateMeasureState(TextPaint paint) {
        apply(paint, this.mFamily);
    }
}
