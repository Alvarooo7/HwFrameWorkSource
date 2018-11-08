package android.view;

import android.os.Build;
import android.util.Log;

public final class InputEventConsistencyVerifier {
    private static final String EVENT_TYPE_GENERIC_MOTION = "GenericMotionEvent";
    private static final String EVENT_TYPE_KEY = "KeyEvent";
    private static final String EVENT_TYPE_TOUCH = "TouchEvent";
    private static final String EVENT_TYPE_TRACKBALL = "TrackballEvent";
    public static final int FLAG_RAW_DEVICE_INPUT = 1;
    private static final boolean IS_ENG_BUILD = Build.IS_ENG;
    private static final int RECENT_EVENTS_TO_LOG = 5;
    private int mButtonsPressed;
    private final Object mCaller;
    private InputEvent mCurrentEvent;
    private String mCurrentEventType;
    private final int mFlags;
    private boolean mHoverEntered;
    private KeyState mKeyStateList;
    private int mLastEventSeq;
    private String mLastEventType;
    private int mLastNestingLevel;
    private final String mLogTag;
    private int mMostRecentEventIndex;
    private InputEvent[] mRecentEvents;
    private boolean[] mRecentEventsUnhandled;
    private int mTouchEventStreamDeviceId;
    private boolean mTouchEventStreamIsTainted;
    private int mTouchEventStreamPointers;
    private int mTouchEventStreamSource;
    private boolean mTouchEventStreamUnhandled;
    private boolean mTrackballDown;
    private boolean mTrackballUnhandled;
    private StringBuilder mViolationMessage;

    private static final class KeyState {
        private static KeyState mRecycledList;
        private static Object mRecycledListLock = new Object();
        public int deviceId;
        public int keyCode;
        public KeyState next;
        public int source;
        public boolean unhandled;

        private KeyState() {
        }

        public static KeyState obtain(int deviceId, int source, int keyCode) {
            KeyState state;
            synchronized (mRecycledListLock) {
                state = mRecycledList;
                if (state != null) {
                    mRecycledList = state.next;
                } else {
                    state = new KeyState();
                }
            }
            state.deviceId = deviceId;
            state.source = source;
            state.keyCode = keyCode;
            state.unhandled = false;
            return state;
        }

        public void recycle() {
            synchronized (mRecycledListLock) {
                this.next = mRecycledList;
                mRecycledList = this.next;
            }
        }
    }

    public void onGenericMotionEvent(android.view.MotionEvent r1, int r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.InputEventConsistencyVerifier.onGenericMotionEvent(android.view.MotionEvent, int):void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.InputEventConsistencyVerifier.onGenericMotionEvent(android.view.MotionEvent, int):void");
    }

    public void onTouchEvent(android.view.MotionEvent r1, int r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.InputEventConsistencyVerifier.onTouchEvent(android.view.MotionEvent, int):void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.InputEventConsistencyVerifier.onTouchEvent(android.view.MotionEvent, int):void");
    }

    public InputEventConsistencyVerifier(Object caller, int flags) {
        this(caller, flags, null);
    }

    public InputEventConsistencyVerifier(Object caller, int flags, String logTag) {
        this.mTouchEventStreamDeviceId = -1;
        this.mCaller = caller;
        this.mFlags = flags;
        if (logTag == null) {
            logTag = "InputEventConsistencyVerifier";
        }
        this.mLogTag = logTag;
    }

    public static boolean isInstrumentationEnabled() {
        return IS_ENG_BUILD;
    }

    public void reset() {
        this.mLastEventSeq = -1;
        this.mLastNestingLevel = 0;
        this.mTrackballDown = false;
        this.mTrackballUnhandled = false;
        this.mTouchEventStreamPointers = 0;
        this.mTouchEventStreamIsTainted = false;
        this.mTouchEventStreamUnhandled = false;
        this.mHoverEntered = false;
        this.mButtonsPressed = 0;
        while (this.mKeyStateList != null) {
            KeyState state = this.mKeyStateList;
            this.mKeyStateList = state.next;
            state.recycle();
        }
    }

    public void onInputEvent(InputEvent event, int nestingLevel) {
        if (event instanceof KeyEvent) {
            onKeyEvent((KeyEvent) event, nestingLevel);
            return;
        }
        MotionEvent motionEvent = (MotionEvent) event;
        if (motionEvent.isTouchEvent()) {
            onTouchEvent(motionEvent, nestingLevel);
        } else if ((motionEvent.getSource() & 4) != 0) {
            onTrackballEvent(motionEvent, nestingLevel);
        } else {
            onGenericMotionEvent(motionEvent, nestingLevel);
        }
    }

