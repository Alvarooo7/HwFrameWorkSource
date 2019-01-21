package com.android.server.notification;

import android.content.Context;

public class ImportanceExtractor implements NotificationSignalExtractor {
    private static final boolean DBG = false;
    private static final String TAG = "ImportanceExtractor";
    private RankingConfig mConfig;

    public void initialize(Context ctx, NotificationUsageStats usageStats) {
    }

    /* JADX WARNING: Missing block: B:11:0x0034, code skipped:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public RankingReconsideration process(NotificationRecord record) {
        if (!(record == null || record.getNotification() == null || this.mConfig == null || Boolean.valueOf("miscellaneous".equals(record.getChannel().getId())).booleanValue())) {
            record.setUserImportance(record.getChannel().getImportance());
        }
        return null;
    }

    public void setConfig(RankingConfig config) {
        this.mConfig = config;
    }

    public void setZenHelper(ZenModeHelper helper) {
    }
}
