package com.plexobject.rbac.domain;

import org.junit.Assert;
import org.junit.Test;

public class ApplicationTest {

    @Test
    public void testGetSetEmpty() {
        Application app = new Application();
        Assert.assertNull(app.getID());
        Assert.assertNull(app.getDescription());
    }

}
