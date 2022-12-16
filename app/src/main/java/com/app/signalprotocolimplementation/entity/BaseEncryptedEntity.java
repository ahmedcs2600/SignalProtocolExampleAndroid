package com.app.signalprotocolimplementation.entity;

import org.whispersystems.libsignal.SignalProtocolAddress;

public abstract class BaseEncryptedEntity {
    protected final int registrationId;
    protected final SignalProtocolAddress address;

    protected BaseEncryptedEntity(int registrationId, SignalProtocolAddress address) {
        this.registrationId = registrationId;
        this.address = address;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public SignalProtocolAddress getSignalProtocolAddress() {
        return address;
    }
}