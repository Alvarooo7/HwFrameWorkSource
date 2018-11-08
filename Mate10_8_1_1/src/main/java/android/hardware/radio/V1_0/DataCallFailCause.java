package android.hardware.radio.V1_0;

public final class DataCallFailCause {
    public static final int ACTIVATION_REJECT_GGSN = 30;
    public static final int ACTIVATION_REJECT_UNSPECIFIED = 31;
    public static final int APN_TYPE_CONFLICT = 112;
    public static final int AUTH_FAILURE_ON_EMERGENCY_CALL = 122;
    public static final int COMPANION_IFACE_IN_USE = 118;
    public static final int CONDITIONAL_IE_ERROR = 100;
    public static final int DATA_REGISTRATION_FAIL = -2;
    public static final int EMERGENCY_IFACE_ONLY = 116;
    public static final int EMM_ACCESS_BARRED = 115;
    public static final int EMM_ACCESS_BARRED_INFINITE_RETRY = 121;
    public static final int ERROR_UNSPECIFIED = 65535;
    public static final int ESM_INFO_NOT_RECEIVED = 53;
    public static final int FEATURE_NOT_SUPP = 40;
    public static final int FILTER_SEMANTIC_ERROR = 44;
    public static final int FILTER_SYTAX_ERROR = 45;
    public static final int IFACE_AND_POL_FAMILY_MISMATCH = 120;
    public static final int IFACE_MISMATCH = 117;
    public static final int INSUFFICIENT_RESOURCES = 26;
    public static final int INTERNAL_CALL_PREEMPT_BY_HIGH_PRIO_APN = 114;
    public static final int INVALID_MANDATORY_INFO = 96;
    public static final int INVALID_PCSCF_ADDR = 113;
    public static final int INVALID_TRANSACTION_ID = 81;
    public static final int IP_ADDRESS_MISMATCH = 119;
    public static final int MAX_ACTIVE_PDP_CONTEXT_REACHED = 65;
    public static final int MESSAGE_INCORRECT_SEMANTIC = 95;
    public static final int MESSAGE_TYPE_UNSUPPORTED = 97;
    public static final int MISSING_UKNOWN_APN = 27;
    public static final int MSG_AND_PROTOCOL_STATE_UNCOMPATIBLE = 101;
    public static final int MSG_TYPE_NONCOMPATIBLE_STATE = 98;
    public static final int MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED = 55;
    public static final int NAS_SIGNALLING = 14;
    public static final int NETWORK_FAILURE = 38;
    public static final int NONE = 0;
    public static final int NSAPI_IN_USE = 35;
    public static final int OEM_DCFAILCAUSE_1 = 4097;
    public static final int OEM_DCFAILCAUSE_10 = 4106;
    public static final int OEM_DCFAILCAUSE_11 = 4107;
    public static final int OEM_DCFAILCAUSE_12 = 4108;
    public static final int OEM_DCFAILCAUSE_13 = 4109;
    public static final int OEM_DCFAILCAUSE_14 = 4110;
    public static final int OEM_DCFAILCAUSE_15 = 4111;
    public static final int OEM_DCFAILCAUSE_2 = 4098;
    public static final int OEM_DCFAILCAUSE_3 = 4099;
    public static final int OEM_DCFAILCAUSE_4 = 4100;
    public static final int OEM_DCFAILCAUSE_5 = 4101;
    public static final int OEM_DCFAILCAUSE_6 = 4102;
    public static final int OEM_DCFAILCAUSE_7 = 4103;
    public static final int OEM_DCFAILCAUSE_8 = 4104;
    public static final int OEM_DCFAILCAUSE_9 = 4105;
    public static final int ONLY_IPV4_ALLOWED = 50;
    public static final int ONLY_IPV6_ALLOWED = 51;
    public static final int ONLY_SINGLE_BEARER_ALLOWED = 52;
    public static final int OPERATOR_BARRED = 8;
    public static final int PDN_CONN_DOES_NOT_EXIST = 54;
    public static final int PDP_WITHOUT_ACTIVE_TFT = 46;
    public static final int PREF_RADIO_TECH_CHANGED = -4;
    public static final int PROTOCOL_ERRORS = 111;
    public static final int QOS_NOT_ACCEPTED = 37;
    public static final int RADIO_POWER_OFF = -5;
    public static final int REGULAR_DEACTIVATION = 36;
    public static final int SERVICE_OPTION_NOT_SUBSCRIBED = 33;
    public static final int SERVICE_OPTION_NOT_SUPPORTED = 32;
    public static final int SERVICE_OPTION_OUT_OF_ORDER = 34;
    public static final int SIGNAL_LOST = -3;
    public static final int TETHERED_CALL_ACTIVE = -6;
    public static final int TFT_SEMANTIC_ERROR = 41;
    public static final int TFT_SYTAX_ERROR = 42;
    public static final int UMTS_REACTIVATION_REQ = 39;
    public static final int UNKNOWN_INFO_ELEMENT = 99;
    public static final int UNKNOWN_PDP_ADDRESS_TYPE = 28;
    public static final int UNKNOWN_PDP_CONTEXT = 43;
    public static final int UNSUPPORTED_APN_IN_CURRENT_PLMN = 66;
    public static final int USER_AUTHENTICATION = 29;
    public static final int VOICE_REGISTRATION_FAIL = -1;

