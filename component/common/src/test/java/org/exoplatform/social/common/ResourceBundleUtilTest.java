/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.common;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 * Unit Test for {@link ResourceBundleUtil}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since  Feb 6, 2012
 */
public class ResourceBundleUtilTest extends TestCase {

  public void testReplaceArgumentsWithArrayArguments() {
    String input = "Hello {0}";
    String[] arguments = new String[] {"World"};
    String output = ResourceBundleUtil.replaceArguments(input, arguments);
    assertEquals("output must be: Hello World", "Hello World", output);

    arguments = new String[]{};
    output = ResourceBundleUtil.replaceArguments(input, arguments);
    assertEquals("output must be: " + input, input, output);

    arguments = new String[] {"Out There", "World"};
    output = ResourceBundleUtil.replaceArguments(input, arguments);
    assertEquals("output must be: Hello Out There", "Hello Out There", output);
  }

  public void testReplaceArgumentsWithListArguments() {
    String input = "Hello {0}";
    List<String> arguments = Arrays.asList(new String[] {"World"});
    String output = ResourceBundleUtil.replaceArguments(input, arguments);
    assertEquals("output must be: Hello World", "Hello World", output);

    arguments = Arrays.asList(new String[]{});
    output = ResourceBundleUtil.replaceArguments(input, arguments);
    assertEquals("output must be: " + input, input, output);

    arguments = Arrays.asList(new String[] {"Out There", "World"});
    output = ResourceBundleUtil.replaceArguments(input, arguments);
    assertEquals("output must be: Hello Out There", "Hello Out There", output);
  }


  public void testReplaceArgumentAdvanced() {
    String input = "At {2} on {3}, we detected {1} spaceships on the planet {0}";
    String[] arguments = new String[] {"Mars", "10", "02:00 PM", "Feb 6, 2012"};
    String output = ResourceBundleUtil.replaceArguments(input, arguments);
    String expected = "At 02:00 PM on Feb 6, 2012, we detected 10 spaceships on the planet Mars";
    assertEquals("output must be: " + expected, expected, output);
  }

}
