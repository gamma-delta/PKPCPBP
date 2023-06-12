package at.at.petrak.pkpcpbp.filters;

import at.petrak.pkpcpbp.filters.FlatteningJson5Transmogrifier;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.fail;

class FlatteningJson5TransmogrifierTest {
    private static final String testInput = """
            {
              lore: {
                "": "Lore",
                desc: "I have uncovered some letters and text not of direct relevance to my art. \\
                  But, I think I may be able to divine some of the history of the world from these. Let me see..."
              },
                    
              interop: {
                "": "Cross-Mod Compatibility",
                desc: "It appears I have installed some mods Hexcasting interoperates with! I've detailed them here."
              },
             \s
              patterns: {
                "": "Patterns",
                desc: "A list of all the patterns I've discovered, as well as what they do."
              },
              spells: {
                "": "Spells",
                desc: "Patterns and actions that perform a magical effect on the world."
              },
              great_spells: {
                "": "Great Spells",
                desc: "The spells catalogued here are purported to be of legendary difficulty and power. \\
                  They seem to have been recorded only sparsely (for good reason, the texts claim). \\
                  It's probably just the ramblings of extinct traditionalists, though -- a pattern's a pattern.$(br2)\\
                  What could possibly go wrong?"
              },
            }""";

    @Test
    void test() {
        var reader = new StringReader(testInput);

        try {
            var it = new FlatteningJson5Transmogrifier(reader);
            System.out.println(it);
        } catch (Exception e) {
            fail("building transmogrifier failed for this reason: ", e);
        }
    }
}