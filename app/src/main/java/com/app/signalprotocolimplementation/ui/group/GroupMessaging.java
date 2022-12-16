package com.app.signalprotocolimplementation.ui.group;

import android.os.Bundle;
import android.util.Pair;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.app.signalprotocolimplementation.R;
import com.app.signalprotocolimplementation.entity.EncryptedLocalUser;
import com.app.signalprotocolimplementation.entity.EncryptedRemoteUser;
import com.app.signalprotocolimplementation.groupconversation.EncryptedGroupSession;
import com.app.signalprotocolimplementation.groupconversation.SenderKeyDistributionModel;
import com.app.signalprotocolimplementation.registration.RegistrationItem;
import com.app.signalprotocolimplementation.registration.RegistrationManager;
import com.app.signalprotocolimplementation.singleconversation.EncryptedSingleSession;
import com.app.signalprotocolimplementation.ui.MessagingAdapter;
import com.app.signalprotocolimplementation.ui.single.Message;

import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.NoSessionException;
import org.whispersystems.libsignal.UntrustedIdentityException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class GroupMessaging extends AppCompatActivity {

    RegistrationItem alice;
    RegistrationItem bob;

    private MessagingAdapter aliceMessagingAdapter;
    private MessagingAdapter bobMessagingAdapter;

    private RecyclerView aliceMessagingView;
    private RecyclerView bobMessagingView;

    private static String GROUP_UUID = UUID.randomUUID().toString();

    /**
     * Sender Key will be sent to other participants and sender Key record will be private
     */
    Pair<String, String> aliceSenderKeyAndSenderKeyRecord;
    Pair<String, String> bobSenderKeyAndSenderKeyRecord;

    private EncryptedGroupSession aliceEncryptedSession;
    private EncryptedGroupSession bobEncryptedSession;

    private Button aliceSendButton;
    private EditText aliceMessageBox;
    private String aliceUserId = "100";

    private Button bobSendButton;
    private EditText bobMessageBox;
    private String bobUserId = "200";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messaging);

        aliceSendButton = findViewById(R.id.aliceSendButton);
        aliceMessageBox = findViewById(R.id.aliceMessageBox);

        bobSendButton = findViewById(R.id.bobSendButton);
        bobMessageBox = findViewById(R.id.bobMessageBox);

        aliceMessagingView = findViewById(R.id.aliceMessagingView);
        bobMessagingView = findViewById(R.id.bobMessagingView);

        initAlice();
        initBob();

        createGroup();
        onNewGroupCreated();

        initBobChatSession();

        initAliceChatSession();
    }

    private void initBobChatSession() {
        try {
            EncryptedLocalUser bobModel = new EncryptedLocalUser(
                    bob.getIdentityKeyPair().serialize(),
                    bob.getRegistrationId(),
                    "bob",
                    RegistrationManager.DEFAULT_DEVICE_ID,
                    bob.getPreKeysBytes(),
                    bob.signedPreKeyRecord());

            bobEncryptedSession = new EncryptedGroupSession(
                    bobModel,
                    bobSenderKeyAndSenderKeyRecord.second,
                    GROUP_UUID
            );

            ArrayList<SenderKeyDistributionModel> mSenderKeyDistributionModel = new ArrayList<>();

            mSenderKeyDistributionModel.add(new SenderKeyDistributionModel(
                    aliceUserId,
                    aliceSenderKeyAndSenderKeyRecord.first
            ));

            bobEncryptedSession.createSession(mSenderKeyDistributionModel);

            bobSendButton.setOnClickListener(view -> onMessageSendFromBob());

        } catch (InvalidKeyException | IOException | InvalidMessageException | LegacyMessageException e) {
            e.printStackTrace();
        }
    }

    private void initAliceChatSession() {
        try {
            EncryptedLocalUser aliceModel = new EncryptedLocalUser(
                    alice.getIdentityKeyPair().serialize(),
                    alice.getRegistrationId(),
                    "alice",
                    RegistrationManager.DEFAULT_DEVICE_ID,
                    alice.getPreKeysBytes(),
                    alice.signedPreKeyRecord());

            aliceEncryptedSession = new EncryptedGroupSession(
                    aliceModel,
                    aliceSenderKeyAndSenderKeyRecord.second,
                    GROUP_UUID

            );

            ArrayList<SenderKeyDistributionModel> mSenderKeyDistributionModel = new ArrayList<>();

            mSenderKeyDistributionModel.add(new SenderKeyDistributionModel(
                    bobUserId,
                    bobSenderKeyAndSenderKeyRecord.first
            ));

            aliceEncryptedSession.createSession(mSenderKeyDistributionModel);

            aliceSendButton.setOnClickListener(view -> onMessageSendFromAlice());

        } catch (InvalidKeyException | IOException | InvalidMessageException | LegacyMessageException e) {
            e.printStackTrace();
        }
    }

    //Alice will create the group
    //UUID will be generated from the admin side
    private void createGroup() {
        try {
            EncryptedLocalUser aliceModel = new EncryptedLocalUser(
                    alice.getIdentityKeyPair().serialize(),
                    alice.getRegistrationId(),
                    "alice",
                    RegistrationManager.DEFAULT_DEVICE_ID,
                    alice.getPreKeysBytes(),
                    alice.signedPreKeyRecord());
            aliceSenderKeyAndSenderKeyRecord = EncryptedGroupSession.createGroup(aliceModel, GROUP_UUID);
        } catch (InvalidKeyException | IOException e) {
            e.printStackTrace();
        }
    }

    //Bob will receive the Group Created Event and will create his sender Key
    private void onNewGroupCreated() {
        try {
            EncryptedLocalUser bobModel = new EncryptedLocalUser(
                    bob.getIdentityKeyPair().serialize(),
                    bob.getRegistrationId(),
                    "bob",
                    RegistrationManager.DEFAULT_DEVICE_ID,
                    bob.getPreKeysBytes(),
                    bob.signedPreKeyRecord());
            bobSenderKeyAndSenderKeyRecord = EncryptedGroupSession.createDistributionKey(bobModel, GROUP_UUID);
        } catch (InvalidKeyException | IOException e) {
            e.printStackTrace();
        }
    }

    private void initAlice() {
        try {
            alice = RegistrationManager.generateKeys();
            aliceMessagingAdapter = new MessagingAdapter();
            aliceMessagingView.setAdapter(aliceMessagingAdapter);
        } catch (InvalidKeyException | IOException e) {
            e.printStackTrace();
        }
    }

    private void initBob() {
        try {
            bob = RegistrationManager.generateKeys();
        } catch (InvalidKeyException | IOException e) {
            e.printStackTrace();
        }
        bobMessagingAdapter = new MessagingAdapter();
        bobMessagingView.setAdapter(bobMessagingAdapter);
    }

    private void onMessageSendFromAlice() {
        String text = aliceMessageBox.getText().toString();

        if (text.isEmpty()) return;

        try {
            String encryptedMessage = aliceEncryptedSession.encryptMessage(text);
            String id = UUID.randomUUID().toString();
            aliceMessagingAdapter.onNewMessage(new Message(
                    id,
                    text,
                    aliceUserId,
                    bobUserId
            ));
            onNewMessageFromAlice(new Message(
                    id,
                    encryptedMessage,
                    aliceUserId,
                    bobUserId
            ));
        } catch (NoSessionException e) {
            e.printStackTrace();
        }
    }

    private void onMessageSendFromBob() {
        String text = bobMessageBox.getText().toString();

        if (text.isEmpty()) return;

        try {
            String encryptedMessage = bobEncryptedSession.encryptMessage(text);
            String id = UUID.randomUUID().toString();
            bobMessagingAdapter.onNewMessage(new Message(
                    id,
                    text,
                    bobUserId,
                    aliceUserId
            ));
            onNewMessageFromBob(new Message(
                    id,
                    encryptedMessage,
                    bobUserId,
                    aliceUserId
            ));
        } catch (NoSessionException e) {
            e.printStackTrace();
        }
    }

    private void onNewMessageFromAlice(Message remoteMessage) {
        try {
            String encryptedMessage = remoteMessage.getMessage();
            String decryptedMessage = bobEncryptedSession.decryptMessage(encryptedMessage, aliceUserId);
            bobMessagingAdapter.onNewMessage(new Message(
                    remoteMessage.getId(),
                    decryptedMessage,
                    remoteMessage.getFromId(),
                    remoteMessage.toString()
            ));
        } catch (InvalidMessageException | DuplicateMessageException | LegacyMessageException | NoSessionException e) {
            e.printStackTrace();
        }
    }

    private void onNewMessageFromBob(Message remoteMessage) {
        try {
            String encryptedMessage = remoteMessage.getMessage();
            String decryptedMessage = aliceEncryptedSession.decryptMessage(encryptedMessage, bobUserId);
            aliceMessagingAdapter.onNewMessage(new Message(
                    remoteMessage.getId(),
                    decryptedMessage,
                    remoteMessage.getFromId(),
                    remoteMessage.toString()
            ));
        } catch (InvalidMessageException | DuplicateMessageException | LegacyMessageException | NoSessionException e) {
            e.printStackTrace();
        }
    }
}