package com.app.signalprotocolimplementation.singleconversation;

import com.app.signalprotocolimplementation.entity.EncryptedLocalUser;
import com.app.signalprotocolimplementation.entity.EncryptedRemoteUser;
import com.app.signalprotocolimplementation.helper.Helper;

import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;

import java.nio.charset.StandardCharsets;

public class EncryptedSingleSession {

    private enum Operation {ENCRYPT, DECRYPT}

    private SessionCipher mSessionCipher;

    private Operation lastOp;

    private final SignalProtocolAddress protocolAddress;

    private final EncryptedLocalUser localUser;
    private EncryptedRemoteUser remoteUser;

    public EncryptedSingleSession(EncryptedLocalUser localUser, EncryptedRemoteUser remoteUser) throws UntrustedIdentityException, InvalidKeyException {
        this.protocolAddress = remoteUser.getSignalProtocolAddress();
        this.localUser = localUser;
        this.remoteUser = remoteUser;
    }

    public EncryptedSingleSession(EncryptedLocalUser localUser,
                                  SignalProtocolAddress remotePersonProtocolAddress) {
        this.protocolAddress = remotePersonProtocolAddress;
        this.localUser = localUser;
    }

    private void initSession() {
        InMemorySignalProtocolStore protocolStore = new InMemorySignalProtocolStore(localUser.getIdentityKeyPair(), localUser.getRegistrationId());

        for (PreKeyRecord record : localUser.getPreKeys()) {
            protocolStore.storePreKey(record.getId(), record);
        }

        protocolStore.storeSignedPreKey(localUser.getSignedPreKey().getId(), localUser.getSignedPreKey());
        mSessionCipher = new SessionCipher(protocolStore, protocolAddress);
    }

    private void initSessionFromPreKey() throws UntrustedIdentityException, InvalidKeyException {
        InMemorySignalProtocolStore protocolStore = new InMemorySignalProtocolStore(localUser.getIdentityKeyPair(), localUser.getRegistrationId());

        for (PreKeyRecord record : localUser.getPreKeys()) {
            protocolStore.storePreKey(record.getId(), record);
        }

        protocolStore.storeSignedPreKey(localUser.getSignedPreKey().getId(), localUser.getSignedPreKey());

        //Session
        SessionBuilder sessionBuilder = new SessionBuilder(protocolStore, remoteUser.getSignalProtocolAddress());
        PreKeyBundle preKeyBundle = new PreKeyBundle(
                remoteUser.getRegistrationId(),
                remoteUser.getSignalProtocolAddress().getDeviceId(),
                remoteUser.getPreKeyId(),
                remoteUser.getPreKeyPublicKey(),
                remoteUser.getSignedPreKeyId(),
                remoteUser.getSignedPreKeyPublicKey(),
                remoteUser.getSignedPreKeySignature(),
                remoteUser.getIdentityKeyPairPublicKey()
        );
        sessionBuilder.process(preKeyBundle);
        mSessionCipher = new SessionCipher(protocolStore, protocolAddress);
    }

    private void createSession(Operation operation) throws UntrustedIdentityException, InvalidKeyException {
        if (operation == lastOp) {
            return;
        }

        lastOp = operation;

        if (remoteUser == null) {
            initSession();
        } else {
            initSessionFromPreKey();
        }
    }

    public String encrypt(String message) throws UntrustedIdentityException, InvalidKeyException, InvalidMessageException, InvalidVersionException {
        createSession(Operation.ENCRYPT);
        CiphertextMessage ciphertextMessage = mSessionCipher.encrypt(message.getBytes());
        PreKeySignalMessage preKeySignalMessage = new PreKeySignalMessage(ciphertextMessage.serialize());
        return Helper.encodeToBase64(preKeySignalMessage.serialize());
    }

    public String decrypt(String message) throws UntrustedIdentityException, InvalidKeyException, InvalidMessageException, InvalidVersionException, DuplicateMessageException, InvalidKeyIdException, LegacyMessageException {
        createSession(Operation.DECRYPT);
        byte[] bytes = Helper.decodeToByteArray(message);
        byte[] decryptedMessage = mSessionCipher.decrypt(new PreKeySignalMessage(bytes));
        return new String(decryptedMessage, StandardCharsets.UTF_8);
    }
}
