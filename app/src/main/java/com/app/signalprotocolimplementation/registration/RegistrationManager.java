package com.app.signalprotocolimplementation.registration;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECKeyPair;
import org.whispersystems.libsignal.ecc.ECPrivateKey;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;
import org.whispersystems.libsignal.util.Medium;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RegistrationManager {

    public static final int DEFAULT_DEVICE_ID = 2;
    private static final int BATCH_SIZE = 100;

    static IdentityKeyPair generateIdentityKeyPair() {
        ECKeyPair djbKeyPair = Curve.generateKeyPair();
        IdentityKey djbIdentityKey = new IdentityKey(djbKeyPair.getPublicKey());
        ECPrivateKey djbPrivateKey = djbKeyPair.getPrivateKey();

        return new IdentityKeyPair(djbIdentityKey, djbPrivateKey);
    }


    static SignedPreKeyRecord generateSignedPreKey(IdentityKeyPair identityKeyPair, int signedPreKeyId) throws InvalidKeyException {
        ECKeyPair keyPair = Curve.generateKeyPair();
        byte[] signature = Curve.calculateSignature(identityKeyPair.getPrivateKey(), keyPair.getPublicKey().serialize());
        return new SignedPreKeyRecord(signedPreKeyId, System.currentTimeMillis(), keyPair, signature);
    }


    public static List<PreKeyRecord> generatePreKeys() {
        List<PreKeyRecord> records = new LinkedList<>();
        int preKeyIdOffset = new Random().nextInt(Medium.MAX_VALUE - 101);
        for (int i = 0; i < BATCH_SIZE; i++) {
            int preKeyId = (preKeyIdOffset + i) % Medium.MAX_VALUE;
            ECKeyPair keyPair = Curve.generateKeyPair();
            PreKeyRecord record = new PreKeyRecord(preKeyId, keyPair);

            records.add(record);
        }

        return records;
    }

    public static RegistrationItem generateKeys() throws InvalidKeyException, IOException {
        IdentityKeyPair identityKeyPair = generateIdentityKeyPair();
        int registrationId = KeyHelper.generateRegistrationId(false);
        SignedPreKeyRecord signedPreKey = generateSignedPreKey(identityKeyPair, new Random().nextInt(Medium.MAX_VALUE - 1));
        List<PreKeyRecord> preKeys = generatePreKeys();
        return new RegistrationItem(
                identityKeyPair,
                registrationId,
                preKeys,
                signedPreKey
        );
    }
}
