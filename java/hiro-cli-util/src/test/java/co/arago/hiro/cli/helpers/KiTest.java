/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.arago.hiro.cli.helpers;

import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author fotto
 */
public class KiTest {

  private static final String ki = "ki\n"
    + "  id: \"Test:KI:400a\"\n"
    + "  title: \"LocalExec test KI_400a\"\n"
    + "  description: \"Run /bin/false with capability ExecuteLocalCommand - check for correc error\"\n"
    + "on\n"
    + "  ogit/_custom_id == \"Test:KI_40x:Machine:1\"\n" +
"when:\n" +
"  IntegrationTest_Todo == \"KI_400a\"\n" +
 "do\n"
    + "  stdout: LOCAL::OUTPUT,\n" +
"  stderr: LOCAL::ERROR,\n" +
"  result: LOCAL::RESULT = action(\"ExecuteLocalCommand\", timeout: 60, command: \"/bin/false\")\n" +
"  log(\"res=${RESULT} out=${OUTPUT} err=${ERROR}\")";

  public KiTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void parseKI() {
    Map<String, String> metaData = Ki.parseKiMetaData(ki);
    assertEquals("Test:KI:400a", metaData.get("id"));
    assertEquals("LocalExec test KI_400a", metaData.get("title"));
    assertNotNull(metaData.get("description"));
  }
}
