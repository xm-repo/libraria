package libraria.document.attributes;

import java.util.HashMap;
import java.util.Map;

public class DocumentAttributes {

    private final Map<Attributes, String> attributes = new HashMap<>();

    public String getAttribute(Attributes attribute) {
        String attr = attributes.get(attribute);
        return attr != null ? attr : "";
    }

    public void setAttribute(Attributes attribute, String value) {
        attributes.put(attribute, value);
    }

}
