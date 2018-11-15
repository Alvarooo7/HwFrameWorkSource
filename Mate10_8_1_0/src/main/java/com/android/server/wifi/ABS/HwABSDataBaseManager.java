package com.android.server.wifi.ABS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class HwABSDataBaseManager {
    private static final int DATA_BASE_MAX_NUM = 500;
    private static final String TAG = "DataBaseManager";
    private static HwABSDataBaseManager mHwABSDataBaseManager = null;
    private SQLiteDatabase mDatabase;
    private HwABSDataBaseHelper mHelper;
    private Object mLock = new Object();

    private HwABSDataBaseManager(Context context) {
        HwABSUtils.logD("HwABSDataBaseManager()");
        this.mHelper = new HwABSDataBaseHelper(context);
        this.mDatabase = this.mHelper.getWritableDatabase();
    }

    public static HwABSDataBaseManager getInstance(Context context) {
        if (mHwABSDataBaseManager == null) {
            mHwABSDataBaseManager = new HwABSDataBaseManager(context);
        }
        return mHwABSDataBaseManager;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void closeDB() {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                HwABSUtils.logD("HwABSDataBaseManager closeDB()");
                this.mDatabase.close();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addOrUpdateApInfos(HwABSApInfoData data) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || data == null) {
            } else if (getApInfoByBssid(data.mBssid) == null) {
                HwABSUtils.logD("addOrUpdateApInfos inlineAddApInfo");
                checkIfAllCaseNumSatisfy();
                inlineAddApInfo(data);
            } else {
                HwABSUtils.logD("addOrUpdateApInfos");
                inlineUpdateApInfo(data);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<HwABSApInfoData> getApInfoBySsid(String ssid) {
        Cursor cursor;
        synchronized (this.mLock) {
            List<HwABSApInfoData> lists = new ArrayList();
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                cursor = null;
                try {
                    cursor = this.mDatabase.rawQuery("SELECT * FROM MIMOApInfoTable where ssid like ?", new String[]{ssid});
                    while (cursor.moveToNext()) {
                        lists.add(new HwABSApInfoData(cursor.getString(cursor.getColumnIndex("bssid")), cursor.getString(cursor.getColumnIndex("ssid")), cursor.getInt(cursor.getColumnIndex("switch_mimo_type")), cursor.getInt(cursor.getColumnIndex("switch_siso_type")), cursor.getInt(cursor.getColumnIndex("auth_type")), cursor.getInt(cursor.getColumnIndex("in_black_list")), cursor.getInt(cursor.getColumnIndex("reassociate_times")), cursor.getInt(cursor.getColumnIndex("failed_times")), cursor.getInt(cursor.getColumnIndex("continuous_failure_times")), cursor.getLong(cursor.getColumnIndex("last_connect_time"))));
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (SQLException e) {
                    HwABSUtils.logE("getApInfoBySsid:" + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return null;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public HwABSApInfoData getApInfoByBssid(String bssid) {
        Throwable th;
        synchronized (this.mLock) {
            if (bssid == null) {
                return null;
            }
            Cursor cursor = null;
            HwABSApInfoData data;
            try {
                cursor = this.mDatabase.rawQuery("SELECT * FROM MIMOApInfoTable where bssid like ?", new String[]{bssid});
                if (cursor.moveToNext()) {
                    data = new HwABSApInfoData(cursor.getString(cursor.getColumnIndex("bssid")), cursor.getString(cursor.getColumnIndex("ssid")), cursor.getInt(cursor.getColumnIndex("switch_mimo_type")), cursor.getInt(cursor.getColumnIndex("switch_siso_type")), cursor.getInt(cursor.getColumnIndex("auth_type")), cursor.getInt(cursor.getColumnIndex("in_black_list")), cursor.getInt(cursor.getColumnIndex("reassociate_times")), cursor.getInt(cursor.getColumnIndex("failed_times")), cursor.getInt(cursor.getColumnIndex("continuous_failure_times")), cursor.getLong(cursor.getColumnIndex("last_connect_time")));
                } else {
                    data = null;
                }
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
            } catch (SQLException e) {
                HwABSUtils.logE("getApInfoByBssid:" + e);
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                data = null;
                throw th;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<HwABSApInfoData> getApInfoInBlackList() {
        List<HwABSApInfoData> lists;
        Cursor cursor;
        SQLException e;
        Throwable th;
        synchronized (this.mLock) {
            try {
                lists = new ArrayList();
                if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
                } else {
                    cursor = null;
                    try {
                        HwABSApInfoData data;
                        cursor = this.mDatabase.rawQuery("SELECT * FROM MIMOApInfoTable where in_black_list like ?", new String[]{"1"});
                        HwABSApInfoData data2 = null;
                        while (cursor.moveToNext()) {
                            try {
                                data = new HwABSApInfoData(cursor.getString(cursor.getColumnIndex("bssid")), cursor.getString(cursor.getColumnIndex("ssid")), cursor.getInt(cursor.getColumnIndex("switch_mimo_type")), cursor.getInt(cursor.getColumnIndex("switch_siso_type")), cursor.getInt(cursor.getColumnIndex("auth_type")), cursor.getInt(cursor.getColumnIndex("in_black_list")), cursor.getInt(cursor.getColumnIndex("reassociate_times")), cursor.getInt(cursor.getColumnIndex("failed_times")), cursor.getInt(cursor.getColumnIndex("continuous_failure_times")), cursor.getLong(cursor.getColumnIndex("last_connect_time")));
                                lists.add(data);
                                data2 = data;
                            } catch (SQLException e2) {
                                e = e2;
                                data = data2;
                            } catch (Throwable th2) {
                                th = th2;
                                data = data2;
                            }
                        }
                        if (cursor != null) {
                            try {
                                cursor.close();
                            } catch (Throwable th3) {
                                th = th3;
                                data = data2;
                                throw th;
                            }
                        }
                    } catch (SQLException e3) {
                        e = e3;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        }
        try {
            HwABSUtils.logE("getApInfoByBssid:" + e);
            if (cursor != null) {
                cursor.close();
            }
            return lists;
        } catch (Throwable th5) {
            th = th5;
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void deleteAPInfosByBssid(HwABSApInfoData data) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0 || data == null) {
            } else {
                inlineDeleteApInfoByBssid(data.mBssid);
            }
        }
    }

    public void deleteAPInfosBySsid(HwABSApInfoData data) {
        synchronized (this.mLock) {
            if (data == null) {
                return;
            }
            inlineDeleteApInfoBySsid(data.mSsid);
        }
    }

    private void inlineDeleteApInfoBySsid(String ssid) {
        if (this.mDatabase != null && (this.mDatabase.isOpen() ^ 1) == 0 && ssid != null) {
            this.mDatabase.delete(HwABSDataBaseHelper.MIMO_AP_TABLE_NAME, "ssid like ?", new String[]{ssid});
        }
    }

    private void inlineDeleteApInfoByBssid(String bssid) {
        if (this.mDatabase != null && (this.mDatabase.isOpen() ^ 1) == 0 && bssid != null) {
            this.mDatabase.delete(HwABSDataBaseHelper.MIMO_AP_TABLE_NAME, "bssid like ?", new String[]{bssid});
        }
    }

    private void inlineAddApInfo(HwABSApInfoData data) {
        if (data.mBssid != null) {
            this.mDatabase.execSQL("INSERT INTO MIMOApInfoTable VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{data.mBssid, data.mSsid, Integer.valueOf(data.mSwitch_mimo_type), Integer.valueOf(data.mSwitch_siso_type), Integer.valueOf(data.mAuth_type), Integer.valueOf(data.mIn_black_List), Integer.valueOf(0), Integer.valueOf(data.mReassociate_times), Integer.valueOf(data.mFailed_times), Integer.valueOf(data.mContinuous_failure_times), Long.valueOf(data.mLast_connect_time), Integer.valueOf(0)});
        }
    }

    private void inlineUpdateApInfo(HwABSApInfoData data) {
        if (this.mDatabase != null && (this.mDatabase.isOpen() ^ 1) == 0 && data.mBssid != null) {
            HwABSUtils.logD("inlineUpdateApInfo ssid = " + data.mSsid);
            ContentValues values = new ContentValues();
            values.put("bssid", data.mBssid);
            values.put("ssid", data.mSsid);
            values.put("switch_mimo_type", Integer.valueOf(data.mSwitch_mimo_type));
            values.put("switch_siso_type", Integer.valueOf(data.mSwitch_siso_type));
            values.put("auth_type", Integer.valueOf(data.mAuth_type));
            values.put("in_black_list", Integer.valueOf(data.mIn_black_List));
            values.put("in_vowifi_black_list", Integer.valueOf(0));
            values.put("reassociate_times", Integer.valueOf(data.mReassociate_times));
            values.put("failed_times", Integer.valueOf(data.mFailed_times));
            values.put("continuous_failure_times", Integer.valueOf(data.mContinuous_failure_times));
            values.put("last_connect_time", Long.valueOf(data.mLast_connect_time));
            this.mDatabase.update(HwABSDataBaseHelper.MIMO_AP_TABLE_NAME, values, "bssid like ?", new String[]{data.mBssid});
        }
    }

    private void checkIfAllCaseNumSatisfy() {
        List<HwABSApInfoData> lists = getAllApInfo();
        long last_connect_time = 0;
        String str = null;
        boolean isDeleteRecord = false;
        HwABSUtils.logD("checkIfAllCaseNumSatisfy lists.size() = " + lists.size());
        if (lists.size() >= 500) {
            isDeleteRecord = true;
            for (HwABSApInfoData data : lists) {
                long current_connect_time = data.mLast_connect_time;
                if (last_connect_time == 0 || last_connect_time > current_connect_time) {
                    last_connect_time = current_connect_time;
                    str = data.mBssid;
                }
            }
        }
        if (isDeleteRecord) {
            synchronized (this.mLock) {
                inlineDeleteApInfoByBssid(str);
            }
        }
    }

    public List<HwABSApInfoData> getAllApInfo() {
        List<HwABSApInfoData> lists;
        synchronized (this.mLock) {
            Cursor cursor = null;
            lists = new ArrayList();
            try {
                cursor = this.mDatabase.rawQuery("SELECT * FROM MIMOApInfoTable", null);
                while (cursor.moveToNext()) {
                    lists.add(new HwABSApInfoData(cursor.getString(cursor.getColumnIndex("bssid")), cursor.getString(cursor.getColumnIndex("ssid")), cursor.getInt(cursor.getColumnIndex("switch_mimo_type")), cursor.getInt(cursor.getColumnIndex("switch_siso_type")), cursor.getInt(cursor.getColumnIndex("auth_type")), cursor.getInt(cursor.getColumnIndex("in_black_list")), cursor.getInt(cursor.getColumnIndex("reassociate_times")), cursor.getInt(cursor.getColumnIndex("failed_times")), cursor.getInt(cursor.getColumnIndex("continuous_failure_times")), cursor.getLong(cursor.getColumnIndex("last_connect_time"))));
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLException e) {
                HwABSUtils.logE("getAllApInfo:" + e);
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return lists;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public HwABSCHRStatistics getCHRStatistics() {
        SQLException e;
        Throwable th;
        synchronized (this.mLock) {
            HwABSCHRStatistics hwABSCHRStatistics = null;
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                Cursor cursor = null;
                try {
                    cursor = this.mDatabase.rawQuery("SELECT * FROM StatisticsTable", null);
                    if (cursor.moveToNext()) {
                        HwABSCHRStatistics statistics = new HwABSCHRStatistics();
                        try {
                            statistics.long_connect_event = cursor.getInt(cursor.getColumnIndex("long_connect_event"));
                            statistics.short_connect_event = cursor.getInt(cursor.getColumnIndex("short_connect_event"));
                            statistics.search_event = cursor.getInt(cursor.getColumnIndex("search_event"));
                            statistics.antenna_preempted_screen_on_event = cursor.getInt(cursor.getColumnIndex("antenna_preempted_screen_on_event"));
                            statistics.antenna_preempted_screen_off_event = cursor.getInt(cursor.getColumnIndex("antenna_preempted_screen_off_event"));
                            statistics.mo_mt_call_event = cursor.getInt(cursor.getColumnIndex("mo_mt_call_event"));
                            statistics.siso_to_mimo_event = cursor.getInt(cursor.getColumnIndex("siso_to_mimo_event"));
                            statistics.ping_pong_times = cursor.getInt(cursor.getColumnIndex("ping_pong_times"));
                            statistics.max_ping_pong_times = cursor.getInt(cursor.getColumnIndex("max_ping_pong_times"));
                            statistics.siso_time = (long) cursor.getInt(cursor.getColumnIndex("siso_time"));
                            statistics.mimo_time = (long) cursor.getInt(cursor.getColumnIndex("mimo_time"));
                            statistics.mimo_screen_on_time = (long) cursor.getInt(cursor.getColumnIndex("mimo_screen_on_time"));
                            statistics.siso_screen_on_time = (long) cursor.getInt(cursor.getColumnIndex("siso_screen_on_time"));
                            statistics.last_upload_time = cursor.getLong(cursor.getColumnIndex("last_upload_time"));
                            statistics.mRssiL0 = cursor.getInt(cursor.getColumnIndex("rssiL0"));
                            statistics.mRssiL1 = cursor.getInt(cursor.getColumnIndex("rssiL1"));
                            statistics.mRssiL2 = cursor.getInt(cursor.getColumnIndex("rssiL2"));
                            statistics.mRssiL3 = cursor.getInt(cursor.getColumnIndex("rssiL3"));
                            statistics.mRssiL4 = cursor.getInt(cursor.getColumnIndex("rssiL4"));
                            hwABSCHRStatistics = statistics;
                        } catch (SQLException e2) {
                            e = e2;
                            hwABSCHRStatistics = statistics;
                            try {
                                HwABSUtils.logE("getCHRStatistics: " + e);
                                if (cursor != null) {
                                    cursor.close();
                                }
                                return null;
                            } catch (Throwable th2) {
                                th = th2;
                                if (cursor != null) {
                                    cursor.close();
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            if (cursor != null) {
                                cursor.close();
                            }
                            throw th;
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (SQLException e3) {
                    e = e3;
                    HwABSUtils.logE("getCHRStatistics: " + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return null;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void inlineAddCHRInfo(HwABSCHRStatistics data) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                this.mDatabase.execSQL("INSERT INTO StatisticsTable VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{Integer.valueOf(data.long_connect_event), Integer.valueOf(data.short_connect_event), Integer.valueOf(data.search_event), Integer.valueOf(data.antenna_preempted_screen_on_event), Integer.valueOf(data.antenna_preempted_screen_off_event), Integer.valueOf(data.mo_mt_call_event), Integer.valueOf(data.siso_to_mimo_event), Integer.valueOf(data.ping_pong_times), Integer.valueOf(data.max_ping_pong_times), Long.valueOf(data.mimo_time), Long.valueOf(data.siso_time), Long.valueOf(data.mimo_screen_on_time), Long.valueOf(data.siso_screen_on_time), Long.valueOf(data.last_upload_time), Integer.valueOf(data.mRssiL0), Integer.valueOf(data.mRssiL1), Integer.valueOf(data.mRssiL2), Integer.valueOf(data.mRssiL3), Integer.valueOf(data.mRssiL4), Integer.valueOf(0)});
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void inlineUpdateCHRInfo(HwABSCHRStatistics data) {
        synchronized (this.mLock) {
            if (this.mDatabase == null || (this.mDatabase.isOpen() ^ 1) != 0) {
            } else {
                HwABSUtils.logD("inlineUpdateCHRInfo ");
                ContentValues values = new ContentValues();
                values.put("long_connect_event", Integer.valueOf(data.long_connect_event));
                values.put("short_connect_event", Integer.valueOf(data.short_connect_event));
                values.put("search_event", Integer.valueOf(data.search_event));
                values.put("antenna_preempted_screen_on_event", Integer.valueOf(data.antenna_preempted_screen_on_event));
                values.put("antenna_preempted_screen_off_event", Integer.valueOf(data.antenna_preempted_screen_off_event));
                values.put("mo_mt_call_event", Integer.valueOf(data.mo_mt_call_event));
                values.put("siso_to_mimo_event", Integer.valueOf(data.siso_to_mimo_event));
                values.put("ping_pong_times", Integer.valueOf(data.ping_pong_times));
                values.put("max_ping_pong_times", Integer.valueOf(data.max_ping_pong_times));
                values.put("mimo_time", Long.valueOf(data.mimo_time));
                values.put("siso_time", Long.valueOf(data.siso_time));
                values.put("mimo_screen_on_time", Long.valueOf(data.mimo_screen_on_time));
                values.put("siso_screen_on_time", Long.valueOf(data.siso_screen_on_time));
                values.put("last_upload_time", Long.valueOf(data.last_upload_time));
                values.put("rssiL0", Integer.valueOf(data.mRssiL0));
                values.put("rssiL1", Integer.valueOf(data.mRssiL1));
                values.put("rssiL2", Integer.valueOf(data.mRssiL2));
                values.put("rssiL3", Integer.valueOf(data.mRssiL3));
                values.put("rssiL4", Integer.valueOf(data.mRssiL4));
                this.mDatabase.update(HwABSDataBaseHelper.STATISTICS_TABLE_NAME, values, "_id like ?", new String[]{"1"});
            }
        }
    }
}
