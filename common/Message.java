package common;

import java.io.Serializable;

public class Message implements Serializable {

    private MessageType type;
    private String data;
    private String source;

    private static final long serialVersionUID = 1L;

    public Message(MessageType type, String data, String source) {
        this.type = type;
        this.data = data;
        this.source = source;

        if (this.type == MessageType.TYPING){
            this.data = source + " is typing ... ";
        }
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public String getSource() {
        return source;
    }
}
