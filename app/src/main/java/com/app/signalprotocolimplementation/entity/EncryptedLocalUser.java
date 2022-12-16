package com.app.signalprotocolimplementation.entity;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EncryptedLocalUser extends BaseEncryptedEntity {

    private final IdentityKeyPair identityKeyPair;
    private final List<PreKeyRecord> preKey;
    private final SignedPreKeyRecord signedPreKey;

    public EncryptedLocalUser(byte[] identityKeyPair,
                              int registrationId,
                              String name,
                              int deviceId,
                              List<byte[]> preKeys,
                              byte[] signedPreKey) throws InvalidKeyException, IOException {
        super(registrationId, new SignalProtocolAddress(name, deviceId));
        this.identityKeyPair = new IdentityKeyPair(identityKeyPair);
        preKey = new ArrayList<>();
        for (byte[] item : preKeys) {
            preKey.add(new PreKeyRecord(item));
        }
        this.signedPreKey = new SignedPreKeyRecord(signedPreKey);
    }


    public static PreKeyRecord toPreKeyRecord(byte[] bytes) throws IOException {
        return new PreKeyRecord(bytes);
    }

    public IdentityKeyPair getIdentityKeyPair() {
        return identityKeyPair;
    }

    public List<PreKeyRecord> getPreKeys() {
        return preKey;
    }

    public SignedPreKeyRecord getSignedPreKey() {
        return signedPreKey;
    }
}