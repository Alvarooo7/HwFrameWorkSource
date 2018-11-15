package com.android.server.devicepolicy;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.server.slice.SliceClientPermissions.SliceAuthority;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PolicyStruct {
    private static final int MAX_PATH_LENGTH = 200;
    private static final String PATH_REGEX = "^[_/]?[A-Za-z0-9]+([-_/]?[A-Za-z0-9]+)*/?$";
    private static final String TAG = PolicyStruct.class.getSimpleName();
    private DevicePolicyPlugin mOwner = null;
    private ArrayMap<String, PolicyItem> policies = new ArrayMap();

    public static class PolicyItem {
        public static final int GLOBAL_POLICY_CHANGED = 1;
        public static final int GLOBAL_POLICY_NOT_SET = 0;
        public static final int GLOBAL_POLICY_NO_CHANGE = 2;
        public Bundle attributes = new Bundle();
        public int globalPolicyChanged = 0;
        public PolicyType itemType;
        public ArrayList<PolicyItem> leafItems = new ArrayList();
        public String policyName;

        public PolicyItem(String name) {
            this.policyName = name;
        }

        public PolicyItem(String policyName, PolicyType itemType) {
            if (policyName != null && policyName.endsWith(SliceAuthority.DELIMITER)) {
                policyName = policyName.substring(0, policyName.length() - 1);
            }
            this.policyName = policyName;
            this.itemType = itemType;
        }

        public boolean isGlobalPolicySet() {
            return this.globalPolicyChanged != 0;
        }

        public boolean isGlobalPolicyChanged() {
            return this.globalPolicyChanged == 1;
        }

        public void setGlobalPolicyChanged(int changeState) {
            this.globalPolicyChanged = changeState;
        }

        public String getPolicyName() {
            return this.policyName;
        }

        public String getPolicyTag() {
            if (TextUtils.isEmpty(this.policyName)) {
                return null;
            }
            String policyTag;
            if (this.policyName.indexOf(SliceAuthority.DELIMITER) == -1 || this.policyName.endsWith(SliceAuthority.DELIMITER)) {
                policyTag = this.policyName;
            } else {
                policyTag = this.policyName.substring(this.policyName.lastIndexOf(47) + 1, this.policyName.length());
            }
            return policyTag;
        }

        public PolicyType getItemType() {
            return this.itemType;
        }

        public Bundle getAttributes() {
            if (this.attributes == null) {
                this.attributes = new Bundle();
            }
            return this.attributes;
        }

        public Set<String> getAttrNames() {
            if (this.attributes == null) {
                return null;
            }
            return this.attributes.keySet();
        }

        public ArrayList<PolicyItem> getChildItem() {
            if (this.leafItems == null) {
                this.leafItems = new ArrayList();
            }
            return this.leafItems;
        }

        public void setChildItem(PolicyItem child) {
            if (this.leafItems == null) {
                this.leafItems = new ArrayList();
            }
            this.leafItems.add(child);
        }

        public void setAttributes(Bundle attributes) {
            this.attributes = attributes;
        }

        public void copyFrom(PolicyItem otherItem) {
            this.policyName = otherItem.getPolicyName();
            this.itemType = otherItem.getItemType();
            Iterator it = otherItem.getAttributes().keySet().iterator();
            while (true) {
                ArrayList<PolicyItem> leafItems = null;
                if (it.hasNext()) {
                    String key = (String) it.next();
                    switch (otherItem.getItemType()) {
                        case STATE:
                            getAttributes().putBoolean(key, false);
                            break;
                        case CONFIGURATION:
                            getAttributes().putString(key, null);
                            break;
                        case LIST:
                            getAttributes().putStringArrayList(key, null);
                            break;
                        default:
                            break;
                    }
                }
                while (true) {
                    if (otherItem.hasLeafItems()) {
                        leafItems = otherItem.getChildItem();
                        Iterator it2 = leafItems.iterator();
                        while (it2.hasNext()) {
                            PolicyItem leaf = (PolicyItem) it2.next();
                            PolicyItem newItem = new PolicyItem(leaf.getPolicyName());
                            setChildItem(newItem);
                            otherItem = leaf;
                            newItem.copyFrom(leaf);
                        }
                    } else {
                        return;
                    }
                }
            }
        }

        public void deepCopyFrom(PolicyItem otherItem) {
            this.policyName = otherItem.getPolicyName();
            this.itemType = otherItem.getItemType();
            for (String key : otherItem.getAttributes().keySet()) {
                switch (otherItem.getItemType()) {
                    case STATE:
                        getAttributes().putBoolean(key, otherItem.getAttributes().getBoolean(key));
                        break;
                    case CONFIGURATION:
                        getAttributes().putString(key, otherItem.getAttributes().getString(key));
                        break;
                    case LIST:
                        getAttributes().putStringArrayList(key, otherItem.getAttributes().getStringArrayList(key));
                        break;
                    default:
                        break;
                }
            }
            while (otherItem.hasLeafItems()) {
                Iterator it = otherItem.getChildItem().iterator();
                while (it.hasNext()) {
                    PolicyItem leaf = (PolicyItem) it.next();
                    PolicyItem newItem = new PolicyItem(leaf.getPolicyName());
                    setChildItem(newItem);
                    otherItem = leaf;
                    newItem.deepCopyFrom(leaf);
                }
            }
        }

        public Bundle combineAllAttributes() {
            Bundle bundle = new Bundle();
            combineAttributes(bundle);
            return bundle;
        }

        private void combineAttributes(Bundle bundle) {
            ArrayList<PolicyItem> leafItems;
            Bundle oldBundle = getAttributes();
            Iterator it = oldBundle.keySet().iterator();
            while (true) {
                leafItems = null;
                if (!it.hasNext()) {
                    break;
                }
                String key = (String) it.next();
                Object obj = oldBundle.get(key);
                if (obj instanceof ArrayList) {
                    bundle.putStringArrayList(key, (ArrayList) obj);
                } else if (obj instanceof String) {
                    bundle.putString(key, (String) obj);
                } else if (obj instanceof Boolean) {
                    bundle.putBoolean(key, ((Boolean) obj).booleanValue());
                } else {
                    bundle.putString(key, null);
                }
            }
            PolicyItem item = this;
            while (true) {
                if (item.hasLeafItems()) {
                    leafItems = getChildItem();
                    Iterator it2 = leafItems.iterator();
                    while (it2.hasNext()) {
                        PolicyItem leaf = (PolicyItem) it2.next();
                        item = leaf;
                        leaf.combineAttributes(bundle);
                    }
                } else {
                    return;
                }
            }
        }

        public void updateAttrValues(Bundle newData) {
            if (newData != null && !newData.isEmpty()) {
                PolicyItem item = this;
                Bundle attributes = item.getAttributes();
                for (String attrName : attributes.keySet()) {
                    if (newData.get(attrName) != null) {
                        Object value = newData.get(attrName);
                        switch (getItemType()) {
                            case STATE:
                                attributes.putBoolean(attrName, newData.getBoolean(attrName));
                                break;
                            case CONFIGURATION:
                                attributes.putString(attrName, newData.getString(attrName));
                                break;
                            case LIST:
                                attributes.putStringArrayList(attrName, newData.getStringArrayList(attrName));
                                break;
                        }
                    }
                }
                while (item.hasLeafItems()) {
                    Iterator it = item.getChildItem().iterator();
                    while (it.hasNext()) {
                        item = (PolicyItem) it.next();
                        item.updateAttrValues(newData);
                    }
                }
            }
        }

        public void addAttrValues(PolicyItem rootItem, Bundle newData) {
            if (newData != null && !newData.isEmpty() && rootItem != null) {
                Bundle attributes = rootItem.getAttributes();
                for (String attrName : attributes.keySet()) {
                    switch (getItemType()) {
                        case STATE:
                            attributes.putBoolean(attrName, newData.getBoolean(attrName));
                            break;
                        case CONFIGURATION:
                            attributes.putString(attrName, newData.getString(attrName));
                            break;
                        case LIST:
                            ArrayList<String> originalList = attributes.getStringArrayList(attrName);
                            if (originalList == null) {
                                originalList = new ArrayList();
                            }
                            addListWithoutDuplicate(originalList, newData.getStringArrayList(attrName));
                            attributes.putStringArrayList(attrName, originalList);
                            break;
                        default:
                            break;
                    }
                }
                Iterator it = rootItem.getChildItem().iterator();
                while (it.hasNext()) {
                    PolicyItem child = (PolicyItem) it.next();
                    child.addAttrValues(child, newData);
                }
            }
        }

        public void addListWithoutDuplicate(List<String> originalList, List<String> addList) {
            if (addList != null && originalList != null) {
                Set<String> set = new HashSet(originalList);
                for (String str : addList) {
                    if (!TextUtils.isEmpty(str) && set.add(str)) {
                        originalList.add(str);
                    }
                }
            }
        }

        public void removeAttrValues(PolicyItem rootItem, Bundle newData) {
            if (newData != null && !newData.isEmpty() && rootItem != null) {
                Bundle attributes = rootItem.getAttributes();
                for (String attrName : attributes.keySet()) {
                    switch (getItemType()) {
                        case STATE:
                            attributes.putBoolean(attrName, false);
                            break;
                        case CONFIGURATION:
                            attributes.putString(attrName, null);
                            break;
                        case LIST:
                            ArrayList<String> originalList = attributes.getStringArrayList(attrName);
                            if (originalList == null) {
                                originalList = new ArrayList();
                            }
                            removeItemsFromList(originalList, newData.getStringArrayList(attrName));
                            break;
                        default:
                            break;
                    }
                }
                Iterator it = rootItem.getChildItem().iterator();
                while (it.hasNext()) {
                    PolicyItem child = (PolicyItem) it.next();
                    child.removeAttrValues(child, newData);
                }
            }
        }

        public void removeItemsFromList(List<String> originalList, List<String> removeList) {
            if (originalList != null && removeList != null) {
                Set<String> removeSet = new HashSet(removeList);
                List<String> newList = new ArrayList();
                for (String str : originalList) {
                    if (!removeSet.contains(str)) {
                        newList.add(str);
                    }
                }
                originalList.clear();
                originalList.addAll(newList);
            }
        }

        public void addAttributes(String... attrNames) {
            if (attrNames != null && attrNames.length != 0) {
                for (String attrName : attrNames) {
                    addAttribute(attrName, null);
                }
            }
        }

        public String getAttrValue(String attrName) {
            if (this.attributes == null) {
                return null;
            }
            return this.attributes.getString(attrName);
        }

        public void addAttribute(String attrName, String attrValue) {
            if (this.attributes == null) {
                this.attributes = new Bundle();
            }
            this.attributes.putString(attrName, attrValue);
        }

        public void removeAttrByName(String attrName) {
            if (this.attributes != null) {
                this.attributes.remove(attrName);
            }
        }

        public boolean isAttrValueExists(String attrName) {
            boolean z = false;
            if (this.attributes == null) {
                return false;
            }
            if (this.attributes.get(attrName) != null) {
                z = true;
            }
            return z;
        }

        public boolean isAttrExists(String attrName) {
            if (this.attributes != null) {
                for (String name : getAttrNames()) {
                    if (name != null && name.equals(attrName)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean hasAnyAttribute() {
            if (this.attributes == null) {
                return false;
            }
            return this.attributes.size() > 0;
        }

        public boolean hasAnyNonNullAttribute() {
            PolicyItem item = this;
            Bundle attrs = item.getAttributes();
            for (String key : attrs.keySet()) {
                if (attrs.get(key) != null) {
                    return true;
                }
            }
            while (item.hasLeafItems()) {
                Iterator it = item.getChildItem().iterator();
                while (it.hasNext()) {
                    item = (PolicyItem) it.next();
                    if (item.hasAnyNonNullAttribute()) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean hasLeafItems() {
            return this.leafItems != null && this.leafItems.size() > 0;
        }

        public boolean hasPolicyName() {
            return (getPolicyName() == null || getPolicyName().isEmpty()) ? false : true;
        }

        public boolean hasPolicyType() {
            return this.itemType != null;
        }

        public static boolean isValidItem(PolicyItem item) {
            if (item != null && item.hasPolicyName() && item.hasPolicyType()) {
                PolicyItem node = item;
                while (node.hasLeafItems()) {
                    Iterator it = node.getChildItem().iterator();
                    while (it.hasNext()) {
                        node = (PolicyItem) it.next();
                        if (!isValidItem(node)) {
                            return false;
                        }
                    }
                }
                return true;
            }
            String str;
            String access$000 = PolicyStruct.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("isValidItem- item not correct: ");
            if (item == null) {
                str = "item is null";
            } else if (!item.hasPolicyName()) {
                str = "policyName is null";
            } else if (item.hasPolicyType()) {
                str = " unbelievable result";
            } else {
                str = "policy type is null";
            }
            stringBuilder.append(str);
            HwLog.d(access$000, stringBuilder.toString());
            return false;
        }

        public void traverse(int level) {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < level; i++) {
                buffer.append("    ");
            }
            buffer.append("|..");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[");
            stringBuilder.append(level);
            stringBuilder.append("] ");
            buffer.append(stringBuilder.toString());
            buffer.append(getPolicyTag());
            stringBuilder = new StringBuilder();
            stringBuilder.append(", ");
            stringBuilder.append(getItemType() != null ? getItemType().name() : "no type");
            buffer.append(stringBuilder.toString());
            if (getAttributes().size() > 0) {
                buffer.append(", ");
                for (String attrName : getAttributes().keySet()) {
                    Object obj = getAttributes().get(attrName);
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("[");
                    stringBuilder2.append(attrName);
                    stringBuilder2.append(", ");
                    stringBuilder2.append(obj);
                    stringBuilder2.append("] ");
                    buffer.append(stringBuilder2.toString());
                }
            }
            HwLog.d(PolicyStruct.TAG, buffer.toString());
            Iterator it = getChildItem().iterator();
            while (it.hasNext()) {
                PolicyItem item = (PolicyItem) it.next();
                if (item != null) {
                    item.traverse(level + 1);
                } else {
                    return;
                }
            }
        }

        public boolean hasTheSameAttrStruct(PolicyItem that) {
            if (this == that) {
                return true;
            }
            if (!this.policyName == null ? this.policyName.equals(that.policyName) : that.policyName == null) {
                return false;
            }
            if (this.itemType != that.itemType) {
                return false;
            }
            if ((this.attributes != null && that.attributes == null) || (this.attributes == null && that.attributes != null)) {
                return false;
            }
            if (this.attributes != null && this.attributes.size() != that.attributes.size()) {
                return false;
            }
            if (this.attributes != null) {
                for (String thatAttrName : that.attributes.keySet()) {
                    if (!this.attributes.containsKey(thatAttrName)) {
                        return false;
                    }
                }
            }
            ArrayList<PolicyItem> thisItems = getChildItem();
            ArrayList<PolicyItem> thatItems = that.getChildItem();
            if (thisItems.size() != thatItems.size()) {
                return false;
            }
            PolicyItem thisParent = this;
            PolicyItem thatParent = that;
            while (thisParent.hasLeafItems() && thatParent.hasLeafItems()) {
                Iterator it = thisItems.iterator();
                while (it.hasNext()) {
                    PolicyItem thisItem = (PolicyItem) it.next();
                    boolean found = false;
                    int i = 0;
                    while (i < thatItems.size()) {
                        if (thisItem.getPolicyName().equals(((PolicyItem) thatItems.get(i)).getPolicyName())) {
                            thisParent = thisItem;
                            thatParent = (PolicyItem) thatItems.get(i);
                            found = true;
                            if (!thisItem.hasTheSameAttrStruct((PolicyItem) thatItems.get(i))) {
                                return false;
                            }
                            if (i != thatItems.size() - 1 && !found) {
                                return false;
                            }
                        } else {
                            i++;
                        }
                    }
                    if (i != thatItems.size() - 1) {
                    }
                }
            }
            return true;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            PolicyItem that = (PolicyItem) other;
            if (!hasTheSameAttrStruct(that)) {
                return false;
            }
            if (this.attributes != null) {
                for (String attrName : this.attributes.keySet()) {
                    switch (this.itemType) {
                        case STATE:
                            if (this.attributes.getBoolean(attrName) == that.attributes.getBoolean(attrName)) {
                                break;
                            }
                            return false;
                        case CONFIGURATION:
                            String thisAttrValue = this.attributes.getString(attrName);
                            String thatAttrValue = that.attributes.getString(attrName);
                            if (thisAttrValue == null) {
                                if (thatAttrValue == null) {
                                    break;
                                }
                            } else if (thisAttrValue.equals(thatAttrValue)) {
                                break;
                            }
                            return false;
                        case LIST:
                            ArrayList<String> thisList = this.attributes.getStringArrayList(attrName);
                            ArrayList<String> thatList = that.attributes.getStringArrayList(attrName);
                            if (thisList == null) {
                                if (thatList == null) {
                                    break;
                                }
                            } else if (thisList.equals(thatList)) {
                                break;
                            }
                            return false;
                        default:
                            break;
                    }
                }
            }
            ArrayList<PolicyItem> thisItems = getChildItem();
            ArrayList<PolicyItem> thatItems = that.getChildItem();
            if (thisItems.size() != thatItems.size()) {
                return false;
            }
            PolicyItem thisParent = this;
            PolicyItem thatParent = that;
            while (thisParent.hasLeafItems() && thatParent.hasLeafItems()) {
                Iterator it = thisItems.iterator();
                while (it.hasNext()) {
                    PolicyItem thisItem = (PolicyItem) it.next();
                    boolean found = false;
                    int i = 0;
                    while (i < thatItems.size()) {
                        if (thisItem.getPolicyName().equals(((PolicyItem) thatItems.get(i)).getPolicyName())) {
                            thisParent = thisItem;
                            thatParent = (PolicyItem) thatItems.get(i);
                            found = true;
                            if (!thisItem.equals(thatItems.get(i))) {
                                return false;
                            }
                            if (i != thatItems.size() - 1 && !found) {
                                return false;
                            }
                        } else {
                            i++;
                        }
                    }
                    if (i != thatItems.size() - 1) {
                    }
                }
            }
            return true;
        }

        public int hashCode() {
            int i = 0;
            int result = 31 * (this.policyName != null ? this.policyName.hashCode() : 0);
            if (this.itemType != null) {
                i = this.itemType.hashCode();
            }
            return result + i;
        }
    }

    public enum PolicyType {
        STATE,
        LIST,
        CONFIGURATION
    }

    public PolicyStruct(DevicePolicyPlugin owner) {
        this.mOwner = owner;
    }

    public PolicyStruct(DevicePolicyPlugin owner, PolicyType type, String path, String... attributes) {
        this.mOwner = owner;
        addStruct(path, type, attributes);
    }

    public boolean hasPolicy() {
        return this.policies != null && this.policies.size() > 0;
    }

    public PolicyItem getPolicyItem(String policyName) {
        if (this.policies == null) {
            this.policies = new ArrayMap();
        }
        return (PolicyItem) this.policies.get(policyName);
    }

    public boolean addPolicyItem(PolicyItem item) {
        if (this.policies == null) {
            this.policies = new ArrayMap();
        }
        if (!PolicyItem.isValidItem(item)) {
            return false;
        }
        this.policies.put(item.getPolicyName(), item);
        return true;
    }

    public DevicePolicyPlugin getOwner() {
        return this.mOwner;
    }

    public boolean containsPolicyName(String name) {
        if (this.policies == null || !this.policies.containsKey(name)) {
            return false;
        }
        return true;
    }

    public ArrayMap<String, PolicyItem> getPolicyMap() {
        if (this.policies == null) {
            this.policies = new ArrayMap();
        }
        return this.policies;
    }

    public static PolicyItem createTree(String path, PolicyType type, String... attrNames) {
        PolicyItem root;
        if (path == null || path.isEmpty() || type == null || !path.matches(PATH_REGEX) || path.length() > 200) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("createTree-path is invalid: ");
            stringBuilder.append(path);
            HwLog.e(str, stringBuilder.toString());
            return null;
        } else if (path.indexOf(SliceAuthority.DELIMITER) == -1) {
            root = new PolicyItem(path, type);
            root.addAttributes(attrNames);
            return root;
        } else if (path.equals(SliceAuthority.DELIMITER)) {
            return null;
        } else {
            if (path.startsWith(SliceAuthority.DELIMITER)) {
                path = path.substring(1);
                if (path.contains(SliceAuthority.DELIMITER)) {
                    root = new PolicyItem(path.substring(0, path.indexOf(SliceAuthority.DELIMITER)), type);
                } else {
                    root = new PolicyItem(path, type);
                }
            } else {
                root = new PolicyItem(path.substring(0, path.indexOf(SliceAuthority.DELIMITER)), type);
            }
            PolicyItem node = root;
            for (int i = path.indexOf(SliceAuthority.DELIMITER) + 1; i < path.length(); i++) {
                if (path.charAt(i) == '/') {
                    PolicyItem newItem = new PolicyItem(path.substring(0, i), type);
                    node.setChildItem(newItem);
                    node = newItem;
                }
            }
            if (path.lastIndexOf(SliceAuthority.DELIMITER) != path.length() - 1) {
                PolicyItem lastNode = new PolicyItem(path, type);
                lastNode.addAttributes(attrNames);
                node.setChildItem(lastNode);
            } else {
                node.addAttributes(attrNames);
            }
            return root;
        }
    }

    public Collection<PolicyItem> getPolicyItems() {
        if (this.policies == null || this.policies.isEmpty()) {
            return null;
        }
        return this.policies.values();
    }

    public PolicyItem getItemByPolicyName(String policyName) {
        if (containsPolicyName(policyName)) {
            return (PolicyItem) this.policies.get(policyName);
        }
        return null;
    }

    public Bundle getAttributesByPolicyName(String policyName) {
        PolicyItem item = getItemByPolicyName(policyName);
        if (item != null) {
            return item.getAttributes();
        }
        return null;
    }

    public void updateAttrValues(String policyName, Bundle newData) {
        PolicyItem item = getItemByPolicyName(policyName);
        if (item != null) {
            boolean hasNonNullAttrValue = false;
            if (newData != null && !newData.isEmpty()) {
                for (String attrName : newData.keySet()) {
                    if (newData.get(attrName) != null) {
                        hasNonNullAttrValue = true;
                        break;
                    }
                }
            }
            if (hasNonNullAttrValue) {
                item.updateAttrValues(newData);
                return;
            }
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("updateAttrValues: ");
            stringBuilder.append(item.getPolicyName());
            stringBuilder.append(" nothing to update");
            HwLog.d(str, stringBuilder.toString());
        }
    }

    /* JADX WARNING: Missing block: B:35:0x00a3, code:
            if (r4.hasLeafItems() == false) goto L_0x00ab;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public PolicyItem addStruct(String path, PolicyType type, String... attrNames) {
        PolicyItem createdRoot = createTree(path, type, attrNames);
        if (createdRoot == null) {
            HwLog.e(TAG, "add struct error");
            return null;
        }
        PolicyItem oldRoot = getPolicyItem(createdRoot.getPolicyName());
        if (oldRoot == null) {
            addPolicyItem(createdRoot);
            return createdRoot;
        }
        if (!oldRoot.hasAnyAttribute() || createdRoot.hasAnyAttribute()) {
            oldRoot.setAttributes(createdRoot.getAttributes());
        }
        PolicyItem otherItem = createdRoot;
        PolicyItem otherParentItem = createdRoot;
        PolicyItem thisParentItem = oldRoot;
        PolicyItem thisItem = oldRoot;
        int sameDepth = 1;
        do {
            Iterator it = otherItem.getChildItem().iterator();
            while (true) {
                int i = 0;
                if (!it.hasNext()) {
                    break;
                }
                PolicyItem createdItem = (PolicyItem) it.next();
                while (i < thisItem.getChildItem().size()) {
                    PolicyItem oldItem = (PolicyItem) thisItem.getChildItem().get(i);
                    if (oldItem.getPolicyName().equals(createdItem.getPolicyName())) {
                        sameDepth++;
                        if (!oldItem.hasAnyAttribute() || createdItem.hasAnyAttribute()) {
                            oldItem.setAttributes(createdItem.getAttributes());
                        }
                        thisParentItem = thisItem;
                        thisItem = oldItem;
                        otherParentItem = otherItem;
                        otherItem = createdItem;
                    } else if (i == thisItem.getChildItem().size() - 1) {
                        thisParentItem.setChildItem(createdItem);
                        return oldRoot;
                    } else {
                        i++;
                    }
                }
            }
        } while (otherItem.hasLeafItems());
        int SingleLinkDepth = 1;
        PolicyItem sL = createdRoot;
        while (sL.hasLeafItems()) {
            sL = (PolicyItem) sL.getChildItem().get(0);
            SingleLinkDepth++;
        }
        if (sameDepth < SingleLinkDepth) {
            thisParentItem.setChildItem((PolicyItem) otherParentItem.getChildItem().get(0));
        }
        return oldRoot;
    }
}
