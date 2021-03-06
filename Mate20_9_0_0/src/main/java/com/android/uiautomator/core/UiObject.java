package com.android.uiautomator.core;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent.PointerCoords;
import android.view.accessibility.AccessibilityNodeInfo;

@Deprecated
public class UiObject {
    protected static final int FINGER_TOUCH_HALF_WIDTH = 20;
    private static final String LOG_TAG = UiObject.class.getSimpleName();
    protected static final int SWIPE_MARGIN_LIMIT = 5;
    @Deprecated
    protected static final long WAIT_FOR_EVENT_TMEOUT = 3000;
    protected static final long WAIT_FOR_SELECTOR_POLL = 1000;
    @Deprecated
    protected static final long WAIT_FOR_SELECTOR_TIMEOUT = 10000;
    protected static final long WAIT_FOR_WINDOW_TMEOUT = 5500;
    private final Configurator mConfig = Configurator.getInstance();
    private final UiSelector mSelector;

    public UiObject(UiSelector selector) {
        this.mSelector = selector;
    }

    public final UiSelector getSelector() {
        Tracer.trace(new Object[0]);
        return new UiSelector(this.mSelector);
    }

    QueryController getQueryController() {
        return UiDevice.getInstance().getAutomatorBridge().getQueryController();
    }

    InteractionController getInteractionController() {
        return UiDevice.getInstance().getAutomatorBridge().getInteractionController();
    }

    public UiObject getChild(UiSelector selector) throws UiObjectNotFoundException {
        Tracer.trace(selector);
        return new UiObject(getSelector().childSelector(selector));
    }

    public UiObject getFromParent(UiSelector selector) throws UiObjectNotFoundException {
        Tracer.trace(selector);
        return new UiObject(getSelector().fromParent(selector));
    }

    public int getChildCount() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.getChildCount();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    protected AccessibilityNodeInfo findAccessibilityNodeInfo(long timeout) {
        AccessibilityNodeInfo node = null;
        long startMills = SystemClock.uptimeMillis();
        long currentMills = 0;
        while (currentMills <= timeout) {
            node = getQueryController().findAccessibilityNodeInfo(getSelector());
            if (node != null) {
                break;
            }
            UiDevice.getInstance().runWatchers();
            currentMills = SystemClock.uptimeMillis() - startMills;
            if (timeout > 0) {
                SystemClock.sleep(WAIT_FOR_SELECTOR_POLL);
            }
        }
        return node;
    }

    public boolean dragTo(UiObject destObj, int steps) throws UiObjectNotFoundException {
        Rect srcRect = getVisibleBounds();
        Rect dstRect = destObj.getVisibleBounds();
        return getInteractionController().swipe(srcRect.centerX(), srcRect.centerY(), dstRect.centerX(), dstRect.centerY(), steps, true);
    }

    public boolean dragTo(int destX, int destY, int steps) throws UiObjectNotFoundException {
        Rect srcRect = getVisibleBounds();
        return getInteractionController().swipe(srcRect.centerX(), srcRect.centerY(), destX, destY, steps, true);
    }

    public boolean swipeUp(int steps) throws UiObjectNotFoundException {
        Tracer.trace(Integer.valueOf(steps));
        Rect rect = getVisibleBounds();
        if (rect.height() <= 10) {
            return false;
        }
        return getInteractionController().swipe(rect.centerX(), rect.bottom - 5, rect.centerX(), rect.top + SWIPE_MARGIN_LIMIT, steps);
    }

    public boolean swipeDown(int steps) throws UiObjectNotFoundException {
        Tracer.trace(Integer.valueOf(steps));
        Rect rect = getVisibleBounds();
        if (rect.height() <= 10) {
            return false;
        }
        return getInteractionController().swipe(rect.centerX(), rect.top + SWIPE_MARGIN_LIMIT, rect.centerX(), rect.bottom - 5, steps);
    }

    public boolean swipeLeft(int steps) throws UiObjectNotFoundException {
        Tracer.trace(Integer.valueOf(steps));
        Rect rect = getVisibleBounds();
        if (rect.width() <= 10) {
            return false;
        }
        return getInteractionController().swipe(rect.right - 5, rect.centerY(), rect.left + SWIPE_MARGIN_LIMIT, rect.centerY(), steps);
    }

    public boolean swipeRight(int steps) throws UiObjectNotFoundException {
        Tracer.trace(Integer.valueOf(steps));
        Rect rect = getVisibleBounds();
        if (rect.width() <= 10) {
            return false;
        }
        return getInteractionController().swipe(rect.left + SWIPE_MARGIN_LIMIT, rect.centerY(), rect.right - 5, rect.centerY(), steps);
    }