    public void onKeyEvent(KeyEvent event, int nestingLevel) {
        if (startEvent(event, nestingLevel, EVENT_TYPE_KEY)) {
            try {
                ensureMetaStateIsNormalized(event.getMetaState());
                int action = event.getAction();
                int deviceId = event.getDeviceId();
                int source = event.getSource();
                int keyCode = event.getKeyCode();
                KeyState state;
                switch (action) {
                    case 0:
                        state = findKeyState(deviceId, source, keyCode, false);
                        if (state != null) {
                            if (!state.unhandled) {
                                if ((this.mFlags & 1) == 0 && event.getRepeatCount() == 0) {
                                    problem("ACTION_DOWN but key is already down and this event is not a key repeat.");
                                    break;
                                }
                            }
                            state.unhandled = false;
                            break;
                        }
                        addKeyState(deviceId, source, keyCode);
                        break;
                    case 1:
                        state = findKeyState(deviceId, source, keyCode, true);
                        if (state != null) {
                            state.recycle();
                            break;
                        } else {
                            problem("ACTION_UP but key was not down.");
                            break;
                        }
                    case 2:
                        break;
                    default:
                        problem("Invalid action " + KeyEvent.actionToString(action) + " for key event.");
                        break;
                }
                finishEvent();
            } catch (Throwable th) {
                finishEvent();
            }
        }
    }

    public void onTrackballEvent(MotionEvent event, int nestingLevel) {
        if (startEvent(event, nestingLevel, EVENT_TYPE_TRACKBALL)) {
            try {
                ensureMetaStateIsNormalized(event.getMetaState());
                int action = event.getAction();
                if ((event.getSource() & 4) != 0) {
                    switch (action) {
                        case 0:
                            if (!this.mTrackballDown || (this.mTrackballUnhandled ^ 1) == 0) {
                                this.mTrackballDown = true;
                                this.mTrackballUnhandled = false;
                            } else {
                                problem("ACTION_DOWN but trackball is already down.");
                            }
                            ensureHistorySizeIsZeroForThisAction(event);
                            ensurePointerCountIsOneForThisAction(event);
                            break;
                        case 1:
                            if (this.mTrackballDown) {
                                this.mTrackballDown = false;
                                this.mTrackballUnhandled = false;
                            } else {
                                problem("ACTION_UP but trackball is not down.");
                            }
                            ensureHistorySizeIsZeroForThisAction(event);
                            ensurePointerCountIsOneForThisAction(event);
                            break;
                        case 2:
                            ensurePointerCountIsOneForThisAction(event);
                            break;
                        default:
                            problem("Invalid action " + MotionEvent.actionToString(action) + " for trackball event.");
                            break;
                    }
                    if (this.mTrackballDown && event.getPressure() <= 0.0f) {
                        problem("Trackball is down but pressure is not greater than 0.");
                    } else if (!(this.mTrackballDown || event.getPressure() == 0.0f)) {
                        problem("Trackball is up but pressure is not equal to 0.");
                    }
                } else {
                    problem("Source was not SOURCE_CLASS_TRACKBALL.");
                }
                finishEvent();
            } catch (Throwable th) {
                finishEvent();
            }
        }
    }

    public void onUnhandledEvent(InputEvent event, int nestingLevel) {
        if (nestingLevel == this.mLastNestingLevel) {
            if (this.mRecentEventsUnhandled != null) {
                this.mRecentEventsUnhandled[this.mMostRecentEventIndex] = true;
            }
            if (event instanceof KeyEvent) {
                KeyEvent keyEvent = (KeyEvent) event;
                KeyState state = findKeyState(keyEvent.getDeviceId(), keyEvent.getSource(), keyEvent.getKeyCode(), false);
                if (state != null) {
                    state.unhandled = true;
                }
            } else {
                MotionEvent motionEvent = (MotionEvent) event;
                if (motionEvent.isTouchEvent()) {
                    this.mTouchEventStreamUnhandled = true;
                } else if ((motionEvent.getSource() & 4) != 0 && this.mTrackballDown) {
                    this.mTrackballUnhandled = true;
                }
            }
        }
    }

    private void ensureMetaStateIsNormalized(int metaState) {
        if (KeyEvent.normalizeMetaState(metaState) != metaState) {
            problem(String.format("Metastate not normalized.  Was 0x%08x but expected 0x%08x.", new Object[]{Integer.valueOf(metaState), Integer.valueOf(KeyEvent.normalizeMetaState(metaState))}));
        }
    }