    public static final java.lang.String dumpBitfield(int r1) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.radio.V1_0.DataCallFailCause.dumpBitfield(int):java.lang.String
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:31)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:296)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.V1_0.DataCallFailCause.dumpBitfield(int):java.lang.String");
    }

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 8) {
            return "OPERATOR_BARRED";
        }
        if (o == 14) {
            return "NAS_SIGNALLING";
        }
        if (o == 26) {
            return "INSUFFICIENT_RESOURCES";
        }
        if (o == 27) {
            return "MISSING_UKNOWN_APN";
        }
        if (o == 28) {
            return "UNKNOWN_PDP_ADDRESS_TYPE";
        }
        if (o == 29) {
            return "USER_AUTHENTICATION";
        }
        if (o == 30) {
            return "ACTIVATION_REJECT_GGSN";
        }
        if (o == 31) {
            return "ACTIVATION_REJECT_UNSPECIFIED";
        }
        if (o == 32) {
            return "SERVICE_OPTION_NOT_SUPPORTED";
        }
        if (o == 33) {
            return "SERVICE_OPTION_NOT_SUBSCRIBED";
        }
        if (o == 34) {
            return "SERVICE_OPTION_OUT_OF_ORDER";
        }
        if (o == 35) {
            return "NSAPI_IN_USE";
        }
        if (o == 36) {
            return "REGULAR_DEACTIVATION";
        }
        if (o == 37) {
            return "QOS_NOT_ACCEPTED";
        }
        if (o == 38) {
            return "NETWORK_FAILURE";
        }
        if (o == 39) {
            return "UMTS_REACTIVATION_REQ";
        }
        if (o == 40) {
            return "FEATURE_NOT_SUPP";
        }
        if (o == 41) {
            return "TFT_SEMANTIC_ERROR";
        }
        if (o == 42) {
            return "TFT_SYTAX_ERROR";
        }
        if (o == 43) {
            return "UNKNOWN_PDP_CONTEXT";
        }
        if (o == 44) {
            return "FILTER_SEMANTIC_ERROR";
        }
        if (o == 45) {
            return "FILTER_SYTAX_ERROR";
        }
        if (o == 46) {
            return "PDP_WITHOUT_ACTIVE_TFT";
        }
        if (o == 50) {
            return "ONLY_IPV4_ALLOWED";
        }
        if (o == 51) {
            return "ONLY_IPV6_ALLOWED";
        }
        if (o == 52) {
            return "ONLY_SINGLE_BEARER_ALLOWED";
        }
        if (o == 53) {
            return "ESM_INFO_NOT_RECEIVED";
        }
        if (o == 54) {
            return "PDN_CONN_DOES_NOT_EXIST";
        }
        if (o == 55) {
            return "MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED";
        }
        if (o == 65) {
            return "MAX_ACTIVE_PDP_CONTEXT_REACHED";
        }
        if (o == 66) {
            return "UNSUPPORTED_APN_IN_CURRENT_PLMN";
        }
        if (o == 81) {
            return "INVALID_TRANSACTION_ID";
        }
        if (o == 95) {
            return "MESSAGE_INCORRECT_SEMANTIC";
        }
        if (o == 96) {
            return "INVALID_MANDATORY_INFO";
        }
        if (o == 97) {
            return "MESSAGE_TYPE_UNSUPPORTED";
        }
        if (o == 98) {
            return "MSG_TYPE_NONCOMPATIBLE_STATE";
        }
        if (o == 99) {
            return "UNKNOWN_INFO_ELEMENT";
        }
        if (o == 100) {
            return "CONDITIONAL_IE_ERROR";
        }
        if (o == 101) {
            return "MSG_AND_PROTOCOL_STATE_UNCOMPATIBLE";
        }
        if (o == 111) {
            return "PROTOCOL_ERRORS";
        }
        if (o == 112) {
            return "APN_TYPE_CONFLICT";
        }
        if (o == 113) {
            return "INVALID_PCSCF_ADDR";
        }
        if (o == 114) {
            return "INTERNAL_CALL_PREEMPT_BY_HIGH_PRIO_APN";
        }
        if (o == 115) {
            return "EMM_ACCESS_BARRED";
        }
        if (o == 116) {
            return "EMERGENCY_IFACE_ONLY";
        }
        if (o == 117) {
            return "IFACE_MISMATCH";
        }
        if (o == 118) {
            return "COMPANION_IFACE_IN_USE";
        }
        if (o == 119) {
            return "IP_ADDRESS_MISMATCH";
        }
        if (o == 120) {
            return "IFACE_AND_POL_FAMILY_MISMATCH";
        }
        if (o == 121) {
            return "EMM_ACCESS_BARRED_INFINITE_RETRY";
        }
        if (o == 122) {
            return "AUTH_FAILURE_ON_EMERGENCY_CALL";
        }
        if (o == OEM_DCFAILCAUSE_1) {
            return "OEM_DCFAILCAUSE_1";
        }
        if (o == OEM_DCFAILCAUSE_2) {
            return "OEM_DCFAILCAUSE_2";
        }
        if (o == OEM_DCFAILCAUSE_3) {
            return "OEM_DCFAILCAUSE_3";
        }
        if (o == OEM_DCFAILCAUSE_4) {
            return "OEM_DCFAILCAUSE_4";
        }
        if (o == OEM_DCFAILCAUSE_5) {
            return "OEM_DCFAILCAUSE_5";
        }
        if (o == OEM_DCFAILCAUSE_6) {
            return "OEM_DCFAILCAUSE_6";
        }
        if (o == OEM_DCFAILCAUSE_7) {
            return "OEM_DCFAILCAUSE_7";
        }
        if (o == OEM_DCFAILCAUSE_8) {
            return "OEM_DCFAILCAUSE_8";
        }
        if (o == OEM_DCFAILCAUSE_9) {
            return "OEM_DCFAILCAUSE_9";
        }
        if (o == OEM_DCFAILCAUSE_10) {
            return "OEM_DCFAILCAUSE_10";
        }
        if (o == OEM_DCFAILCAUSE_11) {
            return "OEM_DCFAILCAUSE_11";
        }
        if (o == OEM_DCFAILCAUSE_12) {
            return "OEM_DCFAILCAUSE_12";
        }
        if (o == OEM_DCFAILCAUSE_13) {
            return "OEM_DCFAILCAUSE_13";
        }
        if (o == OEM_DCFAILCAUSE_14) {
            return "OEM_DCFAILCAUSE_14";
        }
        if (o == OEM_DCFAILCAUSE_15) {
            return "OEM_DCFAILCAUSE_15";
        }
        if (o == -1) {
            return "VOICE_REGISTRATION_FAIL";
        }
        if (o == -2) {
            return "DATA_REGISTRATION_FAIL";
        }
        if (o == -3) {
            return "SIGNAL_LOST";
        }
        if (o == -4) {
            return "PREF_RADIO_TECH_CHANGED";
        }
        if (o == -5) {
            return "RADIO_POWER_OFF";
        }
        if (o == -6) {
            return "TETHERED_CALL_ACTIVE";
        }
        if (o == 65535) {
            return "ERROR_UNSPECIFIED";
        }
        return "0x" + Integer.toHexString(o);
    }
}
