package org.bouncycastle.crypto.generators;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.util.DigestFactory;
import org.bouncycastle.util.Arrays;

public class PKCS5S2ParametersGenerator extends PBEParametersGenerator {
    private Mac hMac;
    private byte[] state;

    public PKCS5S2ParametersGenerator() {
        this(DigestFactory.createSHA1());
    }

    public PKCS5S2ParametersGenerator(Digest digest) {
        this.hMac = new HMac(digest);
        this.state = new byte[this.hMac.getMacSize()];
    }

    private void F(byte[] bArr, int i, byte[] bArr2, byte[] bArr3, int i2) {
        if (i != 0) {
            if (bArr != null) {
                this.hMac.update(bArr, 0, bArr.length);
            }
            this.hMac.update(bArr2, 0, bArr2.length);
            this.hMac.doFinal(this.state, 0);
            System.arraycopy(this.state, 0, bArr3, i2, this.state.length);
            for (int i3 = 1; i3 < i; i3++) {
                this.hMac.update(this.state, 0, this.state.length);
                this.hMac.doFinal(this.state, 0);
                for (int i4 = 0; i4 != this.state.length; i4++) {
                    int i5 = i2 + i4;
                    bArr3[i5] = (byte) (bArr3[i5] ^ this.state[i4]);
                }
            }
            return;
        }
        throw new IllegalArgumentException("iteration count must be at least 1.");
    }

    private byte[] generateDerivedKey(int i) {
        int macSize = this.hMac.getMacSize();
        i = ((i + macSize) - 1) / macSize;
        byte[] bArr = new byte[4];
        byte[] bArr2 = new byte[(i * macSize)];
        this.hMac.init(new KeyParameter(this.password));
        int i2 = 0;
        for (int i3 = 1; i3 <= i; i3++) {
            int i4 = 3;
            while (true) {
                byte b = (byte) (bArr[i4] + 1);
                bArr[i4] = b;
                if (b != (byte) 0) {
                    break;
                }
                i4--;
            }
            F(this.salt, this.iterationCount, bArr, bArr2, i2);
            i2 += macSize;
        }
        return bArr2;
    }

    public CipherParameters generateDerivedMacParameters(int i) {
        return generateDerivedParameters(i);
    }

    public CipherParameters generateDerivedParameters(int i) {
        i /= 8;
        return new KeyParameter(Arrays.copyOfRange(generateDerivedKey(i), 0, i), 0, i);
    }

    public CipherParameters generateDerivedParameters(int i, int i2) {
        i /= 8;
        i2 /= 8;
        byte[] generateDerivedKey = generateDerivedKey(i + i2);
        return new ParametersWithIV(new KeyParameter(generateDerivedKey, 0, i), generateDerivedKey, i, i2);
    }
}