    private void ensurePointerCountIsOneForThisAction(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        if (pointerCount != 1) {
            problem("Pointer count is " + pointerCount + " but it should always be 1 for " + MotionEvent.actionToString(event.getAction()));
        }
    }

    private void ensureActionButtonIsNonZeroForThisAction(MotionEvent event) {
        if (event.getActionButton() == 0) {
            problem("No action button set. Action button should always be non-zero for " + MotionEvent.actionToString(event.getAction()));
        }
    }

    private void ensureHistorySizeIsZeroForThisAction(MotionEvent event) {
        int historySize = event.getHistorySize();
        if (historySize != 0) {
            problem("History size is " + historySize + " but it should always be 0 for " + MotionEvent.actionToString(event.getAction()));
        }
    }

    private boolean startEvent(InputEvent event, int nestingLevel, String eventType) {
        int seq = event.getSequenceNumber();
        if (seq == this.mLastEventSeq && nestingLevel < this.mLastNestingLevel && eventType == this.mLastEventType) {
            return false;
        }
        if (nestingLevel > 0) {
            this.mLastEventSeq = seq;
            this.mLastEventType = eventType;
            this.mLastNestingLevel = nestingLevel;
        } else {
            this.mLastEventSeq = -1;
            this.mLastEventType = null;
            this.mLastNestingLevel = 0;
        }
        this.mCurrentEvent = event;
        this.mCurrentEventType = eventType;
        return true;
    }

    private void finishEvent() {
        int index;
        if (!(this.mViolationMessage == null || this.mViolationMessage.length() == 0)) {
            if (!this.mCurrentEvent.isTainted()) {
                this.mViolationMessage.append("\n  in ").append(this.mCaller);
                this.mViolationMessage.append("\n  ");
                appendEvent(this.mViolationMessage, 0, this.mCurrentEvent, false);
                if (this.mRecentEvents != null) {
                    this.mViolationMessage.append("\n  -- recent events --");
                    for (int i = 0; i < 5; i++) {
                        index = ((this.mMostRecentEventIndex + 5) - i) % 5;
                        InputEvent event = this.mRecentEvents[index];
                        if (event == null) {
                            break;
                        }
                        this.mViolationMessage.append("\n  ");
                        appendEvent(this.mViolationMessage, i + 1, event, this.mRecentEventsUnhandled[index]);
                    }
                }
                Log.d(this.mLogTag, this.mViolationMessage.toString());
                this.mCurrentEvent.setTainted(true);
            }
            this.mViolationMessage.setLength(0);
        }
        if (this.mRecentEvents == null) {
            this.mRecentEvents = new InputEvent[5];
            this.mRecentEventsUnhandled = new boolean[5];
        }
        index = (this.mMostRecentEventIndex + 1) % 5;
        this.mMostRecentEventIndex = index;
        if (this.mRecentEvents[index] != null) {
            this.mRecentEvents[index].recycle();
        }
        this.mRecentEvents[index] = this.mCurrentEvent.copy();
        this.mRecentEventsUnhandled[index] = false;
        this.mCurrentEvent = null;
        this.mCurrentEventType = null;
    }

    private static void appendEvent(StringBuilder message, int index, InputEvent event, boolean unhandled) {
        message.append(index).append(": sent at ").append(event.getEventTimeNano());
        message.append(", ");
        if (unhandled) {
            message.append("(unhandled) ");
        }
        message.append(event);
    }

    private void problem(String message) {
        if (this.mViolationMessage == null) {
            this.mViolationMessage = new StringBuilder();
        }
        if (this.mViolationMessage.length() == 0) {
            this.mViolationMessage.append(this.mCurrentEventType).append(": ");
        } else {
            this.mViolationMessage.append("\n  ");
        }
        this.mViolationMessage.append(message);
    }

    private KeyState findKeyState(int deviceId, int source, int keyCode, boolean remove) {
        KeyState last = null;
        KeyState state = this.mKeyStateList;
        while (state != null) {
            if (state.deviceId == deviceId && state.source == source && state.keyCode == keyCode) {
                if (remove) {
                    if (last != null) {
                        last.next = state.next;
                    } else {
                        this.mKeyStateList = state.next;
                    }
                    state.next = null;
                }
                return state;
            }
            last = state;
            state = state.next;
        }
        return null;
    }

    private void addKeyState(int deviceId, int source, int keyCode) {
        KeyState state = KeyState.obtain(deviceId, source, keyCode);
        state.next = this.mKeyStateList;
        this.mKeyStateList = state;
    }
}
