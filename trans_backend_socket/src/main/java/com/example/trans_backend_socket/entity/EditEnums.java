package com.example.trans_backend_socket.entity;

public enum EditEnums {

    ENTER_EDIT("ENTER_EDIT", "进入编辑"),
    EXIT_EDIT("EXIT_EDIT", "退出编辑"),
    EDIT_ACTION("EDIT_ACTION", "执行编辑操作");

    private final String type; // 消息类型，例如 "ENTER_EDIT", "EXIT_EDIT", "EDIT_ACTION"

    private final String value;

    private EditEnums(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }
    public String getValue() {
        return value;
    }


    public static EditEnums getByType(String type) {
        for (EditEnums editEnum : EditEnums.values()) {
            if (editEnum.getType().equals(type)) {
                return editEnum;
            }
        }
        return null; // 如果没有找到匹配的类型，返回 null
    }


}
