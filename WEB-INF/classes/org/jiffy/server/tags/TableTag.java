package org.jiffy.server.tags;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

public final class TableTag extends BodyTagSupport
{
	// private static Category _logger = Category.getInstance(TableTag.class.getName());

	ArrayList _arTRs = null;
	String _sOldBody = null;

	String _sAlign = null;
	String _sBGColor = null;
	String _sBorder = null;
	String _sCellPadding = null;
	String _sCellSpacing = null;
	String _sHeight = null;
	String _sWidth = null;
	String _sCols = null;
	String _sHSpace = null;
	String _sVSpace = null;
	String _sID = null;

	// CSS Style Support

	/** Style attribute associated with component. */
	private String _sStyle = null;

	/** Named Style class associated with component. */
	private String _sStyleClass = null;

	/** Identifier associated with component. */
	private String _sStyleId = null;

	public void setId(String id)
	{
		_sID = id;
	}

	public String getId()
	{
		return _sID;
	}

	public void setAlign(String s)
	{
		_sAlign = s;
	}

	public String getAlign()
	{
		return _sAlign;
	}

	public void setBgColor(String s)
	{
		_sBGColor = s;
	}

	public String getBgColor()
	{
		return _sBGColor;
	}

	public void setBorder(String s)
	{
		_sBorder = s;
	}

	public String getBorder()
	{
		return _sBorder;
	}

	public void setCellPadding(String s)
	{
		_sCellPadding = s;
	}

	public String getCellPadding()
	{
		return _sCellPadding;
	}

	public void setCellSpacing(String s)
	{
		_sCellSpacing = s;
	}

	public String getCellSpacing()
	{
		return _sCellSpacing;
	}

	public void setHeight(String s)
	{
		_sHeight = s;
	}

	public String getHeight()
	{
		return _sHeight;
	}

	public void setWidth(String s)
	{
		_sWidth = s;
	}

	public String getWidth()
	{
		return _sWidth;
	}

	public void setCols(String s)
	{
		_sCols = s;
	}

	public String getCols()
	{
		return _sCols;
	}

	public void sethSpace(String s)
	{
		_sHSpace = s;
	}

	public String gethSpace()
	{
		return _sHSpace;
	}

	public void setvSpace(String s)
	{
		_sVSpace = s;
	}

	public String getvSpace()
	{
		return _sVSpace;
	}

	// CSS Style Support

	/** Sets the style attribute. */
	public void setStyle(String style)
	{
		this._sStyle = style;
	}

	/** Returns the style attribute. */
	public String getStyle()
	{
		return _sStyle;
	}

	/** Sets the style class attribute. */
	public void setStyleClass(String styleClass)
	{
		this._sStyleClass = styleClass;
	}

	/** Returns the style class attribute. */
	public String getStyleClass()
	{
		return _sStyleClass;
	}

	/** Sets the style id attribute. */
	public void setStyleId(String styleId)
	{
		this._sStyleId = styleId;
	}

	/** Returns the style id attribute. */
	public String getStyleId()
	{
		return _sStyleId;
	}

	public int doStartTag() throws JspException
	{

		_arTRs = new ArrayList();
		// return(EVAL_BODY_TAG);
		return (EVAL_BODY_BUFFERED);
	}

	public int doAfterBody() throws JspException
	{

		BodyContent bc = getBodyContent();

		_sOldBody = bc.getString();
		bc.clearBody();

		return SKIP_BODY;
	}

	public int doEndTag() throws JspException
	{

		JspWriter writer = pageContext.getOut();
		try
		{
			writer.print(generateTable());
		}
		catch (IOException e)
		{
			// _logger.error("Exception:", e);
			throw new JspException(e.getMessage());
		}

		return (EVAL_PAGE);
	}

	/**
	 * Release any acquired resources.
	 */
	public void release()
	{

		super.release();

		_arTRs = null;
		_sOldBody = null;
		_sAlign = null;
		_sBGColor = null;
		_sBorder = null;
		_sCellPadding = null;
		_sCellSpacing = null;
		_sHeight = null;
		_sWidth = null;
		_sCols = null;
		_sHSpace = null;
		_sVSpace = null;
		_sID = null;

	}

	public void addTC(ArrayList arTCIN)
	{

		String sTD = null;

		ArrayList arTR = null;

		for (int i = 0; i < arTCIN.size(); i++)
		{
			sTD = (String) arTCIN.get(i);
			if (_arTRs.size() - 1 >= i)
			{
				arTR = (ArrayList) _arTRs.get(i);
				arTR.add(sTD);
				_arTRs.set(i, arTR);
				// _logger.debug("setting " + sTD + " to position " + i + " in TR List");
			}
			else
			{
				arTR = new ArrayList();
				arTR.add(sTD);
				_arTRs.add(arTR);
				// _logger.debug("adding " + sTD + " to position " + i + " in TR List");
			}
		}

	}

	private String generateTable()
	{

		StringBuffer sb = new StringBuffer();
		ArrayList arTR = null;
		//String sTD = null;

		//int iRowNum = 0;

		sb.append("<table");
		if (_sAlign != null)
		{
			sb.append(" align=\"").append(_sAlign).append("\"");
		}

		if (_sBGColor != null)
		{
			sb.append(" bgcolor=\"").append(_sBGColor).append("\"");
		}

		if (_sBorder != null)
		{
			sb.append(" border=\"").append(_sBorder).append("\"");
		}

		if (_sCellPadding != null)
		{
			sb.append(" cellpadding=\"").append(_sCellPadding).append("\"");
		}

		if (_sCellSpacing != null)
		{
			sb.append(" cellspacing=\"").append(_sCellSpacing).append("\"");
		}

		if (_sHeight != null)
		{
			sb.append(" height=\"").append(_sHeight).append("\"");
		}

		if (_sWidth != null)
		{
			sb.append(" width=\"").append(_sWidth).append("\"");
		}

		if (_sCols != null)
		{
			sb.append(" cols=\"").append(_sCols).append("\"");
		}

		if (_sHSpace != null)
		{
			sb.append(" hspace=\"").append(_sHSpace).append("\"");
		}

		if (_sVSpace != null)
		{
			sb.append(" vspace=\"").append(_sVSpace).append("\"");
		}

		if (_sStyle != null)
		{
			sb.append(" style=\"").append(_sStyle).append("\"");
		}

		if (_sStyleClass != null)
		{
			sb.append(" class=\"").append(_sStyleClass).append("\"");
		}

		if (_sStyleId != null)
		{
			sb.append(" id=\"").append(_sStyleId).append("\"");
		}

		if (_sID != null)
		{
			sb.append(" id=\"").append(_sID).append("\"");
		}

		sb.append(">");
		sb.append(_sOldBody);
		sb.append("<thead>");

		boolean hasHeadEnded = false;
		for (int i = 0; i < _arTRs.size(); i++)
		{
			// 9.0
			if (((ArrayList) _arTRs.get(i)).size() > 0 && ((ArrayList) _arTRs.get(i)).get(0).toString().indexOf("</th>") == -1)
			{
				if (!hasHeadEnded)
				{
					sb.append("</thead><tbody>");
					hasHeadEnded = true;
				}
			}
			// end 9.0

			if (i == 0)
				sb.append(" <tr class=\"header\">");
			else
				sb.append(" <tr>\n");

			arTR = (ArrayList) _arTRs.get(i);
			for (int k = 0; k < arTR.size(); k++)
			{
				if (arTR.get(k) != null)
				{
					sb.append(arTR.get(k));
				}
			}
			sb.append("\n </tr>\n");
		}
		sb.append("</tbody></table>");

		return sb.toString();
	}

}
