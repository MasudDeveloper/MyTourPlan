package com.mrdeveloper.mytourplan.models;

import com.google.gson.annotations.SerializedName;

public class SyncGenericResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("server_id")
    private int serverId;

    @SerializedName("local_id")
    private String localId;

    @SerializedName("message")
    private String message;

    @SerializedName("error")
    private String error;

    public boolean isSuccess() { return success; }
    public int getServerId() { return serverId; }
    public String getLocalId() { return localId; }
    public String getMessage() { return message; }
    public String getError() { return error; }
}
