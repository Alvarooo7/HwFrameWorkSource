package com.huawei.android.pushselfshow.richpush.html.a;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.richpush.html.a.a.a;
import com.huawei.android.pushselfshow.richpush.html.a.a.b;
import com.huawei.android.pushselfshow.richpush.html.api.NativeToJsMessageQueue;
import com.huawei.android.pushselfshow.richpush.html.api.d;
import org.json.JSONException;
import org.json.JSONObject;

public class i implements g {
    public boolean a = false;
    public boolean b = false;
    public long c = 2000;
    public int d = 0;
    public String e;
    public NativeToJsMessageQueue f;
    private a g;
    private b h;
    private LocationManager i;
    private Activity j = null;

    public i(Activity activity) {
        try {
            c.e("PushSelfShowLog", "init GeoBroker");
            this.j = activity;
        } catch (Object -l_2_R) {
            c.e("PushSelfShowLog", "init GeoBroker error ", -l_2_R);
        }
    }

    private void a() {
        d();
    }

    private void e() {
        Object -l_1_R;
        if (this.a) {
            if (this.g != null) {
                this.g.a(this.c, (float) this.d);
            }
            if (this.i != null) {
                -l_1_R = this.i.getLastKnownLocation("gps");
                if (-l_1_R != null) {
                    b(-l_1_R);
                    return;
                }
                return;
            }
            return;
        }
        if (this.h != null) {
            this.h.a(this.c, (float) this.d);
        }
        if (this.i != null) {
            -l_1_R = this.i.getLastKnownLocation("network");
            if (-l_1_R == null) {
                -l_1_R = this.i.getLastKnownLocation("gps");
            }
            if (-l_1_R != null) {
                b(-l_1_R);
            }
        }
    }

    public String a(String str, JSONObject jSONObject) {
        return null;
    }

    public JSONObject a(Location location) {
        Object obj = null;
        Object -l_2_R = new JSONObject();
        try {
            -l_2_R.put("latitude", location.getLatitude());
            -l_2_R.put("longitude", location.getLongitude());
            -l_2_R.put("altitude", !location.hasAltitude() ? null : Double.valueOf(location.getAltitude()));
            -l_2_R.put("accuracy", (double) location.getAccuracy());
            String str = "heading";
            if (location.hasBearing() && location.hasSpeed()) {
                obj = Float.valueOf(location.getBearing());
            }
            -l_2_R.put(str, obj);
            -l_2_R.put("velocity", (double) location.getSpeed());
            -l_2_R.put("timestamp", location.getTime());
        } catch (Object -l_3_R) {
            c.d("PushSelfShowLog", -l_3_R.toString());
        }
        return -l_2_R;
    }

    public void a(int i, int i2, Intent intent) {
    }

    public void a(NativeToJsMessageQueue nativeToJsMessageQueue, String str, String str2, JSONObject jSONObject) {
        try {
            c.e("PushSelfShowLog", "run into geo broker exec");
            d();
            this.i = (LocationManager) this.j.getSystemService("location");
            this.h = new b(this.i, this);
            this.g = new a(this.i, this);
            this.e = str2;
            if (nativeToJsMessageQueue != null) {
                this.f = nativeToJsMessageQueue;
                if ("getLocation".equals(str)) {
                    try {
                        if (jSONObject.has("useGps")) {
                            this.a = jSONObject.getBoolean("useGps");
                        }
                        if (jSONObject.has("keepLoc")) {
                            this.b = jSONObject.getBoolean("keepLoc");
                            if (this.b) {
                                if (jSONObject.has("minTime")) {
                                    this.c = jSONObject.getLong("minTime");
                                }
                                if (jSONObject.has("minDistance")) {
                                    this.d = jSONObject.getInt("minDistance");
                                }
                            }
                        }
                    } catch (JSONException e) {
                        a(d.a.JSON_EXCEPTION);
                    }
                    e();
                    return;
                }
                if ("clearWatch".equals(str)) {
                    c.e("PushSelfShowLog", "call method clearWatch");
                    a();
                } else {
                    a(d.a.METHOD_NOT_FOUND_EXCEPTION);
                }
                return;
            }
            c.a("PushSelfShowLog", "jsMessageQueue is null while run into App exec");
        } catch (Object -l_5_R) {
            c.e("PushSelfShowLog", "run into geo broker exec error ", -l_5_R);
        }
    }

    public void a(d.a aVar) {
        c.a("PushSelfShowLog", "geo broker fail ,reason is %s", d.c()[aVar.ordinal()]);
        if (this.f != null) {
            this.f.a(this.e, aVar, "error", null);
        }
    }

    public void b() {
    }

    public void b(Location location) {
        if (this.f != null) {
            this.f.a(this.e, d.a.OK, "success", a(location));
        }
    }

    public void c() {
        d();
    }

    public void d() {
        c.e("PushSelfShowLog", "call geo broker reset");
        try {
            if (this.h != null) {
                this.h.a();
                this.h = null;
            }
            if (this.g != null) {
                this.g.a();
                this.g = null;
            }
            this.i = null;
            this.a = false;
            this.b = false;
            this.c = 2000;
            this.d = 0;
            this.e = null;
            this.f = null;
        } catch (Object -l_1_R) {
            c.e("PushSelfShowLog", "call GeoBroker reset error", -l_1_R);
        }
    }
}
