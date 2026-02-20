package io.appmetrica.analytics.impl.db;

import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConstantsTest extends CommonTest {

    @Test
    public void testReportTableContainsAllColumns() {
        testColumns(
            Pattern.compile("CREATE TABLE IF NOT EXISTS "
                + Constants.EventsTable.TABLE_NAME +
                " \\((id) INTEGER PRIMARY KEY,((([ ]*([a-zA-Z_]*) ((TEXT)|(INTEGER)|(BLOB)))(( DEFAULT (('')|(0)|(1)|(2)|(-1)|('\\{\\}')))*),*)*) \\)"
            ),
            Constants.EventsTable.CREATE_TABLE,
            Constants.EventsTable.ACTUAL_COLUMNS
        );
    }

    @Test
    public void testSessionTableContainsAllColumns() {
        testColumns(
            Pattern.compile("CREATE TABLE IF NOT EXISTS "
                + Constants.SessionTable.TABLE_NAME +
                " \\((id) INTEGER,((([ ]*([a-zA-Z_]*) ((TEXT)|(INTEGER)|(BLOB)))(( DEFAULT (('')|(0)|(2)|('\\{\\}')))*),*)*) \\)"
            ),
            Constants.SessionTable.CREATE_TABLE,
            Constants.SessionTable.ACTUAL_COLUMNS
        );
    }

    private void testColumns(Pattern createScriptPattern, String createScript, List<String> actualColumns) {
        assertThat(createScriptPattern.matcher(createScript).matches()).isTrue();
        Matcher matcher = createScriptPattern.matcher(createScript);
        matcher.find();
        HashSet<String> existedKeys = new HashSet<String>();
        for (String string : cutColumnDescription(matcher.group(2))
            .split(",")) {
            existedKeys.add(string.trim());
        }
        existedKeys.add(cutColumnDescription(matcher.group(1)).trim());

        for (String column : actualColumns) {
            assertThat(existedKeys).contains(column);
            existedKeys.remove(column);
        }
        assertThat(existedKeys).isEmpty();
    }

    private String cutColumnDescription(String in) {
        return in.replace("TEXT", "")
            .replace("INTEGER", "")
            .replace("BLOB", "")
            .replace("DEFAULT", "")
            .replace("PRIMARY", "")
            .replace("KEY", "")
            .replaceAll("(0)|(1)|(2)|(-1)", "")
            .replace("''", "")
            .replace("'{}'", "");
    }
}
