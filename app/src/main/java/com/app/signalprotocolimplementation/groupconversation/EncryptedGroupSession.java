package com.app.signalprotocolimplementation.groupconversation;

import android.util.Pair;

import com.app.signalprotocolimplementation.entity.EncryptedLocalUser;
import com.app.signalprotocolimplementation.helper.Helper;
import com.app.signalprotocolimplementation.registration.RegistrationManager;

import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.NoSessionException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.groups.GroupCipher;
import org.whispersystems.libsignal.groups.GroupSessionBuilder;
import org.whispersystems.libsignal.groups.state.SenderKeyRecord;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.SenderKeyDistributionMessage;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EncryptedGroupSession {

    private final EncryptedLocalUser localUser;
    private final UUID distributionId;

    private final GroupCipher encryptGroupCipher;

    private final InMemorySignalProtocolStore protocolStore;

    private final Map<String, GroupCipher> decryptGroupCiphers;

    private final Map<String, String> senderKeyDistributionStore;

    private final GroupSessionBuilder sessionBuilder;

    public EncryptedGroupSession(EncryptedLocalUser localUser, String keyRecord, String groupName) throws IOException {
        this.localUser = localUser;
        this.distributionId = UUID.fromString(groupName);
        decryptGroupCiphers = new HashMap<>();

        protocolStore = new InMemorySignalProtocolStore(localUser.getIdentityKeyPair(), localUser.getRegistrationId());

        sessionBuilder = new GroupSessionBuilder(protocolStore);

        protocolStore.storeSignedPreKey(localUser.getSignedPreKey().getId(), localUser.getSignedPreKey());

        for (PreKeyRecord record : localUser.getPreKeys()) {
            protocolStore.storePreKey(record.getId(), record);
        }

        if (keyRecord != null)
            protocolStore.storeSenderKey(localUser.getSignalProtocolAddress(), distributionId, new SenderKeyRecord(Helper.decodeToByteArray(keyRecord)));

        encryptGroupCipher = new GroupCipher(protocolStore, localUser.getSignalProtocolAddress());

        senderKeyDistributionStore = new HashMap<>();
    }

    public String getKeyRecord() {
        SenderKeyRecord keyRecord = protocolStore.loadSenderKey(localUser.getSignalProtocolAddress(), distributionId);
        return Helper.encodeToBase64(keyRecord.serialize());
    }

    private void addDecryptGroupCiphers(List<SenderKeyDistributionModel> keys) throws InvalidMessageException, LegacyMessageException {
        for (SenderKeyDistributionModel item : keys) {
            byte[] serializedSenderDistributionKey = Helper.decodeToByteArray(item.senderKeyDistributionMessage);
            SenderKeyDistributionMessage senderKeyDistributionMessage = new SenderKeyDistributionMessage(serializedSenderDistributionKey);

            SignalProtocolAddress senderAddress = new SignalProtocolAddress(item.id, RegistrationManager.DEFAULT_DEVICE_ID);

            if (!decryptGroupCiphers.containsKey(item.id) || !Objects.equals(senderKeyDistributionStore.get(item.id), item.senderKeyDistributionMessage)) {
                senderKeyDistributionStore.remove(item.id);
                decryptGroupCiphers.remove(item.id);

                //Start the session for reading group originator
                sessionBuilder.process(senderAddress, senderKeyDistributionMessage);

                //Save the decryption group cipher for that contact
                senderKeyDistributionStore.put(item.id, item.senderKeyDistributionMessage);
                decryptGroupCiphers.put(item.id, new GroupCipher(protocolStore, senderAddress));
            }
        }
    }

    public void createSession(List<SenderKeyDistributionModel> list) throws LegacyMessageException, InvalidMessageException {
        addDecryptGroupCiphers(list);
    }


    public static Pair<String, String> createGroup(EncryptedLocalUser localUser, String uuid) {
        return createDistributionKey(localUser, uuid);
    }

    public static Pair<String, String> createDistributionKey(EncryptedLocalUser localUser, String uuid) {
        SignalProtocolAddress address = localUser.getSignalProtocolAddress();
        InMemorySignalProtocolStore store = new InMemorySignalProtocolStore(localUser.getIdentityKeyPair(), localUser.getRegistrationId());
        store.storeSignedPreKey(localUser.getSignedPreKey().getId(), localUser.getSignedPreKey());

        for (PreKeyRecord record : localUser.getPreKeys()) {
            store.storePreKey(record.getId(), record);
        }


        store.storeSenderKey(localUser.getSignalProtocolAddress(), UUID.fromString(uuid), new SenderKeyRecord());


        GroupSessionBuilder sessionBuilder = new GroupSessionBuilder(store);

        SenderKeyDistributionMessage senderDistributionMessage = sessionBuilder.create(address, UUID.fromString(uuid));
        SenderKeyRecord senderKeyRecord = store.loadSenderKey(localUser.getSignalProtocolAddress(), UUID.fromString(uuid));

        return new Pair<>(
                Helper.encodeToBase64(senderDistributionMessage.serialize()),
                Helper.encodeToBase64(senderKeyRecord.serialize())
        );
    }

    public String encryptMessage(String message) throws NoSessionException {
        CiphertextMessage encryptedMessage = encryptGroupCipher.encrypt(distributionId, message.getBytes(StandardCharsets.UTF_8));
        return Helper.encodeToBase64(encryptedMessage.serialize());
    }

    public String decryptMessage(String encryptedMessage, String userId)
            throws NoSessionException,
            InvalidMessageException,
            DuplicateMessageException,
            LegacyMessageException {
        GroupCipher decryptGroupCipher = decryptGroupCiphers.get(userId);
        byte[] text = Objects.requireNonNull(decryptGroupCipher).decrypt(Helper.decodeToByteArray(encryptedMessage));
        return new String(text, StandardCharsets.UTF_8);
    }

    public String decryptMessage(String encryptedMessage, String userId, String senderKey)
            throws InvalidMessageException,
            LegacyMessageException,
            NoSessionException,
            DuplicateMessageException {
        List<SenderKeyDistributionModel> mList = new ArrayList<>();
        mList.add(new SenderKeyDistributionModel(userId, senderKey));
        addDecryptGroupCiphers(mList);
        return decryptMessage(encryptedMessage, userId);
    }
}