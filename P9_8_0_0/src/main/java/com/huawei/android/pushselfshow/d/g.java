package com.huawei.android.pushselfshow.d;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.c.a;
import com.huawei.android.pushselfshow.richpush.tools.b;
import com.huawei.android.pushselfshow.richpush.tools.d;

public class g extends Thread {
    private Context a;
    private a b;

    public g(Context context, a aVar) {
        this.a = context;
        this.b = aVar;
    }

    private static Intent b(Context context, a aVar) {
        if (aVar == null) {
            return null;
        }
        Object -l_2_R = com.huawei.android.pushselfshow.utils.a.b(context, aVar.u());
        Intent -l_3_R;
        Intent -l_2_R2;
        if (aVar.g() == null) {
            if (aVar.v() != null) {
                -l_3_R = new Intent(aVar.v());
                if (com.huawei.android.pushselfshow.utils.a.a(context, aVar.u(), -l_3_R).booleanValue()) {
                    -l_2_R2 = -l_3_R;
                }
            }
            -l_2_R.setPackage(aVar.u());
        } else {
            try {
                -l_3_R = Intent.parseUri(aVar.g(), 0);
                c.a("PushSelfShowLog", "Intent.parseUri(msg.intentUri, 0)，" + -l_3_R.toURI());
                if (com.huawei.android.pushselfshow.utils.a.a(context, aVar.u(), -l_3_R).booleanValue()) {
                    -l_2_R2 = -l_3_R;
                }
            } catch (Throwable -l_3_R2) {
                c.a("PushSelfShowLog", "intentUri error ", -l_3_R2);
            }
        }
        return -l_2_R;
    }

    public boolean a(Context context) {
        if ("cosa".equals(this.b.m())) {
            return b(context);
        }
        if ("email".equals(this.b.m())) {
            return c(context);
        }
        return !"rp".equals(this.b.m()) ? true : d(context);
    }

    public boolean a(Context context, a aVar) {
        int -l_3_I = 0;
        if (!"cosa".equals(aVar.m())) {
            return false;
        }
        Intent -l_4_R = b(context, aVar);
        if (-l_4_R == null) {
            c.a("PushSelfShowLog", "launchCosaApp,intent == null");
            -l_3_I = 1;
        }
        if (com.huawei.android.pushselfshow.utils.a.a(context, -l_4_R)) {
            return -l_3_I;
        }
        c.b("PushSelfShowLog", "no permission to start activity");
        return true;
    }

    public boolean b(Context context) {
        if (com.huawei.android.pushselfshow.utils.a.c(context, this.b.u())) {
            return true;
        }
        com.huawei.android.pushselfshow.utils.a.a(context, "4", this.b, -1);
        return false;
    }

    public boolean c(Context context) {
        if (com.huawei.android.pushselfshow.utils.a.d(context)) {
            return true;
        }
        com.huawei.android.pushselfshow.utils.a.a(context, "15", this.b, -1);
        return false;
    }

    public boolean d(Context context) {
        if (this.b.x() == null || this.b.x().length() == 0) {
            com.huawei.android.pushselfshow.utils.a.a(context, "6", this.b, -1);
            c.a("PushSelfShowLog", "ilegle richpush param ,rpl is null");
            return false;
        }
        c.a("PushSelfShowLog", "enter checkRichPush, rpl is " + this.b.x() + ",psMsg.rpct:" + this.b.z());
        if ("application/zip".equals(this.b.z()) || this.b.x().endsWith(".zip")) {
            this.b.f("application/zip");
            if (this.b.h() == 1) {
                String -l_4_R = new d().a(context, this.b.x(), this.b.i(), b.a("application/zip"));
                if (-l_4_R != null && -l_4_R.length() > 0) {
                    this.b.d(-l_4_R);
                    this.b.f("application/zip_local");
                }
                c.a("PushSelfShowLog", "Download first ,the localfile" + -l_4_R);
            }
            return true;
        }
        if ("text/html".equals(this.b.z()) || this.b.x().endsWith(".html")) {
            this.b.f("text/html");
            return true;
        }
        c.a("PushSelfShowLog", "unknow rpl type");
        com.huawei.android.pushselfshow.utils.a.a(context, "6", this.b, -1);
        return false;
    }

    public void run() {
        c.a("PushSelfShowLog", "enter run()");
        try {
            if (a(this.a)) {
                if (a(this.a, this.b)) {
                    com.huawei.android.pushselfshow.utils.a.a(this.a, "17", this.b, -1);
                    return;
                }
                d.a(this.a, this.b);
            }
        } catch (Object -l_1_R) {
            c.d("PushSelfShowLog", -l_1_R.toString());
        }
    }
}
