/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.XElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A question in the Phsychologist's test.
 * @author akarnokd, 2011.04.20.
 */
public class TestQuestion {
    /** The question identifier. */
    public String id;
    /** The question label. */
    public String label;
    /** The available answers. */
    public final List<TestAnswer> answers = new ArrayList<>();
    /**
     * Choose an option and mark its answer as selected while deselect the others.
     * @param id the id to select
     */
    public void choose(String id) {
        for (TestAnswer ta : answers) {
            ta.selected = ta.id.equals(id);
        }
    }
    /**
     * Parse a test.xml.
     * @param test the test XML
     * @param result the target output
     * @return the list of questions
     */
    public static Map<String, TestQuestion> parse(XElement test,

            Map<String, TestQuestion> result) {
        for (XElement xq : test.childrenWithName("question")) {
            TestQuestion tq = new TestQuestion();
            tq.id = xq.get("id");
            tq.label = xq.get("label");
            for (XElement xa : xq.childrenWithName("answer")) {
                TestAnswer ta = new TestAnswer(xa.get("id"));
                ta.label = xa.get("label");
                ta.selected = "true".equals(xa.get("selected", "false"));
                ta.points = xa.getInt("points");
                tq.answers.add(ta);
            }
            result.put(tq.id, tq);
        }

        return result;
    }
}
