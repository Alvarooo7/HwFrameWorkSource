package com.android.server.print;

import java.util.List;
import java.util.function.BiConsumer;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$RemotePrintService$jrFOjxtIoMNm8S0KNTqIDHuv4oY implements BiConsumer {
    public static final /* synthetic */ -$$Lambda$RemotePrintService$jrFOjxtIoMNm8S0KNTqIDHuv4oY INSTANCE = new -$$Lambda$RemotePrintService$jrFOjxtIoMNm8S0KNTqIDHuv4oY();

    private /* synthetic */ -$$Lambda$RemotePrintService$jrFOjxtIoMNm8S0KNTqIDHuv4oY() {
    }

    public final void accept(Object obj, Object obj2) {
        ((RemotePrintService) obj).handleStartPrinterDiscovery((List) obj2);
    }
}
