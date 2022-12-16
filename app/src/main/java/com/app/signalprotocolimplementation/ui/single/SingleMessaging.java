package com.app.signalprotocolimplementation.ui.single;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.app.signalprotocolimplementation.R;
import com.app.signalprotocolimplementation.entity.EncryptedLocalUser;
import com.app.signalprotocolimplementation.entity.EncryptedRemoteUser;
import com.app.signalprotocolimplementation.registration.RegistrationItem;
import com.app.signalprotocolimplementation.registration.RegistrationManager;
import com.app.signalprotocolimplementation.singleconversation.EncryptedSingleSession;
import com.app.signalprotocolimplementation.ui.MessagingAdapter;

import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.UntrustedIdentityException;

import java.io.IOException;
import java.util.UUID;

public class SingleMessaging extends AppCompatActivity {

    RegistrationItem alice;
    RegistrationItem bob;

    private MessagingAdapter aliceMessagingAdapter;
    private MessagingAdapter bobMessagingAdapter;

    private Button aliceSendButton;
    private EditText aliceMessageBox;
    private String aliceUserId = "100";

    private Button bobSendButton;
    private EditText bobMessageBox;
    private String bobUserId = "200";

    private EncryptedSingleSession aliceEncryptedSession;
    private EncryptedSingleSession bobEncryptedSession;

    private RecyclerView aliceMessagingView;
    private RecyclerView bobMessagingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_messaging);

        aliceSendButton = findViewById(R.id.aliceSendButton);
        aliceMessageBox = findViewById(R.id.aliceMessageBox);

        bobSendButton = findViewById(R.id.bobSendButton);
        bobMessageBox = findViewById(R.id.bobMessageBox);

        aliceMessagingView = findViewById(R.id.aliceMessagingView);
        bobMessagingView = findViewById(R.id.bobMessagingView);

        initAlice();
        initBob();

        initAliceChatSession();
        initBobChatSession();
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

            EncryptedRemoteUser aliceModel = new EncryptedRemoteUser(
                    alice.getRegistrationId(),
                    "alice",
                    RegistrationManager.DEFAULT_DEVICE_ID,
                    alice.getPreKeys().get(0).getId(),
                    alice.getPreKeys().get(0).getKeyPair().getPublicKey().serialize(),
                    alice.getSignedPreKeyId(),
                    alice.getSignedPreKeyRecord().getKeyPair().getPublicKey().serialize(),
                    alice.getSignedPreKeyRecord().getSignature(),
                    alice.getIdentityKeyPair().getPublicKey().serialize()
            );

            bobEncryptedSession = new EncryptedSingleSession(
                    bobModel,
                    aliceModel
            );

            bobSendButton.setOnClickListener(view -> onMessageSendFromBob());

        } catch (InvalidKeyException | IOException | UntrustedIdentityException e) {
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

            EncryptedRemoteUser bobModel = new EncryptedRemoteUser(
                    bob.getRegistrationId(),
                    "bob",
                    RegistrationManager.DEFAULT_DEVICE_ID,
                    bob.getPreKeys().get(0).getId(),
                    bob.getPreKeys().get(0).getKeyPair().getPublicKey().serialize(),
                    bob.getSignedPreKeyId(),
                    bob.getSignedPreKeyRecord().getKeyPair().getPublicKey().serialize(),
                    bob.getSignedPreKeyRecord().getSignature(),
                    bob.getIdentityKeyPair().getPublicKey().serialize()
            );

            aliceEncryptedSession = new EncryptedSingleSession(
                    aliceModel,
                    bobModel
            );

            aliceSendButton.setOnClickListener(view -> {
                onMessageSendFromAlice();
            });

        } catch (InvalidKeyException | IOException | UntrustedIdentityException e) {
            e.printStackTrace();
        }
    }

    private void onMessageSendFromAlice() {
        String text = aliceMessageBox.getText().toString();

        if (text.isEmpty()) return;

        try {
            String encryptedMessage = aliceEncryptedSession.encrypt(text);
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
        } catch (UntrustedIdentityException | InvalidKeyException | InvalidMessageException | InvalidVersionException e) {
            e.printStackTrace();
        }
    }

    private void onMessageSendFromBob() {
        String text = bobMessageBox.getText().toString();

        if (text.isEmpty()) return;

        try {
            String encryptedMessage = bobEncryptedSession.encrypt(text);
            String id = UUID.randomUUID().toString();

            bobMessagingAdapter.onNewMessage(new Message(
                    id,
                    text,
                    aliceUserId,
                    bobUserId
            ));

            onNewMessageFromBob(new Message(
                    id,
                    encryptedMessage,
                    bobUserId,
                    aliceUserId
            ));
        } catch (UntrustedIdentityException | InvalidKeyException | InvalidMessageException | InvalidVersionException e) {
            e.printStackTrace();
        }
    }

    private void initAlice() {
        try {
            alice = RegistrationManager.generateKeys();
        } catch (InvalidKeyException | IOException e) {
            e.printStackTrace();
        }

        aliceMessagingAdapter = new MessagingAdapter();
        aliceMessagingView.setAdapter(aliceMessagingAdapter);
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

    private void onNewMessageFromAlice(Message remoteMessage) {
        try {
            String encryptedMessage = remoteMessage.getMessage();
            String decryptedMessage = bobEncryptedSession.decrypt(encryptedMessage);
            bobMessagingAdapter.onNewMessage(new Message(
                    remoteMessage.getId(),
                    decryptedMessage,
                    remoteMessage.getFromId(),
                    remoteMessage.toString()
            ));
        } catch (UntrustedIdentityException | InvalidKeyException | InvalidMessageException | InvalidVersionException | DuplicateMessageException | InvalidKeyIdException | LegacyMessageException e) {
            e.printStackTrace();
        }
    }
    private void onNewMessageFromBob(Message remoteMessage) {
        try {
            String encryptedMessage = remoteMessage.getMessage();
            String decryptedMessage = aliceEncryptedSession.decrypt(encryptedMessage);
            aliceMessagingAdapter.onNewMessage(new Message(
                    remoteMessage.getId(),
                    decryptedMessage,
                    remoteMessage.getFromId(),
                    remoteMessage.toString()
            ));
        } catch (UntrustedIdentityException | InvalidKeyException | InvalidMessageException | InvalidVersionException | DuplicateMessageException | InvalidKeyIdException | LegacyMessageException e) {
            e.printStackTrace();
        }
    }
}