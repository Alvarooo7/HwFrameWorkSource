package com.huawei.security.keystore;

import android.os.IBinder;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keymaster.HwOperationResult;
import com.huawei.security.keystore.ArrayUtils.EmptyArray;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.ProviderException;

public class HwUniversalKeyStoreCryptoOperationStreamer {
    private static final int DEFAULT_MAX_CHUNK_SIZE = 65536;
    private byte[] mBuffered;
    private int mBufferedLength;
    private int mBufferedOffset;
    private long mConsumedInputSizeBytes;
    private final Stream mKeyStoreStream;
    private final int mMaxChunkSize;
    private long mProducedOutputSizeBytes;

    interface Stream {
        HwOperationResult finish(byte[] bArr, byte[] bArr2);

        HwOperationResult update(byte[] bArr);
    }

    public static class MainDataStream implements Stream {
        private final HwKeystoreManager mKeyStore;
        private final IBinder mOperationToken;

        public MainDataStream(HwKeystoreManager keyStore, IBinder operationToken) {
            this.mKeyStore = keyStore;
            this.mOperationToken = operationToken;
        }

        public HwOperationResult update(byte[] input) {
            return this.mKeyStore.update(this.mOperationToken, null, input);
        }

        public HwOperationResult finish(byte[] signature, byte[] additionalEntropy) {
            return this.mKeyStore.finish(this.mOperationToken, null, signature, additionalEntropy);
        }
    }

    public HwUniversalKeyStoreCryptoOperationStreamer(Stream operation) {
        this(operation, DEFAULT_MAX_CHUNK_SIZE);
    }

    public HwUniversalKeyStoreCryptoOperationStreamer(Stream operation, int maxChunkSize) {
        this.mBuffered = EmptyArray.BYTE;
        this.mBufferedOffset = 0;
        this.mBufferedLength = 0;
        this.mKeyStoreStream = operation;
        this.mMaxChunkSize = maxChunkSize;
    }

