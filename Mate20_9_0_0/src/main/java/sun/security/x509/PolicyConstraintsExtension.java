package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class PolicyConstraintsExtension extends Extension implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.PolicyConstraints";
    public static final String INHIBIT = "inhibit";
    public static final String NAME = "PolicyConstraints";
    public static final String REQUIRE = "require";
    private static final byte TAG_INHIBIT = (byte) 1;
    private static final byte TAG_REQUIRE = (byte) 0;
    private int inhibit;
    private int require;

    private void encodeThis() throws IOException {
        if (this.require == -1 && this.inhibit == -1) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream tagged = new DerOutputStream();
        DerOutputStream seq = new DerOutputStream();
        if (this.require != -1) {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putInteger(this.require);
            tagged.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, false, (byte) 0), tmp);
        }
        if (this.inhibit != -1) {
            DerOutputStream tmp2 = new DerOutputStream();
            tmp2.putInteger(this.inhibit);
            tagged.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, false, (byte) 1), tmp2);
        }
        seq.write((byte) 48, tagged);
        this.extensionValue = seq.toByteArray();
    }

    public PolicyConstraintsExtension(int require, int inhibit) throws IOException {
        this(Boolean.FALSE, require, inhibit);
    }

    public PolicyConstraintsExtension(Boolean critical, int require, int inhibit) throws IOException {
        this.require = -1;
        this.inhibit = -1;
        this.require = require;
        this.inhibit = inhibit;
        this.extensionId = PKIXExtensions.PolicyConstraints_Id;
        this.critical = critical.booleanValue();
        encodeThis();
    }

    public PolicyConstraintsExtension(Boolean critical, Object value) throws IOException {
        this.require = -1;
        this.inhibit = -1;
        this.extensionId = PKIXExtensions.PolicyConstraints_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag == (byte) 48) {
            DerInputStream in = val.data;
            while (in != null && in.available() != 0) {
                DerValue next = in.getDerValue();
                if (!next.isContextSpecific((byte) 0) || next.isConstructed()) {
                    if (!next.isContextSpecific((byte) 1) || next.isConstructed()) {
                        throw new IOException("Invalid encoding of PolicyConstraint");
                    } else if (this.inhibit == -1) {
                        next.resetTag((byte) 2);
                        this.inhibit = next.getInteger();
                    } else {
                        throw new IOException("Duplicate inhibitPolicyMappingfound in the PolicyConstraintsExtension");
                    }
                } else if (this.require == -1) {
                    next.resetTag((byte) 2);
                    this.require = next.getInteger();
                } else {
                    throw new IOException("Duplicate requireExplicitPolicyfound in the PolicyConstraintsExtension");
                }
            }
            return;
        }
        throw new IOException("Sequence tag missing for PolicyConstraint.");
    }

    public String toString() {
        StringBuilder stringBuilder;
        String s = new StringBuilder();
        s.append(super.toString());
        s.append("PolicyConstraints: [  Require: ");
        s = s.toString();
        if (this.require == -1) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(s);
            stringBuilder.append("unspecified;");
            s = stringBuilder.toString();
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append(s);
            stringBuilder.append(this.require);
            stringBuilder.append(";");
            s = stringBuilder.toString();
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append(s);
        stringBuilder.append("\tInhibit: ");
        s = stringBuilder.toString();
        if (this.inhibit == -1) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(s);
            stringBuilder.append("unspecified");
            s = stringBuilder.toString();
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append(s);
            stringBuilder.append(this.inhibit);
            s = stringBuilder.toString();
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append(s);
        stringBuilder.append(" ]\n");
        return stringBuilder.toString();
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.PolicyConstraints_Id;
            this.critical = false;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (obj instanceof Integer) {
            if (name.equalsIgnoreCase(REQUIRE)) {
                this.require = ((Integer) obj).intValue();
            } else if (name.equalsIgnoreCase(INHIBIT)) {
                this.inhibit = ((Integer) obj).intValue();
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Attribute name [");
                stringBuilder.append(name);
                stringBuilder.append("] not recognized by CertAttrSet:PolicyConstraints.");
                throw new IOException(stringBuilder.toString());
            }
            encodeThis();
            return;
        }
        throw new IOException("Attribute value should be of type Integer.");
    }

    public Integer get(String name) throws IOException {
        if (name.equalsIgnoreCase(REQUIRE)) {
            return new Integer(this.require);
        }
        if (name.equalsIgnoreCase(INHIBIT)) {
            return new Integer(this.inhibit);
        }
        throw new IOException("Attribute name not recognized by CertAttrSet:PolicyConstraints.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(REQUIRE)) {
            this.require = -1;
        } else if (name.equalsIgnoreCase(INHIBIT)) {
            this.inhibit = -1;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet:PolicyConstraints.");
        }
        encodeThis();
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(REQUIRE);
        elements.addElement(INHIBIT);
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }
}
