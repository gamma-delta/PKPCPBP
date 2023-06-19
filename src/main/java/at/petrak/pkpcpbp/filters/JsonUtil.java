package at.petrak.pkpcpbp.filters;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class JsonUtil {
    private static final Jankson JANKSON = Jankson.builder().build();

    public static StringReader processJson(Reader in, boolean flatten) throws IOException, SyntaxError {
        var jsonSrc = JsonUtil.jesusChristJustGetTheGodDamnReaderIntoAFuckingStream(in);
        JsonObject asJson;
        try {
            // that is, replace `\<LF>       foobar` with `\<LF>foobar`
            asJson = JANKSON.load(jsonSrc.replace("\r\n", "\n").replaceAll("\\\\\\n\\s*", "\\\\\n"));
        } catch (SyntaxError exn) {
            throw new RuntimeException(exn.getCompleteMessage(), exn);
        }

        JsonObject out = flatten
            ? flattenObject(asJson)
            : asJson;
        var unTfedSrc = out.toJson(JsonGrammar.STRICT);
        return new StringReader(unTfedSrc);
    }

    private static JsonObject flattenObject(JsonObject obj) {
        var out = new JsonObject();
        flattenInner(null, obj, out);
        return out;
    }

    private static void flattenInner(String prefix, JsonObject obj, JsonObject out) {
        obj.forEach((keyStub, val) -> {
            String key;
            if (prefix == null || prefix.isEmpty()) {
                // Root
                key = keyStub;
            } else if (keyStub.isEmpty()) {
                // Allow cases like: { foo: { "": 123, bar: 456 } }
                // Flattens to { "foo": 123, "foo.bar": 456 }
                key = prefix;
            } else if (":_-/".indexOf(prefix.charAt(prefix.length() - 1)) != -1) {
                // Check if the prefix ends with specific things
                // Allow { "paucal:": { item1: "Item 1" } }
                // flattens to { "paucal:item1": "Item 1" }
                key = prefix + keyStub;
            } else {
                // Normal case
                key = prefix + "." + keyStub;
            }

            if (val instanceof JsonObject subobj) {
                flattenInner(key, subobj, out);
            } else {
                out.put(key, val);
            }
        });
    }

    public static String jesusChristJustGetTheGodDamnReaderIntoAFuckingStream(Reader in) throws IOException {
        // https://frontbackend.com/java/how-to-convert-reader-to-string-in-java
        char[] buffer = new char[4096];
        StringBuilder bob = new StringBuilder();

        // i love cursed for loops
        for (int numChars; (numChars = in.read(buffer)) >= 0; ) {
            bob.append(buffer, 0, numChars);
        }

        return bob.toString().replaceAll("\r\n", "\n");
    }
}
