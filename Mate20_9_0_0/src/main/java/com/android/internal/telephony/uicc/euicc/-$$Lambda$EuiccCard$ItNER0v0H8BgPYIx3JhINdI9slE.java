package com.android.internal.telephony.uicc.euicc;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$EuiccCard$ItNER0v0H8BgPYIx3JhINdI9slE implements ApduResponseHandler {
    public static final /* synthetic */ -$$Lambda$EuiccCard$ItNER0v0H8BgPYIx3JhINdI9slE INSTANCE = new -$$Lambda$EuiccCard$ItNER0v0H8BgPYIx3JhINdI9slE();

    private /* synthetic */ -$$Lambda$EuiccCard$ItNER0v0H8BgPYIx3JhINdI9slE() {
    }

    public final Object handleResult(byte[] bArr) {
        return EuiccCard.parseResponseAndCheckSimpleError(bArr, 4).toBytes();
    }
}
