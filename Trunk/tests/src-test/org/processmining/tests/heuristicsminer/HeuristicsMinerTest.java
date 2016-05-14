package org.processmining.tests.heuristicsminer;
import org.junit.Test;
import org.processmining.contexts.cli.CLI;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.AssertionFailedError;

public class HeuristicsMinerTest extends TestCase {

  @Test
  public void testHeuristicsMiner1() throws Throwable {
    String args[] = new String[] {"-l"};
    CLI.main(args);
  }

  @Test
  public void testHeuristicsMiner2() throws Throwable {
    String testFileRoot = System.getProperty("test.testFileRoot", ".");
    String args[] = new String[] {"-f", "/Users/Jan/Documents/Development/HeuristicsMiner/Trunk/tests/testfiles/HeuristicsMiner_Example.txt"};
    
    CLI.main(args);
  }
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(HeuristicsMinerTest.class);
  }
  
}
