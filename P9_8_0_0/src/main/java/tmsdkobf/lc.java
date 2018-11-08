package tmsdkobf;

import android.text.TextUtils;
import com.qq.taf.jce.JceStruct;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class lc {
    public static void ep() {
        try {
            kz.q(System.currentTimeMillis() / 1000);
            Object -l_0_R = er();
            if (!-l_0_R.isEmpty()) {
                Object -l_2_R;
                Object -l_1_R = la.bD(getPath());
                int -l_3_I = 0;
                if (-l_1_R.size() <= 0) {
                    -l_3_I = 1;
                    -l_2_R = new JSONArray();
                } else {
                    -l_2_R = new JSONArray((String) -l_1_R.get(0));
                }
                int -l_4_I = 0;
                int -l_5_I = la.em();
                if ((kz.eh() > 0 ? 1 : null) == null) {
                    -l_4_I = 1;
                }
                for (Integer -l_7_R : -l_0_R.keySet()) {
                    int -l_8_I = -l_7_R.intValue();
                    long -l_9_J = ((Long) -l_0_R.get(-l_7_R)).longValue();
                    int -l_11_I = 0;
                    if (-l_3_I == 0) {
                        -l_11_I = 0;
                        while (-l_11_I < -l_2_R.length()) {
                            JSONObject -l_12_R = (JSONObject) -l_2_R.get(-l_11_I);
                            if (-l_12_R.getInt("uid") != -l_8_I) {
                                -l_11_I++;
                            } else {
                                long -l_14_J = -l_12_R.getLong("total_traffic");
                                long -l_16_J = -l_12_R.getLong("curr_traffic");
                                -l_12_R.put("total_traffic", -l_9_J);
                                if (-l_5_I == 0) {
                                    -l_12_R.put("curr_traffic", (-l_16_J + -l_9_J) - -l_14_J);
                                } else {
                                    -l_12_R.put("curr_traffic", -l_16_J + -l_9_J);
                                }
                            }
                        }
                    }
                    if (-l_3_I != 0 || -l_11_I >= -l_2_R.length()) {
                        Object -l_12_R2 = new JSONObject();
                        -l_12_R2.put("uid", -l_8_I);
                        -l_12_R2.put("total_traffic", -l_9_J);
                        if (-l_4_I == 0) {
                            -l_12_R2.put("curr_traffic", -l_9_J);
                        } else {
                            -l_12_R2.put("curr_traffic", 0);
                        }
                        -l_2_R.put(-l_12_R2);
                    }
                }
                if (-l_2_R.length() > 0) {
                    la.a(-l_2_R.toString(), getPath(), SmsCheckResult.ESCT_163);
                }
            }
        } catch (Throwable th) {
        }
    }

    public static void eq() {
        try {
            ep();
            Object -l_1_R = la.bD(getPath());
            if (-l_1_R == null || -l_1_R.isEmpty()) {
                la.b(SmsCheckResult.ESCT_163, 1002, "");
                return;
            }
            JSONArray jSONArray = new JSONArray((String) -l_1_R.get(0));
            JSONArray -l_3_R = new JSONArray();
            if (jSONArray.length() > 0) {
                Object -l_5_R = TMServiceFactory.getSystemInfoService().f(1, 2);
                if (-l_5_R != null && -l_5_R.size() > 0) {
                    JceStruct -l_6_R = new ao(SmsCheckResult.ESCT_163, new ArrayList());
                    for (int -l_7_I = 0; -l_7_I < jSONArray.length(); -l_7_I++) {
                        try {
                            JSONObject -l_8_R = (JSONObject) jSONArray.get(-l_7_I);
                            int -l_9_I = -l_8_R.getInt("uid");
                            long -l_10_J = -l_8_R.getLong("curr_traffic");
                            Object -l_12_R = "";
                            int -l_13_I = 0;
                            Object -l_14_R = -l_5_R.iterator();
                            while (-l_14_R.hasNext()) {
                                ov -l_15_R = (ov) -l_14_R.next();
                                if (-l_15_R.getUid() == -l_9_I) {
                                    -l_12_R = -l_15_R.getPackageName();
                                    -l_13_I = -l_15_R.hx();
                                    break;
                                }
                            }
                            if (!TextUtils.isEmpty(-l_12_R)) {
                                -l_8_R.put("curr_traffic", 0);
                                if ((-l_10_J > 0 ? 1 : null) != null) {
                                    -l_3_R.put(-l_8_R);
                                    -l_14_R = new ap(new HashMap());
                                    -l_14_R.bG.put(Integer.valueOf(8), String.valueOf(System.currentTimeMillis()));
                                    -l_14_R.bG.put(Integer.valueOf(9), String.valueOf(-l_10_J));
                                    -l_14_R.bG.put(Integer.valueOf(10), -l_12_R);
                                    -l_14_R.bG.put(Integer.valueOf(11), -l_13_I == 0 ? "0" : "1");
                                    -l_6_R.bD.add(-l_14_R);
                                }
                            }
                        } catch (Throwable th) {
                        }
                    }
                    Object -l_7_R = im.bK();
                    if (-l_6_R.bD.size() > 0 && -l_7_R != null) {
                        jSONArray = -l_3_R;
                        -l_7_R.a(4060, -l_6_R, null, 0, new jy() {
                            public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                                if (i3 == 0 && i4 == 0) {
                                    la.a(jSONArray.toString(), lc.getPath(), SmsCheckResult.ESCT_163);
                                    kz.r(System.currentTimeMillis() / 1000);
                                }
                            }
                        });
                    }
                    return;
                }
                la.b(SmsCheckResult.ESCT_163, 1001, "");
                return;
            }
            la.b(SmsCheckResult.ESCT_163, 1002, "");
        } catch (Throwable th2) {
        }
    }

    private static java.util.Map<java.lang.Integer, java.lang.Long> er() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:195:0x028f
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:248)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:56)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:296)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:199)
*/
        /*
        r4 = new java.util.HashMap;
        r4.<init>();
        r12 = 0;
        r14 = 0;
        r17 = 0;
        r13 = new java.io.FileReader;	 Catch:{ Throwable -> 0x00e6, all -> 0x00fa }
        r29 = "/proc/net/xt_qtaguid/stats";	 Catch:{ Throwable -> 0x00e6, all -> 0x00fa }
        r0 = r29;	 Catch:{ Throwable -> 0x00e6, all -> 0x00fa }
        r13.<init>(r0);	 Catch:{ Throwable -> 0x00e6, all -> 0x00fa }
        r15 = new java.io.BufferedReader;	 Catch:{ Throwable -> 0x02b7, all -> 0x02aa }
        r15.<init>(r13);	 Catch:{ Throwable -> 0x02bb, all -> 0x02ae }
    L_0x0018:
        r17 = r15.readLine();	 Catch:{ Throwable -> 0x02bf, all -> 0x02b2 }
        if (r17 != 0) goto L_0x0055;
    L_0x001e:
        if (r13 != 0) goto L_0x00d9;
    L_0x0020:
        if (r15 != 0) goto L_0x00e1;
    L_0x0022:
        r14 = r15;
        r12 = r13;
    L_0x0024:
        r29 = tmsdk.common.TMSDKContext.getApplicaionContext();	 Catch:{ Throwable -> 0x0202 }
        r29 = r29.getApplicationInfo();	 Catch:{ Throwable -> 0x0202 }
        r0 = r29;	 Catch:{ Throwable -> 0x0202 }
        r0 = r0.uid;	 Catch:{ Throwable -> 0x0202 }
        r16 = r0;	 Catch:{ Throwable -> 0x0202 }
        r19 = 0;	 Catch:{ Throwable -> 0x0202 }
        r29 = r4.size();	 Catch:{ Throwable -> 0x0202 }
        if (r29 == 0) goto L_0x010a;	 Catch:{ Throwable -> 0x0202 }
    L_0x003a:
        r29 = r4.size();	 Catch:{ Throwable -> 0x0202 }
        r30 = 1;	 Catch:{ Throwable -> 0x0202 }
        r0 = r29;	 Catch:{ Throwable -> 0x0202 }
        r1 = r30;	 Catch:{ Throwable -> 0x0202 }
        if (r0 == r1) goto L_0x010e;	 Catch:{ Throwable -> 0x0202 }
    L_0x0046:
        if (r19 != 0) goto L_0x012f;	 Catch:{ Throwable -> 0x0202 }
    L_0x0048:
        r29 = r4.size();	 Catch:{ Throwable -> 0x0202 }
        r30 = 1;
        r0 = r29;
        r1 = r30;
        if (r0 == r1) goto L_0x0268;
    L_0x0054:
        return r4;
    L_0x0055:
        r29 = r17.trim();	 Catch:{ Throwable -> 0x00c0 }
        r30 = "[:\\s]+";	 Catch:{ Throwable -> 0x00c0 }
        r20 = r29.split(r30);	 Catch:{ Throwable -> 0x00c0 }
        r29 = 4;	 Catch:{ Throwable -> 0x00c0 }
        r29 = r20[r29];	 Catch:{ Throwable -> 0x00c0 }
        r29 = java.lang.Integer.parseInt(r29);	 Catch:{ Throwable -> 0x00c0 }
        r30 = 1;	 Catch:{ Throwable -> 0x00c0 }
        r0 = r29;	 Catch:{ Throwable -> 0x00c0 }
        r1 = r30;	 Catch:{ Throwable -> 0x00c0 }
        if (r0 != r1) goto L_0x0018;	 Catch:{ Throwable -> 0x00c0 }
    L_0x0070:
        r29 = 3;	 Catch:{ Throwable -> 0x00c0 }
        r29 = r20[r29];	 Catch:{ Throwable -> 0x00c0 }
        r21 = java.lang.Integer.parseInt(r29);	 Catch:{ Throwable -> 0x00c0 }
        r29 = 100000; // 0x186a0 float:1.4013E-40 double:4.94066E-319;	 Catch:{ Throwable -> 0x00c0 }
        r29 = r21 % r29;	 Catch:{ Throwable -> 0x00c0 }
        r30 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;	 Catch:{ Throwable -> 0x00c0 }
        r0 = r29;	 Catch:{ Throwable -> 0x00c0 }
        r1 = r30;	 Catch:{ Throwable -> 0x00c0 }
        if (r0 == r1) goto L_0x0018;	 Catch:{ Throwable -> 0x00c0 }
    L_0x0085:
        if (r21 == 0) goto L_0x0018;	 Catch:{ Throwable -> 0x00c0 }
    L_0x0087:
        r29 = 5;	 Catch:{ Throwable -> 0x00c0 }
        r29 = r20[r29];	 Catch:{ Throwable -> 0x00c0 }
        r30 = java.lang.Long.parseLong(r29);	 Catch:{ Throwable -> 0x00c0 }
        r29 = 7;	 Catch:{ Throwable -> 0x00c0 }
        r29 = r20[r29];	 Catch:{ Throwable -> 0x00c0 }
        r32 = java.lang.Long.parseLong(r29);	 Catch:{ Throwable -> 0x00c0 }
        r24 = r30 + r32;	 Catch:{ Throwable -> 0x00c0 }
        r29 = java.lang.Integer.valueOf(r21);	 Catch:{ Throwable -> 0x00c0 }
        r0 = r29;	 Catch:{ Throwable -> 0x00c0 }
        r29 = r4.get(r0);	 Catch:{ Throwable -> 0x00c0 }
        if (r29 != 0) goto L_0x00c3;	 Catch:{ Throwable -> 0x00c0 }
    L_0x00a5:
        r30 = 0;	 Catch:{ Throwable -> 0x00c0 }
        r29 = (r24 > r30 ? 1 : (r24 == r30 ? 0 : -1));	 Catch:{ Throwable -> 0x00c0 }
        if (r29 > 0) goto L_0x00d6;	 Catch:{ Throwable -> 0x00c0 }
    L_0x00ab:
        r29 = 1;	 Catch:{ Throwable -> 0x00c0 }
    L_0x00ad:
        if (r29 != 0) goto L_0x0018;	 Catch:{ Throwable -> 0x00c0 }
    L_0x00af:
        r29 = java.lang.Integer.valueOf(r21);	 Catch:{ Throwable -> 0x00c0 }
        r30 = java.lang.Long.valueOf(r24);	 Catch:{ Throwable -> 0x00c0 }
        r0 = r29;	 Catch:{ Throwable -> 0x00c0 }
        r1 = r30;	 Catch:{ Throwable -> 0x00c0 }
        r4.put(r0, r1);	 Catch:{ Throwable -> 0x00c0 }
        goto L_0x0018;	 Catch:{ Throwable -> 0x00c0 }
    L_0x00c0:
        r20 = move-exception;	 Catch:{ Throwable -> 0x00c0 }
        goto L_0x0018;	 Catch:{ Throwable -> 0x00c0 }
    L_0x00c3:
        r29 = java.lang.Integer.valueOf(r21);	 Catch:{ Throwable -> 0x00c0 }
        r0 = r29;	 Catch:{ Throwable -> 0x00c0 }
        r29 = r4.get(r0);	 Catch:{ Throwable -> 0x00c0 }
        r29 = (java.lang.Long) r29;	 Catch:{ Throwable -> 0x00c0 }
        r30 = r29.longValue();	 Catch:{ Throwable -> 0x00c0 }
        r24 = r24 + r30;
        goto L_0x00a5;
    L_0x00d6:
        r29 = 0;
        goto L_0x00ad;
    L_0x00d9:
        r13.close();	 Catch:{ Throwable -> 0x00de }
        goto L_0x0020;	 Catch:{ Throwable -> 0x00de }
    L_0x00de:
        r17 = move-exception;	 Catch:{ Throwable -> 0x00de }
        goto L_0x0022;	 Catch:{ Throwable -> 0x00de }
    L_0x00e1:
        r15.close();	 Catch:{ Throwable -> 0x00de }
        goto L_0x0022;
    L_0x00e6:
        r18 = move-exception;
    L_0x00e7:
        if (r12 != 0) goto L_0x00ef;
    L_0x00e9:
        if (r14 != 0) goto L_0x00f6;
    L_0x00eb:
        r17 = r18;
        goto L_0x0024;
    L_0x00ef:
        r12.close();	 Catch:{ Throwable -> 0x00f3 }
        goto L_0x00e9;	 Catch:{ Throwable -> 0x00f3 }
    L_0x00f3:
        r17 = move-exception;	 Catch:{ Throwable -> 0x00f3 }
        goto L_0x0024;	 Catch:{ Throwable -> 0x00f3 }
    L_0x00f6:
        r14.close();	 Catch:{ Throwable -> 0x00f3 }
        goto L_0x00eb;
    L_0x00fa:
        r27 = move-exception;
    L_0x00fb:
        if (r12 != 0) goto L_0x0100;
    L_0x00fd:
        if (r14 != 0) goto L_0x0106;
    L_0x00ff:
        throw r27;
    L_0x0100:
        r12.close();	 Catch:{ Throwable -> 0x0104 }
        goto L_0x00fd;	 Catch:{ Throwable -> 0x0104 }
    L_0x0104:
        r29 = move-exception;	 Catch:{ Throwable -> 0x0104 }
        goto L_0x00ff;	 Catch:{ Throwable -> 0x0104 }
    L_0x0106:
        r14.close();	 Catch:{ Throwable -> 0x0104 }
        goto L_0x00ff;
    L_0x010a:
        r19 = 1;
        goto L_0x0046;
    L_0x010e:
        r29 = r4.keySet();	 Catch:{ Throwable -> 0x0202 }
        r22 = r29.iterator();	 Catch:{ Throwable -> 0x0202 }
    L_0x0116:
        r29 = r22.hasNext();	 Catch:{ Throwable -> 0x0202 }
        if (r29 == 0) goto L_0x0046;	 Catch:{ Throwable -> 0x0202 }
    L_0x011c:
        r23 = r22.next();	 Catch:{ Throwable -> 0x0202 }
        r23 = (java.lang.Integer) r23;	 Catch:{ Throwable -> 0x0202 }
        r29 = r23.intValue();	 Catch:{ Throwable -> 0x0202 }
        r0 = r29;	 Catch:{ Throwable -> 0x0202 }
        r1 = r16;	 Catch:{ Throwable -> 0x0202 }
        if (r0 != r1) goto L_0x0116;	 Catch:{ Throwable -> 0x0202 }
    L_0x012c:
        r19 = 1;	 Catch:{ Throwable -> 0x0202 }
        goto L_0x0116;	 Catch:{ Throwable -> 0x0202 }
    L_0x012f:
        r22 = tmsdk.common.TMServiceFactory.getSystemInfoService();	 Catch:{ Throwable -> 0x0202 }
        r29 = 1;	 Catch:{ Throwable -> 0x0202 }
        r30 = 2;	 Catch:{ Throwable -> 0x0202 }
        r0 = r22;	 Catch:{ Throwable -> 0x0202 }
        r1 = r29;	 Catch:{ Throwable -> 0x0202 }
        r2 = r30;	 Catch:{ Throwable -> 0x0202 }
        r23 = r0.f(r1, r2);	 Catch:{ Throwable -> 0x0202 }
        r26 = r23.iterator();	 Catch:{ Throwable -> 0x0202 }
        r15 = r14;
        r13 = r12;
    L_0x0147:
        r29 = r26.hasNext();	 Catch:{ Throwable -> 0x028a }
        if (r29 != 0) goto L_0x0151;	 Catch:{ Throwable -> 0x028a }
    L_0x014d:
        r14 = r15;	 Catch:{ Throwable -> 0x028a }
        r12 = r13;	 Catch:{ Throwable -> 0x028a }
        goto L_0x0048;	 Catch:{ Throwable -> 0x028a }
    L_0x0151:
        r27 = r26.next();	 Catch:{ Throwable -> 0x028a }
        r27 = (tmsdkobf.ov) r27;	 Catch:{ Throwable -> 0x028a }
        r28 = r27.getUid();	 Catch:{ Throwable -> 0x028a }
        r29 = 100000; // 0x186a0 float:1.4013E-40 double:4.94066E-319;	 Catch:{ Throwable -> 0x028a }
        r29 = r28 % r29;	 Catch:{ Throwable -> 0x028a }
        r30 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;	 Catch:{ Throwable -> 0x028a }
        r0 = r29;	 Catch:{ Throwable -> 0x028a }
        r1 = r30;	 Catch:{ Throwable -> 0x028a }
        if (r0 == r1) goto L_0x0147;	 Catch:{ Throwable -> 0x028a }
    L_0x0168:
        if (r28 == 0) goto L_0x0147;	 Catch:{ Throwable -> 0x028a }
    L_0x016a:
        r6 = 0;	 Catch:{ Throwable -> 0x028a }
        r29 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x028a }
        r29.<init>();	 Catch:{ Throwable -> 0x028a }
        r30 = "/proc/uid_stat/";	 Catch:{ Throwable -> 0x028a }
        r29 = r29.append(r30);	 Catch:{ Throwable -> 0x028a }
        r0 = r29;	 Catch:{ Throwable -> 0x028a }
        r1 = r28;	 Catch:{ Throwable -> 0x028a }
        r29 = r0.append(r1);	 Catch:{ Throwable -> 0x028a }
        r30 = "/tcp_snd";	 Catch:{ Throwable -> 0x028a }
        r29 = r29.append(r30);	 Catch:{ Throwable -> 0x028a }
        r5 = r29.toString();	 Catch:{ Throwable -> 0x028a }
        r8 = 0;
        r12 = new java.io.FileReader;	 Catch:{ Throwable -> 0x0205, all -> 0x021c }
        r12.<init>(r5);	 Catch:{ Throwable -> 0x0205, all -> 0x021c }
        r14 = new java.io.BufferedReader;	 Catch:{ Throwable -> 0x02a3, all -> 0x029d }
        r14.<init>(r12);	 Catch:{ Throwable -> 0x02a3, all -> 0x029d }
        r8 = r14.readLine();	 Catch:{ Throwable -> 0x02a7, all -> 0x02a0 }
        if (r8 != 0) goto L_0x01f3;
    L_0x019c:
        if (r12 != 0) goto L_0x01f8;
    L_0x019e:
        if (r14 != 0) goto L_0x01fe;
    L_0x01a0:
        r15 = r14;
        r13 = r12;
    L_0x01a2:
        r29 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x028a }
        r29.<init>();	 Catch:{ Throwable -> 0x028a }
        r30 = "/proc/uid_stat/";	 Catch:{ Throwable -> 0x028a }
        r29 = r29.append(r30);	 Catch:{ Throwable -> 0x028a }
        r0 = r29;	 Catch:{ Throwable -> 0x028a }
        r1 = r28;	 Catch:{ Throwable -> 0x028a }
        r29 = r0.append(r1);	 Catch:{ Throwable -> 0x028a }
        r30 = "/tcp_rcv";	 Catch:{ Throwable -> 0x028a }
        r29 = r29.append(r30);	 Catch:{ Throwable -> 0x028a }
        r5 = r29.toString();	 Catch:{ Throwable -> 0x028a }
        r8 = 0;
        r12 = new java.io.FileReader;	 Catch:{ Throwable -> 0x023f, all -> 0x0252 }
        r12.<init>(r5);	 Catch:{ Throwable -> 0x023f, all -> 0x0252 }
        r14 = new java.io.BufferedReader;	 Catch:{ Throwable -> 0x0298, all -> 0x0293 }
        r14.<init>(r12);	 Catch:{ Throwable -> 0x0298, all -> 0x0293 }
        r8 = r14.readLine();	 Catch:{ Throwable -> 0x029b, all -> 0x0296 }
        if (r8 != 0) goto L_0x022e;
    L_0x01d2:
        if (r12 != 0) goto L_0x0235;
    L_0x01d4:
        if (r14 != 0) goto L_0x023b;
    L_0x01d6:
        r30 = 0;
        r29 = (r6 > r30 ? 1 : (r6 == r30 ? 0 : -1));
        if (r29 > 0) goto L_0x0264;
    L_0x01dc:
        r29 = 1;
    L_0x01de:
        if (r29 != 0) goto L_0x01ef;
    L_0x01e0:
        r29 = java.lang.Integer.valueOf(r28);	 Catch:{ Throwable -> 0x0202 }
        r30 = java.lang.Long.valueOf(r6);	 Catch:{ Throwable -> 0x0202 }
        r0 = r29;	 Catch:{ Throwable -> 0x0202 }
        r1 = r30;	 Catch:{ Throwable -> 0x0202 }
        r4.put(r0, r1);	 Catch:{ Throwable -> 0x0202 }
    L_0x01ef:
        r15 = r14;
        r13 = r12;
        goto L_0x0147;
    L_0x01f3:
        r6 = java.lang.Long.parseLong(r8);	 Catch:{ Throwable -> 0x02a7, all -> 0x02a0 }
        goto L_0x019c;
    L_0x01f8:
        r12.close();	 Catch:{ Throwable -> 0x01fc }
        goto L_0x019e;	 Catch:{ Throwable -> 0x01fc }
    L_0x01fc:
        r8 = move-exception;	 Catch:{ Throwable -> 0x01fc }
        goto L_0x01a0;	 Catch:{ Throwable -> 0x01fc }
    L_0x01fe:
        r14.close();	 Catch:{ Throwable -> 0x01fc }
        goto L_0x01a0;
    L_0x0202:
        r29 = move-exception;
        goto L_0x0054;
    L_0x0205:
        r9 = move-exception;
        r14 = r15;
        r12 = r13;
    L_0x0208:
        if (r12 != 0) goto L_0x0210;
    L_0x020a:
        if (r14 != 0) goto L_0x0218;
    L_0x020c:
        r8 = r9;
        r15 = r14;
        r13 = r12;
        goto L_0x01a2;
    L_0x0210:
        r12.close();	 Catch:{ Throwable -> 0x0214 }
        goto L_0x020a;	 Catch:{ Throwable -> 0x0214 }
    L_0x0214:
        r8 = move-exception;	 Catch:{ Throwable -> 0x0214 }
        r15 = r14;	 Catch:{ Throwable -> 0x0214 }
        r13 = r12;	 Catch:{ Throwable -> 0x0214 }
        goto L_0x01a2;	 Catch:{ Throwable -> 0x0214 }
    L_0x0218:
        r14.close();	 Catch:{ Throwable -> 0x0214 }
        goto L_0x020c;
    L_0x021c:
        r10 = move-exception;
        r14 = r15;
        r12 = r13;
    L_0x021f:
        if (r12 != 0) goto L_0x0224;
    L_0x0221:
        if (r14 != 0) goto L_0x022a;
    L_0x0223:
        throw r10;	 Catch:{ Throwable -> 0x0202 }
    L_0x0224:
        r12.close();	 Catch:{ Throwable -> 0x0228 }
        goto L_0x0221;	 Catch:{ Throwable -> 0x0228 }
    L_0x0228:
        r29 = move-exception;	 Catch:{ Throwable -> 0x0228 }
        goto L_0x0223;	 Catch:{ Throwable -> 0x0228 }
    L_0x022a:
        r14.close();	 Catch:{ Throwable -> 0x0228 }
        goto L_0x0223;
    L_0x022e:
        r30 = java.lang.Long.parseLong(r8);	 Catch:{ Throwable -> 0x029b, all -> 0x0296 }
        r6 = r6 + r30;
        goto L_0x01d2;
    L_0x0235:
        r12.close();	 Catch:{ Throwable -> 0x0239 }
        goto L_0x01d4;	 Catch:{ Throwable -> 0x0239 }
    L_0x0239:
        r8 = move-exception;	 Catch:{ Throwable -> 0x0239 }
    L_0x023a:
        goto L_0x01d6;	 Catch:{ Throwable -> 0x0239 }
    L_0x023b:
        r14.close();	 Catch:{ Throwable -> 0x0239 }
        goto L_0x01d6;
    L_0x023f:
        r9 = move-exception;
        r14 = r15;
        r12 = r13;
    L_0x0242:
        if (r12 != 0) goto L_0x0248;
    L_0x0244:
        if (r14 != 0) goto L_0x024e;
    L_0x0246:
        r8 = r9;
        goto L_0x01d6;
    L_0x0248:
        r12.close();	 Catch:{ Throwable -> 0x024c }
        goto L_0x0244;	 Catch:{ Throwable -> 0x024c }
    L_0x024c:
        r8 = move-exception;	 Catch:{ Throwable -> 0x024c }
        goto L_0x023a;	 Catch:{ Throwable -> 0x024c }
    L_0x024e:
        r14.close();	 Catch:{ Throwable -> 0x024c }
        goto L_0x0246;
    L_0x0252:
        r11 = move-exception;
        r14 = r15;
        r12 = r13;
    L_0x0255:
        if (r12 != 0) goto L_0x025a;
    L_0x0257:
        if (r14 != 0) goto L_0x0260;
    L_0x0259:
        throw r11;	 Catch:{ Throwable -> 0x0202 }
    L_0x025a:
        r12.close();	 Catch:{ Throwable -> 0x025e }
        goto L_0x0257;	 Catch:{ Throwable -> 0x025e }
    L_0x025e:
        r29 = move-exception;	 Catch:{ Throwable -> 0x025e }
        goto L_0x0259;	 Catch:{ Throwable -> 0x025e }
    L_0x0260:
        r14.close();	 Catch:{ Throwable -> 0x025e }
        goto L_0x0259;
    L_0x0264:
        r29 = 0;
        goto L_0x01de;
    L_0x0268:
        r29 = r4.keySet();	 Catch:{ Throwable -> 0x0202 }
        r22 = r29.iterator();	 Catch:{ Throwable -> 0x0202 }
    L_0x0270:
        r29 = r22.hasNext();	 Catch:{ Throwable -> 0x0202 }
        if (r29 == 0) goto L_0x0054;	 Catch:{ Throwable -> 0x0202 }
    L_0x0276:
        r23 = r22.next();	 Catch:{ Throwable -> 0x0202 }
        r23 = (java.lang.Integer) r23;	 Catch:{ Throwable -> 0x0202 }
        r29 = r23.intValue();	 Catch:{ Throwable -> 0x0202 }
        r0 = r29;	 Catch:{ Throwable -> 0x0202 }
        r1 = r16;	 Catch:{ Throwable -> 0x0202 }
        if (r0 != r1) goto L_0x0270;	 Catch:{ Throwable -> 0x0202 }
    L_0x0286:
        r4.clear();	 Catch:{ Throwable -> 0x0202 }
        goto L_0x0270;
    L_0x028a:
        r29 = move-exception;
        r14 = r15;
        r12 = r13;
        goto L_0x0054;
        r29 = move-exception;
        r14 = r15;
        goto L_0x0054;
    L_0x0293:
        r11 = move-exception;
        r14 = r15;
        goto L_0x0255;
    L_0x0296:
        r11 = move-exception;
        goto L_0x0255;
    L_0x0298:
        r9 = move-exception;
        r14 = r15;
        goto L_0x0242;
    L_0x029b:
        r9 = move-exception;
        goto L_0x0242;
    L_0x029d:
        r10 = move-exception;
        r14 = r15;
        goto L_0x021f;
    L_0x02a0:
        r10 = move-exception;
        goto L_0x021f;
    L_0x02a3:
        r9 = move-exception;
        r14 = r15;
        goto L_0x0208;
    L_0x02a7:
        r9 = move-exception;
        goto L_0x0208;
    L_0x02aa:
        r27 = move-exception;
        r12 = r13;
        goto L_0x00fb;
    L_0x02ae:
        r27 = move-exception;
        r12 = r13;
        goto L_0x00fb;
    L_0x02b2:
        r27 = move-exception;
        r14 = r15;
        r12 = r13;
        goto L_0x00fb;
    L_0x02b7:
        r18 = move-exception;
        r12 = r13;
        goto L_0x00e7;
    L_0x02bb:
        r18 = move-exception;
        r12 = r13;
        goto L_0x00e7;
    L_0x02bf:
        r18 = move-exception;
        r14 = r15;
        r12 = r13;
        goto L_0x00e7;
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.lc.er():java.util.Map<java.lang.Integer, java.lang.Long>");
    }

    public static String getPath() {
        return TMSDKContext.getApplicaionContext().getFilesDir().getAbsolutePath() + File.separator + "d_" + SmsCheckResult.ESCT_163;
    }
}