    private Rect getVisibleBounds(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }
        int w = UiDevice.getInstance().getDisplayWidth();
        int h = UiDevice.getInstance().getDisplayHeight();
        Rect nodeRect = AccessibilityNodeInfoHelper.getVisibleBoundsInScreen(node, w, h);
        AccessibilityNodeInfo scrollableParentNode = getScrollableParent(node);
        if (scrollableParentNode == null || nodeRect.intersect(AccessibilityNodeInfoHelper.getVisibleBoundsInScreen(scrollableParentNode, w, h))) {
            return nodeRect;
        }
        return new Rect();
    }

    private AccessibilityNodeInfo getScrollableParent(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo parent = node;
        while (parent != null) {
            parent = parent.getParent();
            if (parent != null && parent.isScrollable()) {
                return parent;
            }
        }
        return null;
    }

    public boolean click() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            return getInteractionController().clickAndSync(rect.centerX(), rect.centerY(), this.mConfig.getActionAcknowledgmentTimeout());
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean clickAndWaitForNewWindow() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        return clickAndWaitForNewWindow(WAIT_FOR_WINDOW_TMEOUT);
    }

    public boolean clickAndWaitForNewWindow(long timeout) throws UiObjectNotFoundException {
        Tracer.trace(Long.valueOf(timeout));
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            return getInteractionController().clickAndWaitForNewWindow(rect.centerX(), rect.centerY(), this.mConfig.getActionAcknowledgmentTimeout());
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean clickTopLeft() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            return getInteractionController().clickNoSync(rect.left + SWIPE_MARGIN_LIMIT, rect.top + SWIPE_MARGIN_LIMIT);
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean longClickBottomRight() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            return getInteractionController().longTapNoSync(rect.right - 5, rect.bottom - 5);
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean clickBottomRight() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            return getInteractionController().clickNoSync(rect.right - 5, rect.bottom - 5);
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean longClick() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            return getInteractionController().longTapNoSync(rect.centerX(), rect.centerY());
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean longClickTopLeft() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            return getInteractionController().longTapNoSync(rect.left + SWIPE_MARGIN_LIMIT, rect.top + SWIPE_MARGIN_LIMIT);
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public String getText() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            String retVal = safeStringReturn(node.getText());
            Log.d(LOG_TAG, String.format("getText() = %s", new Object[]{retVal}));
            return retVal;
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public String getClassName() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            String retVal = safeStringReturn(node.getClassName());
            Log.d(LOG_TAG, String.format("getClassName() = %s", new Object[]{retVal}));
            return retVal;
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public String getContentDescription() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return safeStringReturn(node.getContentDescription());
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean setText(String text) throws UiObjectNotFoundException {
        Tracer.trace(text);
        clearTextField();
        return getInteractionController().sendText(text);
    }

    public void clearTextField() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            getInteractionController().longTapNoSync(rect.left + FINGER_TOUCH_HALF_WIDTH, rect.centerY());
            UiObject selectAll = new UiObject(new UiSelector().descriptionContains("Select all"));
            if (selectAll.waitForExists(50)) {
                selectAll.click();
            }
            SystemClock.sleep(250);
            getInteractionController().sendKey(67, 0);
            return;
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isChecked() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isChecked();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isSelected() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isSelected();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isCheckable() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isCheckable();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isEnabled() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isEnabled();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isClickable() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isClickable();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isFocused() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isFocused();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isFocusable() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isFocusable();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isScrollable() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isScrollable();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isLongClickable() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isLongClickable();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public String getPackageName() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return safeStringReturn(node.getPackageName());
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public Rect getVisibleBounds() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return getVisibleBounds(node);
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public Rect getBounds() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect nodeRect = new Rect();
            node.getBoundsInScreen(nodeRect);
            return nodeRect;
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean waitForExists(long timeout) {
        Tracer.trace(Long.valueOf(timeout));
        return findAccessibilityNodeInfo(timeout) != null;
    }

    public boolean waitUntilGone(long timeout) {
        Tracer.trace(Long.valueOf(timeout));
        long startMills = SystemClock.uptimeMillis();
        long currentMills = 0;
        while (currentMills <= timeout) {
            if (findAccessibilityNodeInfo(0) == null) {
                return true;
            }
            currentMills = SystemClock.uptimeMillis() - startMills;
            if (timeout > 0) {
                SystemClock.sleep(WAIT_FOR_SELECTOR_POLL);
            }
        }
        return false;
    }

    public boolean exists() {
        Tracer.trace(new Object[0]);
        return waitForExists(0);
    }

    private String safeStringReturn(CharSequence cs) {
        if (cs == null) {
            return "";
        }
        return cs.toString();
    }

    public boolean pinchOut(int percent, int steps) throws UiObjectNotFoundException {
        int i = 100;
        if (percent < 0) {
            i = 1;
        } else if (percent <= 100) {
            i = percent;
        }
        float percentage = ((float) i) / 100.0f;
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            if (rect.width() > 40) {
                return performTwoPointerGesture(new Point(rect.centerX() - 20, rect.centerY()), new Point(rect.centerX() + FINGER_TOUCH_HALF_WIDTH, rect.centerY()), new Point(rect.centerX() - ((int) (((float) (rect.width() / 2)) * percentage)), rect.centerY()), new Point(rect.centerX() + ((int) (((float) (rect.width() / 2)) * percentage)), rect.centerY()), steps);
            }
            throw new IllegalStateException("Object width is too small for operation");
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean pinchIn(int percent, int steps) throws UiObjectNotFoundException {
        int i = 100;
        if (percent < 0) {
            i = 0;
        } else if (percent <= 100) {
            i = percent;
        }
        float percentage = ((float) i) / 100.0f;
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            if (rect.width() > 40) {
                return performTwoPointerGesture(new Point(rect.centerX() - ((int) (((float) (rect.width() / 2)) * percentage)), rect.centerY()), new Point(rect.centerX() + ((int) (((float) (rect.width() / 2)) * percentage)), rect.centerY()), new Point(rect.centerX() - 20, rect.centerY()), new Point(rect.centerX() + FINGER_TOUCH_HALF_WIDTH, rect.centerY()), steps);
            }
            throw new IllegalStateException("Object width is too small for operation");
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean performTwoPointerGesture(Point startPoint1, Point startPoint2, Point endPoint1, Point endPoint2, int steps) {
        int steps2;
        PointerCoords p1;
        PointerCoords p2;
        Point point = startPoint1;
        Point point2 = startPoint2;
        Point point3 = endPoint1;
        Point point4 = endPoint2;
        if (steps == 0) {
            steps2 = 1;
        } else {
            steps2 = steps;
        }
        float stepX1 = (float) ((point3.x - point.x) / steps2);
        float stepY1 = (float) ((point3.y - point.y) / steps2);
        float stepX2 = (float) ((point4.x - point2.x) / steps2);
        float stepY2 = (float) ((point4.y - point2.y) / steps2);
        int eventX1 = point.x;
        int eventY1 = point.y;
        int eventX2 = point2.x;
        PointerCoords[] points1 = new PointerCoords[(steps2 + 2)];
        PointerCoords[] points2 = new PointerCoords[(steps2 + 2)];
        int eventY2 = point2.y;
        int eventX22 = eventX2;
        eventX2 = eventY1;
        eventY1 = eventX1;
        eventX1 = 0;
        while (eventX1 < steps2 + 1) {
            p1 = new PointerCoords();
            p1.x = (float) eventY1;
            p1.y = (float) eventX2;
            p1.pressure = 1.0f;
            p1.size = 1.0f;
            points1[eventX1] = p1;
            p2 = new PointerCoords();
            p2.x = (float) eventX22;
            p2.y = (float) eventY2;
            p2.pressure = 1.0f;
            p2.size = 1.0f;
            points2[eventX1] = p2;
            eventY1 = (int) (((float) eventY1) + stepX1);
            eventX2 = (int) (((float) eventX2) + stepY1);
            eventX22 = (int) (((float) eventX22) + stepX2);
            eventY2 = (int) (((float) eventY2) + stepY2);
            eventX1++;
            point = startPoint1;
            point2 = startPoint2;
        }
        p1 = new PointerCoords();
        p1.x = (float) point3.x;
        p1.y = (float) point3.y;
        p1.pressure = 1.0f;
        p1.size = 1.0f;
        points1[steps2 + 1] = p1;
        p2 = new PointerCoords();
        p2.x = (float) point4.x;
        p2.y = (float) point4.y;
        p2.pressure = 1.0f;
        p2.size = 1.0f;
        points2[steps2 + 1] = p2;
        return performMultiPointerGesture(points1, points2);
    }

    public boolean performMultiPointerGesture(PointerCoords[]... touches) {
        return getInteractionController().performMultiPointerGesture(touches);
    }
}
