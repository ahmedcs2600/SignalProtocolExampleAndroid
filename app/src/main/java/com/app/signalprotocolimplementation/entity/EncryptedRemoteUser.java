package com.app.signalprotocolimplementation.entity;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECPublicKey;

public class EncryptedRemoteUser extends BaseEncryptedEntity {

    private final int preKeyId;
    private final ECPublicKey preKeyPublicKey;

    private final int signedPreKeyId;
    private final ECPublicKey signedPreKeyPublicKey;

    private final byte[] signedPreKeySignature;
    private final IdentityKey identityKeyPairPublicKey;

    public EncryptedRemoteUser(
            int registrationId,
            String name,
            int deviceId,
            int preKeyId,
            byte[] preKeyPublicKey,
            int signedPreKeyId,
            byte[] signedPreKeyPublicKey,
            byte[] signedPreKeySignature,
            byte[] identityKeyPairPublicKey) throws InvalidKeyException {

        super(registrationId, new SignalProtocolAddress(name, deviceId));
        this.preKeyId = preKeyId;
        this.preKeyPublicKey = Curve.decodePoint(preKeyPublicKey, 0);
        this.signedPreKeyId = signedPreKeyId;
        this.signedPreKeyPublicKey = Curve.decodePoint(signedPreKeyPublicKey, 0);
        this.signedPreKeySignature = signedPreKeySignature;
        this.identityKeyPairPublicKey = new IdentityKey(identityKeyPairPublicKey, 0);
    }

    public int getPreKeyId() {
        return preKeyId;
    }

    public ECPublicKey getPreKeyPublicKey() {
        return preKeyPublicKey;
    }

    public int getSignedPreKeyId() {
        return signedPreKeyId;
    }

    public ECPublicKey getSignedPreKeyPublicKey() {
        return signedPreKeyPublicKey;
    }

    public byte[] getSignedPreKeySignature() {
        return signedPreKeySignature;
    }

    public IdentityKey getIdentityKeyPairPublicKey() {
        return identityKeyPairPublicKey;
    }
}