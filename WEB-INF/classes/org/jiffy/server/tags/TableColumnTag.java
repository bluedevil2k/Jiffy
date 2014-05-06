package org.jiffy.server.tags;

import java.util.ArrayList;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

public final class TableColumnTag extends BodyTagSupport
{
	ArrayList _arTDs = null;
	String _sOldBody = null;

	public int doStartTag() throws JspException
	{

		_arTDs = new ArrayList();
		return (EVAL_BODY_BUFFERED);
	}

	public int doAfterBody() throws JspException
	{

		BodyContent bc = getBodyContent();

		_sOldBody = bc.getString();
		bc.clearBody();

		TableTag tableTag = (TableTag) findAncestorWithClass(this, TableTag.class);

		ArrayList arTDs = this.getTDs(_sOldBody);
		if (arTDs != null && arTDs.size() > 0)
		{
			tableTag.addTC(arTDs);
		}

		return SKIP_BODY;
	}

	public int doEndTag() throws JspException
	{

		return (EVAL_PAGE);
	}

	/**
	 * Release any acquired resources.
	 */
	public void release()
	{

		super.release();

		_arTDs = null;
		_sOldBody = null;

	}

	private ArrayList getTDs(String sIN)
	{

		ArrayList arTDs = new ArrayList();
		int iEnd = -1;
		int iStart = 0;
		String sTD = null;

		while (indexOf(sIN, "<th", "<td", iStart) != -1)
		{
			iEnd = indexOf(sIN, "</th>", "</td>", iStart);
			if (iEnd == -1)
			{
				return null;
			}
			sTD = sIN.substring(iStart, iEnd + 5);

			if (indexOf(sTD, "colspan=\"0\"", "colspan=0", 0) != -1)
			{
				arTDs.add(null);
			}
			else
			{
				arTDs.add(sTD);
			}
			iStart = iEnd + 5;
		}

		return arTDs;
	}

	public static int indexOf(String sIN, String sSearch1, String sSearch2, int iStartPos)
	{
		int iIndex1 = -1;
		int iIndex2 = -1;
		String sLower = sIN.toLowerCase();

		iIndex1 = sLower.indexOf(sSearch1, iStartPos);
		iIndex2 = sLower.indexOf(sSearch2, iStartPos);

		if (iIndex1 == -1 && iIndex2 == -1)
		{
			return -1;
		}
		else if (iIndex1 == -1)
		{
			return iIndex2;
		}
		else if (iIndex2 == -1)
		{
			return iIndex1;
		}
		else
		{
			return Math.min(iIndex1, iIndex2);
		}
	}

}
