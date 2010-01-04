package com.plexobject.rbac.domain;

import org.junit.Assert;
import org.junit.Test;

public class DomainTest {

    @Test
    public void testGetSetEmpty() {
        Domain app = new Domain();
        Assert.assertNull(app.getID());
        Assert.assertNull(app.getDescription());
    }

}
