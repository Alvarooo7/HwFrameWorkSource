package com.huawei.android.pushagent.utils.b;

class j implements c<Boolean, Object> {
    private j() {
    }

    public Boolean py(Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        return obj instanceof String ? Boolean.valueOf(Boolean.parseBoolean((String) obj)) : null;
    }
}