    public byte[] update(byte[] input, int inputOffset, int inputLength) throws HwUniversalKeyStoreException {
        if (inputLength == 0) {
            return EmptyArray.BYTE;
        }
        byte[] result;
        ByteArrayOutputStream byteArrayOutputStream = null;
        while (inputLength > 0) {
            int inputBytesInChunk;
            byte[] chunk;
            if (this.mBufferedLength + inputLength > this.mMaxChunkSize) {
                inputBytesInChunk = this.mMaxChunkSize - this.mBufferedLength;
                chunk = ArrayUtils.concat(this.mBuffered, this.mBufferedOffset, this.mBufferedLength, input, inputOffset, inputBytesInChunk);
            } else if (this.mBufferedLength == 0 && inputOffset == 0 && inputLength == input.length) {
                chunk = input;
                inputBytesInChunk = input.length;
            } else {
                inputBytesInChunk = inputLength;
                chunk = ArrayUtils.concat(this.mBuffered, this.mBufferedOffset, this.mBufferedLength, input, inputOffset, inputBytesInChunk);
            }
            inputOffset += inputBytesInChunk;
            inputLength -= inputBytesInChunk;
            this.mConsumedInputSizeBytes += (long) inputBytesInChunk;
            HwOperationResult opResult = this.mKeyStoreStream.update(chunk);
            if (opResult == null) {
                throw new ProviderException("Failed to communicate with keystore service");
            } else if (opResult.resultCode != 1) {
                throw HwKeystoreManager.getKeyStoreException(opResult.resultCode);
            } else {
                if (opResult.inputConsumed == chunk.length) {
                    this.mBuffered = EmptyArray.BYTE;
                    this.mBufferedOffset = 0;
                    this.mBufferedLength = 0;
                } else if (opResult.inputConsumed <= 0) {
                    if (inputLength > 0) {
                        throw new HwUniversalKeyStoreException(HwKeymasterDefs.KM_ERROR_UNKNOWN_ERROR, "Keystore consumed nothing from max-sized chunk: " + chunk.length + " bytes");
                    }
                    this.mBuffered = chunk;
                    this.mBufferedOffset = 0;
                    this.mBufferedLength = chunk.length;
                } else if (opResult.inputConsumed < chunk.length) {
                    this.mBuffered = chunk;
                    this.mBufferedOffset = opResult.inputConsumed;
                    this.mBufferedLength = chunk.length - opResult.inputConsumed;
                } else {
                    throw new HwUniversalKeyStoreException(HwKeymasterDefs.KM_ERROR_UNKNOWN_ERROR, "Keystore consumed more input than provided. Provided: " + chunk.length + ", consumed: " + opResult.inputConsumed);
                }
                if (opResult.output != null && opResult.output.length > 0) {
                    if (inputLength <= 0) {
                        if (byteArrayOutputStream == null) {
                            result = opResult.output;
                        } else {
                            try {
                                byteArrayOutputStream.write(opResult.output);
                                result = byteArrayOutputStream.toByteArray();
                            } catch (IOException e) {
                                throw new ProviderException("Failed to buffer output", e);
                            }
                        }
                        this.mProducedOutputSizeBytes += (long) result.length;
                        return result;
                    } else if (byteArrayOutputStream == null) {
                        byteArrayOutputStream = new ByteArrayOutputStream();
                        try {
                            byteArrayOutputStream.write(opResult.output);
                        } catch (IOException e2) {
                            throw new ProviderException("Failed to buffer output", e2);
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        if (byteArrayOutputStream == null) {
            result = EmptyArray.BYTE;
        } else {
            result = byteArrayOutputStream.toByteArray();
        }
        this.mProducedOutputSizeBytes += (long) result.length;
        return result;
    }

    public byte[] doFinal(byte[] input, int inputOffset, int inputLength, byte[] signature, byte[] additionalEntropy) throws HwUniversalKeyStoreException {
        if (inputLength == 0) {
            input = EmptyArray.BYTE;
            inputOffset = 0;
        }
        byte[] output = ArrayUtils.concat(update(input, inputOffset, inputLength), flush());
        HwOperationResult opResult = this.mKeyStoreStream.finish(signature, additionalEntropy);
        if (opResult == null) {
            throw new ProviderException("Failed to communicate with keystore service");
        } else if (opResult.resultCode != 1) {
            throw HwKeystoreManager.getKeyStoreException(opResult.resultCode);
        } else {
            this.mProducedOutputSizeBytes += (long) opResult.output.length;
            return ArrayUtils.concat(output, opResult.output);
        }
    }

    public byte[] flush() throws HwUniversalKeyStoreException {
        if (this.mBufferedLength <= 0) {
            return EmptyArray.BYTE;
        }
        ByteArrayOutputStream byteArrayOutputStream = null;
        while (this.mBufferedLength > 0) {
            byte[] chunk = ArrayUtils.subarray(this.mBuffered, this.mBufferedOffset, this.mBufferedLength);
            HwOperationResult opResult = this.mKeyStoreStream.update(chunk);
            if (opResult == null) {
                throw new ProviderException("Failed to communicate with keystore service");
            } else if (opResult.resultCode != 1) {
                throw HwKeystoreManager.getKeyStoreException(opResult.resultCode);
            } else if (opResult.inputConsumed <= 0) {
                break;
            } else {
                if (opResult.inputConsumed >= chunk.length) {
                    this.mBuffered = EmptyArray.BYTE;
                    this.mBufferedOffset = 0;
                    this.mBufferedLength = 0;
                } else {
                    this.mBuffered = chunk;
                    this.mBufferedOffset = opResult.inputConsumed;
                    this.mBufferedLength = chunk.length - opResult.inputConsumed;
                }
                if (opResult.inputConsumed > chunk.length) {
                    throw new HwUniversalKeyStoreException(HwKeymasterDefs.KM_ERROR_UNKNOWN_ERROR, "Keystore consumed more input than provided. Provided: " + chunk.length + ", consumed: " + opResult.inputConsumed);
                } else if (opResult.output != null && opResult.output.length > 0) {
                    if (byteArrayOutputStream == null) {
                        if (this.mBufferedLength == 0) {
                            this.mProducedOutputSizeBytes += (long) opResult.output.length;
                            return opResult.output;
                        }
                        byteArrayOutputStream = new ByteArrayOutputStream();
                    }
                    try {
                        byteArrayOutputStream.write(opResult.output);
                    } catch (IOException e) {
                        throw new ProviderException("Failed to buffer output", e);
                    }
                }
            }
        }
        if (this.mBufferedLength > 0) {
            throw new HwUniversalKeyStoreException(HwKeymasterDefs.KM_ERROR_UNKNOWN_ERROR, "Keystore failed to consume last " + (this.mBufferedLength != 1 ? this.mBufferedLength + " bytes" : "byte") + " of input");
        }
        byte[] result = byteArrayOutputStream != null ? byteArrayOutputStream.toByteArray() : EmptyArray.BYTE;
        this.mProducedOutputSizeBytes += (long) result.length;
        return result;
    }

    public long getConsumedInputSizeBytes() {
        return this.mConsumedInputSizeBytes;
    }

    public long getProducedOutputSizeBytes() {
        return this.mProducedOutputSizeBytes;
    }
}
