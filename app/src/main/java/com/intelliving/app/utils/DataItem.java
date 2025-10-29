package com.intelliving.app.utils;

import androidx.annotation.Nullable;

/**
 * Created by simone.mutti on 06/11/17.
 */

public class DataItem {

    public enum VipType{
        EXTERNAL_UNIT,
        INTERNAL_UNIT,
        SWITCHBOARD,
        OPENDOOR,
        ACTUATOR,
        RTSP_CAMERA,
        PALIP
    };

    private String name;
    private String description;
    private String id;

    private boolean showPhoneAction;
    private VipType type;

    public DataItem(String name, String description, String id, boolean showPhoneAction, VipType type) {

        this.name = name;
        this.description = description;
        this.id = id;

        this.showPhoneAction = showPhoneAction;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isShowPhoneAction() {
        return showPhoneAction;
    }

    public void setShowPhoneAction(boolean showPhoneAction) {
        this.showPhoneAction = showPhoneAction;
    }

    public VipType getType() {
        return type;
    }

    public void setType(VipType type) {
        this.type = type;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;

        if (obj != null && obj instanceof DataItem)
        {
            result = this.id == ((DataItem) obj).id;
        }

        return result;
    }
}
