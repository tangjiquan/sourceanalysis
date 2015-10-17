/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web.tag;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * GroupTag is a tag that adds a group to an ancestor CacheTag's groups.<p>
 *
 * @author <a href="mailto:robvdv@yahoo.com">Robert van der Vliet</a>
 */
public class GroupTag extends TagSupport {
    private Object group = null;

    public int doStartTag() throws JspTagException {
        CacheTag ancestorCacheTag = (CacheTag) TagSupport.findAncestorWithClass(this, CacheTag.class);

        if (ancestorCacheTag == null) {
            throw new JspTagException("GroupTag cannot be used from outside a CacheTag");
        }

        ancestorCacheTag.addGroup(String.valueOf(group));
        return EVAL_BODY_INCLUDE;
    }

    public void setGroup(Object group) {
        this.group = group;
    }
}
